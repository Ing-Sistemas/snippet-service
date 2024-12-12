package com.example.springboot.app.external.services.printscript

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.enums.TestCaseResult
import org.springframework.security.oauth2.jwt.Jwt

interface LanguageService {

    fun validateSnippet(
        snippetId: String,
        version: String,
        headers: HttpHeaders
    ): ResponseEntity<PSValResponse>

    fun format(
        snippetId: String,
        jwt: Jwt
    ): ResponseEntity<String>

    fun autoFormat(
        snippetId: String,
        jwt: Jwt,
        rules: List<CompleteRuleDTO>
    )

    fun autoLint(
        snippetId: String,
        jwt: Jwt,
        rules: List<CompleteRuleDTO>
    )

    fun runTests(
        test: RunTestDTO,
        headers: HttpHeaders,
        sId: String
    ): TestCaseResult
}