package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.external.services.printscript.request.FormatRequest
import com.example.springboot.app.external.services.printscript.request.LintRequest
import com.example.springboot.app.external.services.printscript.request.PSRequest
import com.example.springboot.app.external.services.printscript.response.PSResponse
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.rules.RulesService
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.enums.Compliance
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.snippets.ControllerUtils
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestCaseResult
import com.example.springboot.app.utils.FormatConfig
import com.example.springboot.app.utils.UserUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.context.annotation.Lazy
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
        headers: HttpHeaders
    ): ResponseEntity<PSResponse> {
        val url = "$psUrl/format"
        val requestEntity = HttpEntity(snippetId, headers)
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to format snippet")
        }
        return response
    }

//    fun lint(
//        snippetId: String,
//        headers: HttpHeaders
//    ): ResponseEntity<PSResponse> {
//        val url = "$psUrl/lint"
//        val requestEntity = HttpEntity(snippetId, headers)
//        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
//        if (response.body == null) {
//            throw Exception("Failed to lint snippet")
//        }
//        return response
//    }

    // for async formatting
    fun autoFormat(
        snippetId: String,
        userId: String,
        rules: List<RuleDTO>
    ){
        val url = "$psUrl/format"
        val jwt = userUtils.getAuth0AccessToken()
        val formatConfig = generateFormatConfig(rules)
        val requestEntity = HttpEntity(FormatRequest(snippetId, userId, formatConfig), ControllerUtils.generateHeadersFromStr(jwt!!))
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        processResponse(response, userId, rules)
    }

    fun autoLint(
        snippetId: String,
        userId: String,
        rules: List<RuleDTO>
    ){
        val url = "$psUrl/lint"
        val jwt = userUtils.getAuth0AccessToken()
        val requestEntity = HttpEntity(LintRequest(snippetId, userId, rules), ControllerUtils.generateHeadersFromStr(jwt!!))
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        processResponse(response, userId, rules)
    }

    fun runTests(test: TestCase, userId: String): TestCaseResult {
        val url = "$psUrl/run_tests"
        val requestEntity = HttpEntity(test)
        val response = restTemplate.postForEntity(url, requestEntity, String::class.java)
        if (response.body == null) {
            throw Exception("Failed to run tests")
        }
        return if (response.body == "success") {
            TestCaseResult.SUCCESS
        } else {
            TestCaseResult.FAIL
        }
    }

    private fun processResponse(response: ResponseEntity<PSResponse>, userId: String, rules: List<RuleDTO>): ResponseEntity<String> {
        return when {
            response.statusCode.is2xxSuccessful -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, Compliance.COMPLIANT)
                }
                ResponseEntity.ok(response.body!!.message)
            }
            response.statusCode.is4xxClientError -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, Compliance.NOT_COMPLIANT)
                }
                ResponseEntity.status(400).body(response.body!!.message)
            }
            response.statusCode.is5xxServerError -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, Compliance.FAILED)
                }
                ResponseEntity.status(500).body(response.body!!.message)
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