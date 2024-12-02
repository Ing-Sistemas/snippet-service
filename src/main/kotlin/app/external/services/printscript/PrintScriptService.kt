package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.external.services.printscript.request.FormatRequest
import com.example.springboot.app.external.services.printscript.request.LintRequest
import com.example.springboot.app.external.services.printscript.request.PSRequest
import com.example.springboot.app.external.services.printscript.response.PSResponse
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestCaseResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PrintScriptService @Autowired constructor (
    private val restTemplate: RestTemplate,
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

    // for async formatting
    fun autoFormat(
        snippetId: String,
        userId: String,
        rules: List<RuleDTO>
    ){
        val url = "$psUrl/auto_format"
        val requestEntity = HttpEntity(FormatRequest(snippetId, userId ,rules))
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to format snippet")//CHANGE, instead of throwing, change status in DB
        }
    }

    fun autoLint(
        snippetId: String,
        userId: String,
        rules: List<RuleDTO>
    ){
        val url = "$psUrl/auto_lint"
        val requestEntity = HttpEntity(LintRequest(snippetId, userId, rules))
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.statusCode.is4xxClientError) {
            throw Exception("Failed to lint snippet")//CHANGE, instead of throwing, change status in DB
        }
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
}