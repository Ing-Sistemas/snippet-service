package com.example.springboot.app.utils

import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient

@Component
class UserUtils @Autowired constructor(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private val auth0URL: String,

    @Value("\${spring.security.oauth2.resourceserver.jwt.client-secret}")
    private val auth0ClientSecret: String,

    @Value("\${spring.security.oauth2.resourceserver.jwt.client-id}")
    private val auth0ClientId: String,

    private val restTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(UserUtils::class.java)

    fun getAuth0AccessToken(): String? {
        val audience = auth0URL + "api/v2/"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val requestBody = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", auth0ClientId)
            add("client_secret", auth0ClientSecret)
            add("audience", audience)
        }

        val entity = HttpEntity<MultiValueMap<String, String>>(requestBody, headers)
        val url = auth0URL + "oauth/token"

        return try {
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                object : ParameterizedTypeReference<Map<String, Any>>() {}
            )

            val responseBody = response.body
            val accessToken = responseBody?.get("access_token") as? String
            logger.info(accessToken)
            accessToken ?: throw RuntimeException("Access token not found in response")
        } catch (e: HttpClientErrorException) {
            logger.error("HTTP error while requesting Auth0 token: ${e.message}")
            null
        } catch (e: RestClientException) {
            logger.error("Unexpected error while requesting Auth0 token: ${e.message}")
            null
        }
    }

    fun getUsers(
        page: Int,
        perPage: Int,
        nickname: String,
    ): ResponseEntity<List<AuthUserDTO>> {
        return try {
            restTemplate.apply {
                messageConverters.add(FormHttpMessageConverter())
                messageConverters.add(StringHttpMessageConverter())

            }
            val token = getAuth0AccessToken()
            val headers = HttpHeaders().apply {
                add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }

            val entity = HttpEntity<Void>(headers)
            val url = auth0URL+ "api/v2/users"

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JsonNode::class.java,
            )

            logger.debug("Fetched users: {}", parseJsonToAuthUserDTOList(response.body!!))
            val users = parseJsonToAuthUserDTOList(response.body!!)
            ResponseEntity.ok(users)
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            throw e
        }
    }


    fun getUserById(userId: String, headers: HttpHeaders): ResponseEntity<AuthUserDTO> {
        try {
            val request: HttpEntity<Void> = HttpEntity(headers)
            return restTemplate.exchange(
                "$auth0URL/api/v2/users/$userId",
                HttpMethod.GET,
                request,
                AuthUserDTO::class.java,
            )
        } catch (e: HttpClientErrorException) {
            throw e
        }
    }

    fun parseJsonToAuthUserDTOList(json: JsonNode): List<AuthUserDTO> {
        return json.mapNotNull { userNode ->
            val userId = userNode["user_id"]?.asText()
            val nickname = userNode["nickname"]?.asText()
            if (userId != null && nickname != null) {
                AuthUserDTO(user_id = userId, nickname = nickname)
            } else {
                null
            }
        }
    }
}