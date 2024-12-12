package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.external.services.printscript.request.FormatRequest
import com.example.springboot.app.external.services.printscript.request.LintRequest
import com.example.springboot.app.external.services.printscript.request.PSRequest
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.rules.RulesService
import com.example.springboot.app.rules.model.dto.RuleDTO
import com.example.springboot.app.rules.enums.SnippetStatus
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import org.springframework.http.HttpMethod.POST
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.snippets.SnippetService
import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.enums.TestCaseResult
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

@Service
class PrintScriptService @Autowired constructor (
    private val restTemplate: RestTemplate,
    private val snippetService: SnippetService,
    @Lazy private val rulesService: RulesService,

): LanguageService {
    @Value("\${spring.constants.print_script_url}") private lateinit var psUrl: String

    override fun validateSnippet(
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
    override fun format(
        snippetId: String,
        jwt: Jwt
    ): ResponseEntity<String> {
        val url = "$psUrl/format"
        val formatConfig = rulesService.getRules(RulesetType.FORMAT, getUserIdFromJWT(jwt))
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
    override fun autoFormat(
        snippetId: String,
        jwt: Jwt,
        rules: List<CompleteRuleDTO>
    ){
        val url = "$psUrl/format"
        val userId = getUserIdFromJWT(jwt)
        val requestEntity = HttpEntity(FormatRequest(snippetId, rules), generateHeaders(jwt))
        val response = restTemplate.exchange(
            url,
            POST,
            requestEntity,
            object : ParameterizedTypeReference<String> () {}
        )
        processFormatResponse(response, userId, rules)
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


    override fun autoLint(
        snippetId: String,
        jwt: Jwt,
        rules: List<CompleteRuleDTO>
    ){
        val url = "$psUrl/lint"
        val userId = getUserIdFromJWT(jwt)
        val requestEntity = HttpEntity(LintRequest(snippetId, rules), generateHeaders(jwt))
        val response = restTemplate.exchange(url,
            POST,
            requestEntity,
            object : ParameterizedTypeReference<List<String>>() {}
        )
        println("Response from linting: ${response.body}")
        processLintResponse(response, userId, snippetId)
    }


    override fun runTests(test: RunTestDTO, headers: HttpHeaders, sId: String): TestCaseResult {
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


    private fun processLintResponse(response: ResponseEntity<List<String>>, userId: String, snippetId: String): ResponseEntity<List<String>> {
        return when {
            response.statusCode.is2xxSuccessful -> {
                snippetService.changeSnippetStatus(snippetId, SnippetStatus.COMPLIANT)
                ResponseEntity.ok(response.body!!)
            }
            response.statusCode.is4xxClientError -> {
                snippetService.changeSnippetStatus(snippetId, SnippetStatus.NOT_COMPLIANT)
                ResponseEntity.status(400).body(response.body!!)
            }
            response.statusCode.is5xxServerError -> {
                snippetService.changeSnippetStatus(snippetId, SnippetStatus.FAILED)
                ResponseEntity.status(500).body(response.body!!)
            }
            else -> {
                throw Exception("Failed to process response")
            }
        }
    }

    private fun processFormatResponse(response: ResponseEntity<String>, userId: String, rules: List<CompleteRuleDTO>): ResponseEntity<String> {
        return when {
            response.statusCode.is2xxSuccessful -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, SnippetStatus.SUCCESS)
                }
                ResponseEntity.ok(response.body!!)
            }
            response.statusCode.is4xxClientError -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, SnippetStatus.FAILED)
                }
                ResponseEntity.status(400).body(response.body!!)
            }
            response.statusCode.is5xxServerError -> {
                rules.forEach { rule ->
                    rulesService.changeUserRuleCompliance(userId, rule.id, SnippetStatus.FAILED)
                }
                ResponseEntity.status(500).body(response.body!!)
            }
            else -> {
                throw Exception("Failed to process response")
            }
        }
    }
}