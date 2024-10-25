package com.example.springboot.app.controller

import com.example.springboot.app.dto.UpdateSnippetDTO
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.service.SnippetService
import com.example.springboot.app.utils.SnippetRequestCreate
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SnippetController(
    private val snippetService: SnippetService,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    @PostMapping("/create")
    fun create(
        @RequestBody snippetRequestCreate: SnippetRequestCreate,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetEntity> {
        return try {
            val savedSnippetResponse = snippetService.createAndSetPermissions(snippetRequestCreate, jwt)
            return savedSnippetResponse
        } catch (e: Exception) {
            logger.error(" The error is: {}", e.message)
            ResponseEntity.status(400).body(null)
        }
    }

    @PutMapping("/update")
    fun update(
        @RequestBody snippetId: String,
        @RequestBody code: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetEntity> {
        // this sends the userId to the Perm service, check if the user can w, and then send the update
        // to the asset service
        return try {
            val updateSnippetDTO = UpdateSnippetDTO(snippetId, code)
            val updatedSnippet = snippetService.updateSnippet(snippetId, updateSnippetDTO, jwt)
            updatedSnippet
        } catch (e: Exception) {
            logger.error("Error updating snippet: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @GetMapping("/get")
    fun get(
        @RequestBody userId: String,
        @RequestBody snippetId: String
    ): ResponseEntity<SnippetEntity> {
        //val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
        //check if the user can read the snippet
        return try {
            val snippet = snippetService.findSnippetById(snippetId)
            ResponseEntity.ok(snippet)
        } catch (e: Exception) {
            logger.error("Error getting snippet: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @DeleteMapping("/delete")
    fun delete(
        @RequestBody userId:String,
        @RequestBody snippetId: String
    ): ResponseEntity<Void> {
        // this sends the userId to the Perm service, check if the user can delete and if so
        // the Perm deletes the snippet from its db, the asset deletes de file
        // and the SnippetS deletes the snippet from its db
        return try {
            snippetService.deleteSnippet(snippetId)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error("Error deleting snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }
}