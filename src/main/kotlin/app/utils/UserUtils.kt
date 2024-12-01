package com.example.springboot.app.utils

import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class UserUtils
    @Autowired
    constructor(
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private val auth0URL: String,

        private val rest: RestTemplate,
) {
        private val logger = LoggerFactory.getLogger(UserUtils::class.java)

    fun getUsers(
        page: Int,
        perPage: Int,
        nickname: String,
        jwt: Jwt
    ): ResponseEntity<List<AuthUserDTO>> {
        try {
            val request: HttpEntity<Void> = HttpEntity(generateHeaders(jwt))
            return rest.exchange(
                "$auth0URL/api/v2/users?" +
                        "filter=user_id,nickname" +
                        "&per_page=$perPage" +
                        "&page=$page" +
                        "&q=nickname:*$nickname*",
                HttpMethod.GET,
                request,
                object : ParameterizedTypeReference<List<AuthUserDTO>>() {},
            )
        } catch (e: HttpClientErrorException) {
            logger.error("HTTP error while fetching users: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            throw e
        }
    }


    fun getUserById(userId: String, headers: HttpHeaders): ResponseEntity<AuthUserDTO> {
        try {
            val request: HttpEntity<Void> = HttpEntity(headers)
            return rest.exchange(
                "$auth0URL/api/v2/users/$userId",
                HttpMethod.GET,
                request,
                AuthUserDTO::class.java,
            )
        } catch (e: HttpClientErrorException) {
            throw e
        }
    }
}