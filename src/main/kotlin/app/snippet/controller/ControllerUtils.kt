package com.example.springboot.app.snippet.controller

import com.example.springboot.app.auth.OAuth2ResourceServerSecurityConfiguration
import com.example.springboot.app.snippet.model.dto.SnippetDTO
import com.example.springboot.app.external.rest.request.SnippetRequestCreate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

object ControllerUtils {
    fun generateSnippetDTO(snippetRequestCreate: SnippetRequestCreate): SnippetDTO {
        return SnippetDTO(
            UUID.randomUUID().toString(),
            snippetRequestCreate.title,
            snippetRequestCreate.language,
            snippetRequestCreate.extension,
            snippetRequestCreate.version,
            snippetRequestCreate.compliance,
            snippetRequestCreate.author,
            snippetRequestCreate.code

        )
    }

    fun getUserIdFromJWT(jwt: Jwt): String {
        val auth = OAuth2ResourceServerSecurityConfiguration(
            System.getenv("AUTH0_AUDIENCE"),
            System.getenv("AUTH_SERVER_URI"),
            System.getenv("UI_URL")
        ).jwtDecoder()
        return auth.decode(jwt.tokenValue).subject!!
    }

    fun generateHeaders(jwt: Jwt): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Bearer ${jwt.tokenValue}")
            contentType = MediaType.APPLICATION_JSON
        }
    }

}