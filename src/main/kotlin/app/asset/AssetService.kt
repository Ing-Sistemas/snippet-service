package com.example.springboot.app.asset

import com.example.springboot.app.controller.ControllerUtils.generateHeaders
import com.example.springboot.app.controller.SnippetController
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient



@Component
class AssetService{
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)
    private val client: WebClient = WebClient.builder()
        .baseUrl("http://host.docker.internal:8083")
        .build()

    fun saveSnippet(snippetId: String, snippetCode: String): ResponseEntity<String> {
        logger.info("Saving snippet with id: $snippetId")
        return try {
//            val fileContentFlux: Flux<DataBuffer> = DataBufferUtils.readInputStream(
//                { snippetFile.inputStream },
//                DefaultDataBufferFactory(),
//                4096
//            )
            val response = client.put()
                .uri("/v1/asset/${CONTAINER}/${snippetId}")
                .header("accept", "*/*")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(snippetCode)
                .retrieve()
                .toEntity(String::class.java)
                .block()!!
            logger.info("Response: $response")

            response
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Failed to save snippet: ${e.message}")
        }
    }

    fun getSnippet(snippetId: String, jwt: Jwt): ResponseEntity<String> {
        val header = generateHeaders(jwt)
        return try {
            val response = client.get()
                .uri("/v1/asset/{container}/{snippetId}", CONTAINER, snippetId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .toEntity(String::class.java)
                .block()!!

            logger.info("Response: $response.body!!")
            response
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Failed to retrieve snippet: ${e.message}")
        }
    }

    fun deleteSnippet(snippetId: String): ResponseEntity<String> {
        return try {
            client.delete()
                .uri("/v1/asset/test-container/{snippetId}", snippetId)
                .retrieve()
                .toBodilessEntity()
                .block()
            ResponseEntity.ok(null)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Failed to delete snippet: ${e.message}")
        }
    }

    companion object {
        private const val CONTAINER = "test-container"
    }
}