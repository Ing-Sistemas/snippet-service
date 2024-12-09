package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.external.services.printscript.request.FormatRequest
import com.example.springboot.app.external.services.printscript.request.LintRequest
import com.example.springboot.app.external.services.printscript.request.PSRequest
import com.example.springboot.app.external.services.printscript.response.FormatResponse
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.rules.RulesService
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.enums.Compliance
import com.example.springboot.app.rules.enums.RulesetType
import org.springframework.http.HttpMethod.POST
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.rules.FormatRule
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.dto.TestCaseDTO
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestCaseResult
import com.example.springboot.app.utils.FormatConfig
import com.example.springboot.app.utils.UserUtils
import com.example.springboot.app.utils.TestRunHttp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.context.annotation.Lazy
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.client.exchange

@Service
class PrintScriptService @Autowired constructor (
    private val restTemplate: RestTemplate,
    private val userUtils: UserUtils,
    @Lazy private val rulesService: RulesService,

){
    @Value("\${spring.constants.print_script_url}") private lateinit var psUrl: String

    fun validateSnippet(
    snippetId: String,
    version: String,
    headers: HttpHeaders
    ): ResponseEntity<PSValResponse> {
        val url = "$psUrl/validate"
        val requestPSEntity = HttpEntity(PSRequest(version, snippetId), headers)
        val resPrintScript = restTemplate.postForEntity(url, requestPSEntity, PSValResponse::class.java)
        return when {
            resPrintScript.statusCode.is4xxClientError -> ResponseEntity.status(400).body(resPrintScript.body)
            resPrintScript.statusCode.is5xxServerError -> throw Exception("Failed to validate snippet in service")
            else -> resPrintScript
        }
    }


    // for sync formatting
    fun format(
        snippetId: String,
        jwt: Jwt
    ): ResponseEntity<String> {
        val url = "$psUrl/format"
        val formatConfig = generateFormatConfig(rulesService.getRules(RulesetType.FORMAT, getUserIdFromJWT(jwt)))
        val requestEntity = HttpEntity(FormatRequest(snippetId, formatConfig), generateHeaders(jwt))
        println(formatConfig.toString())
        val response = restTemplate.exchange(
            url,
            POST,
            requestEntity,
            object : ParameterizedTypeReference<String> () {}
        )
        if (response.statusCode.is2xxSuccessful) {
            return response
        } else {
            throw Exception("Failed to format snippet: ${response.body}")
        }
    }

    // for async formatting
    fun autoFormat(
        snippetId: String,
        jwt: Jwt,
        rules: List<RuleDTO>
    ){
        val url = "$psUrl/format"
        val formatConfig = generateFormatConfig(rules)
        val userId = getUserIdFromJWT(jwt)
        val requestEntity = HttpEntity(FormatRequest(snippetId, formatConfig), generateHeaders(jwt))
        val response = restTemplate.exchange(
            url,
            POST,
            requestEntity,
            object : ParameterizedTypeReference<String> () {}
        )
        processResponse(response, userId, rules)
    }

    /*
    fun lint(
        snippetId: String,
        headers: HttpHeaders
    ): ResponseEntity<PSResponse> {
        val url = "$psUrl/lint"
        val requestEntity = HttpEntity(snippetId, headers)
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to lint snippet")
        }
        return response
    }
     */


    fun autoLint(
        snippetId: String,
        jwt: Jwt,
        rules: List<RuleDTO>
    ){
        val url = "$psUrl/lint"
        val userId = getUserIdFromJWT(jwt)
        val requestEntity = HttpEntity(LintRequest(snippetId, rules), generateHeaders(jwt))
        val response = restTemplate.postForEntity(url, requestEntity, String::class.java)
        processResponse(response, userId, rules)
    }
    fun runTests(test: RunTestDTO, headers: HttpHeaders, sId: String): TestCaseResult {
        val url = "$psUrl/test/run_tests/${sId}"
        val requestEntity = HttpEntity(test, headers)
        val response = restTemplate.postForEntity(url, requestEntity, String::class.java)

        for (outPut in test.output) {
            return if (outPut != response.body) {
                TestCaseResult.FAIL
            } else {
                TestCaseResult.SUCCESS
            }
        }
        return TestCaseResult.SUCCESS

//        if (response.body == null) {
//            throw Exception("Failed to run tests ${response.body}")
//        }
//        return if (response.body == "success") {
//            TestCaseResult.SUCCESS
//        } else {
//            TestCaseResult.FAIL
//        }
    }

    private fun processResponse(response: ResponseEntity<String>, userId: String, rules: List<RuleDTO>): ResponseEntity<String> {
        return when {
            response.statusCode.is2xxSuccessful -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, Compliance.COMPLIANT)
                }
                ResponseEntity.ok(response.body!!)
            }
            response.statusCode.is4xxClientError -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, Compliance.NOT_COMPLIANT)
                }
                ResponseEntity.status(400).body(response.body!!)
            }
            response.statusCode.is5xxServerError -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, Compliance.FAILED)
                }
                ResponseEntity.status(500).body(response.body!!)
            }
            else -> {
                throw Exception("Failed to process response")
            }
        }
    }

    private fun generateFormatConfig(rules: List<RuleDTO>): FormatConfig {
        val configMap = rules
            .filter { it.ruleType == RulesetType.FORMAT }
            .associateBy { it.name }

        // abomination
        return FormatConfig(
            spaceBeforeColon = configMap["spaceBeforeColon"]?.value?.toString()?.toBoolean() ?: false,
            spaceAfterColon = configMap["spaceAfterColon"]?.value?.toString()?.toBoolean() ?: false,
            spaceAroundEquals = configMap["spaceAroundEquals"]?.value?.toString()?.toBoolean() ?: false,
            lineJumpBeforePrintln = configMap["lineJumpBeforePrintln"]?.value?.toString()?.toIntOrNull() ?: 0,
            lineJumpAfterSemicolon = configMap["lineJumpAfterSemicolon"]?.value?.toString()?.toBoolean() ?: false,
            singleSpaceBetweenTokens = configMap["singleSpaceBetweenTokens"]?.value?.toString()?.toBoolean() ?: true,
            spaceAroundOperators = configMap["spaceAroundOperators"]?.value?.toString()?.toBoolean() ?: true
        )
    }
}