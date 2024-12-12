package com.example.springboot.app.snippets

import com.example.springboot.app.external.auth.OAuth2ResourceServerSecurityConfiguration
import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.rules.enums.SnippetStatus
import com.example.springboot.app.snippets.dto.SnippetDTO
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.multipart.MultipartFile
import java.util.*
import org.springframework.mock.web.MockMultipartFile

object ControllerUtils {
    fun generateSnippetDTO(snippetRequestCreate: SnippetRequestCreate): SnippetDTO {
        return SnippetDTO(
            UUID.randomUUID().toString(),
            snippetRequestCreate.title,
            snippetRequestCreate.language,
            snippetRequestCreate.extension,
            snippetRequestCreate.version,
            SnippetStatus.PENDING
        )
    }

    fun getUserIdFromJWT(jwt: Jwt): String {
        val auth = OAuth2ResourceServerSecurityConfiguration(
            System.getenv("AUTH0_AUDIENCE"),
            System.getenv("AUTH_SERVER_URI")
        ).jwtDecoder()
        val subject =  auth.decode(jwt.tokenValue).subject
        return subject?.removePrefix("auth0|") ?: throw IllegalArgumentException("User ID is null or invalid")
    }

    fun generateHeaders(jwt: Jwt): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Bearer ${jwt.tokenValue}")
            contentType = MediaType.APPLICATION_JSON
        }
    }

    fun generateFile(snippetRequestCreate: SnippetRequestCreate): MultipartFile {
        return MockMultipartFile(
            snippetRequestCreate.title,
            snippetRequestCreate.title,
            snippetRequestCreate.extension,
            snippetRequestCreate.code.toByteArray()
        )
    }

    fun generateFileFromData(
        snippet: SnippetDTO,
        code: String
    ): MultipartFile {
        return MockMultipartFile(
            snippet.title,
            snippet.title,
            snippet.extension,
            code.toByteArray()
        )
    }

    fun getFileContent(file: MultipartFile): String {
        return String(file.bytes)
    }

}