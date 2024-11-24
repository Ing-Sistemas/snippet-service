package com.example.springboot.app.snippet.controller

import com.example.springboot.app.auth.OAuth2ResourceServerSecurityConfiguration
import com.example.springboot.app.snippet.dto.SnippetDTO
import com.example.springboot.app.external.request.SnippetRequestCreate
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
        )
    }

    fun getUserIdFromJWT(jwt: Jwt): String {
        val auth = OAuth2ResourceServerSecurityConfiguration(
            System.getenv("AUTH0_AUDIENCE"),
            System.getenv("AUTH_SERVER_URI")
        ).jwtDecoder()
        return auth.decode(jwt.tokenValue).subject!!
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

    fun getFileContent(file: MultipartFile): String {
        return String(file.bytes)
    }

}