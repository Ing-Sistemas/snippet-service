package com.example.springboot.app.service

import com.example.springboot.app.auth.OAuth2ResourceServerSecurityConfiguration
import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.dto.UpdateSnippetDTO
import com.example.springboot.app.repository.SnippetRepository
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.utils.PermissionRequest
import com.example.springboot.app.utils.PermissionResponse
import com.example.springboot.app.utils.SnippetRequestCreate
import com.example.springboot.app.utils.URLs.API_URL
import com.example.springboot.app.utils.URLs.BASE_URL
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class SnippetService (
    private val snippetRepository: SnippetRepository,
    private val restTemplate: RestTemplate
) {
    private val host = System.getenv().getOrDefault("HOST", "localhost")
    private val permissionPort = System.getenv().getOrDefault("PERMISSION_SERVICE_PORT", "none")

    fun createAndSetPermissions(
        snippetRequestCreate: SnippetRequestCreate,
        jwt: Jwt
    ): ResponseEntity<SnippetEntity> {
        return try {
            val snippetDTO = SnippetDTO(null, snippetRequestCreate.title, snippetRequestCreate.language)
            val savedSnippet = snippetRepository.save(translateToEntity(snippetDTO)) //to return
            val permUrl = "$BASE_URL$host:$permissionPort/$API_URL/create"
            val assetUrl = "$BASE_URL$host:..." //TODO

            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer ${jwt.tokenValue}")
                contentType = MediaType.APPLICATION_JSON
            }

            val requestEntity = HttpEntity(PermissionRequest(savedSnippet.id!!, jwt), headers)
            val res = restTemplate.postForEntity(
                permUrl,
                requestEntity,
                PermissionResponse::class.java
            )

            if (res.body != null) {
                println(res.body!!.permissions)
            } else {
                throw Exception("Failed to create permissions")

            }
            val auth = OAuth2ResourceServerSecurityConfiguration(
                System.getenv("AUTH0_AUDIENCE"),
                System.getenv("AUTH_SERVER_URI")
            ).jwtDecoder()

            val userId = auth.decode(jwt.tokenValue).subject!!
            println(userId)
            ResponseEntity.ok(savedSnippet)
        } catch (e: Exception) {
            println(e)
            ResponseEntity.status(500).body(null)
        }
    }

    fun updateSnippet(
        snippetId: String,
        updateSnippetDTO: UpdateSnippetDTO,
        jwt: Jwt
    ): ResponseEntity<SnippetEntity> {
        return try {
            val snippet = snippetRepository.findSnippetEntityById(snippetId)
            if (snippet != null) {
                val permUrl = "$BASE_URL$host:$permissionPort/$API_URL/update"
                val assetUrl = "$BASE_URL$host:..." //TODO
                val res = restTemplate.postForEntity(
                    permUrl,
                    PermissionRequest(snippetId, jwt),
                    PermissionResponse::class.java
                )
                if (res.body != null) {
                    println(res.body!!.permissions)
                } else {
                    ResponseEntity.status(400).body("Failed to create permissions")
                }
                ResponseEntity.ok(snippet)
            } else {
                ResponseEntity.status(404).body(null)
            }
        } catch (e: Exception) {
            println(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    fun deleteSnippet(snippetId: String){
        return snippetRepository.deleteById(snippetId)
    }

    fun findSnippetById(id: String): SnippetEntity {
        return snippetRepository.findSnippetEntityById(id)
    }

    private fun translateToEntity(snippetDTO: SnippetDTO): SnippetEntity{
        return SnippetEntity(snippetDTO.id ?: UUID.randomUUID().toString(), snippetDTO.title, snippetDTO.language)
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO{
        return SnippetDTO(snippetEntity.id, snippetEntity.title, snippetEntity.language)
    }
}