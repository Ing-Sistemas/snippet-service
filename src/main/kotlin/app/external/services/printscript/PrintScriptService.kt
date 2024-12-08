package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.external.services.printscript.request.FormatRequest
import com.example.springboot.app.external.services.printscript.request.PSRequest
import com.example.springboot.app.external.services.printscript.response.PSResponse
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.rules.FormatRule
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.dto.TestCaseDTO
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestCaseResult
import com.example.springboot.app.utils.TestRunHttp
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
        rule: FormatRule,
    ){
        val url = "$psUrl/auto_format"
        val requestEntity = HttpEntity(FormatRequest(snippetId, rule))
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to format snippet")//todo, instead of throwing, change status in asset service maybe (?)
        }
    }

    fun autoLint(
        snippetId: String,
    ){
        val url = "$psUrl/auto_lint"
        val requestEntity = HttpEntity(snippetId)
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to lint snippet")//todo same as above jijiji
        }
    }
    fun runTests(test: RunTestDTO, userId: String, snippetId: String): TestCaseResult {
        val url = "$psUrl/run_tests"
        val entityToPass = TestRunHttp(test, snippetId)
        val requestEntity = HttpEntity(entityToPass)
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