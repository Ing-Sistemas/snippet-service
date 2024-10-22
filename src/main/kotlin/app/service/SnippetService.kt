package com.example.springboot.app.service

import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.dto.UpdateSnippetDTO
import com.example.springboot.app.repository.SnippetRepository
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.utils.PermissionRequest
import com.example.springboot.app.utils.PermissionResponse
import com.example.springboot.app.utils.SnippetRequestCreate
import com.example.springboot.app.utils.URLs.API_URL
import com.example.springboot.app.utils.URLs.BASE_URL
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


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

            val res = restTemplate.postForEntity(
                permUrl,
                PermissionRequest(savedSnippet.id!!, jwt),
                PermissionResponse::class.java
            )
            if (res.body != null) {
                println(res.body!!.permissions)
            } else {
                ResponseEntity.status(400).body("Failed to create permissions")
            }
            // TODO create the snippet file bucket (the asset recives the title as key
            // TODO better to create it with the snippet_id
            ResponseEntity.ok(savedSnippet)
        } catch (e: Exception) {
            println(e.message)
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
        return SnippetEntity(null, snippetDTO.title, snippetDTO.language)
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO{
        return SnippetDTO(snippetEntity.id, snippetEntity.title, snippetEntity.language)
    }
}