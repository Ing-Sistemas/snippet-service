package com.example.springboot.app.controller

import com.example.springboot.app.auth.OAuth2ResourceServerSecurityConfiguration
import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.dto.UpdateSnippetDTO
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.service.SnippetService
import com.example.springboot.app.utils.PSRequest
import com.example.springboot.app.utils.URLs.API_URL
import com.example.springboot.app.utils.URLs.BASE_URL
import com.example.springboot.app.utils.rest.request.PermissionRequest
import com.example.springboot.app.utils.rest.request.SnippetRequestCreate
import com.example.springboot.app.utils.rest.response.PSValResponse
import com.example.springboot.app.utils.rest.response.PermissionResponse
import com.example.springboot.app.utils.rest.response.SnippetResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.util.*

@RestController
@RequestMapping("/api")
class SnippetController(
    private val snippetService: SnippetService,
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)
    private val host = System.getenv().getOrDefault("HOST", "localhost")
    private val permissionPort = System.getenv().getOrDefault("PERMISSION_SERVICE_PORT", "none")
    private val psPort = System.getenv().getOrDefault("PRINT_SCRIPT_SERVICE_PORT", "none")
    // TODO create the snippet file bucket (the asset receives the title as key
    // TODO better to create it with the snippet_id as key

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Snippet service is up and running")
    }

    @PostMapping("/create")
    fun create(
        @RequestBody snippetRequestCreate: SnippetRequestCreate,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetResponse> {
        return try {
            val snippetDTO = generateSnippetDTO(snippetRequestCreate)
            val headers = generateHeaders(jwt)

            val validation  = validateSnippet(snippetDTO.id, snippetDTO.version ,headers)
           if (validation.statusCode.is4xxClientError) {
                return ResponseEntity.status(400).body(SnippetResponse(null, validation.body?.error))
            } else if (validation.statusCode.is5xxServerError) {
                throw Exception("Failed to validate snippet in service")
            }

            createPermissions(snippetDTO.id, headers)
            ResponseEntity.ok().body(snippetService.createSnippet(snippetDTO))
            return  ResponseEntity.ok().body(SnippetResponse(snippetService.createSnippet(snippetDTO), null))
        } catch (e: Exception) {
            logger.error(" The error is: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PutMapping("/update")
    fun update(
        @RequestBody updateSnippetDTO: UpdateSnippetDTO,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetEntity> {
        return try {
            val userId = getUserIdFromJWT(jwt)
            val snippetTitle = updateSnippetDTO.title
            val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
            val headers = generateHeaders(jwt)
            val requestEntity = HttpEntity(PermissionRequest(snippetTitle), headers)

            val resPermission = restTemplate.postForEntity(
                permURL,
                requestEntity,
                PermissionResponse::class.java
            )

            if (resPermission.body != null) {
                if (resPermission.body!!.permissions.contains("WRITE")) {
                    return ResponseEntity.ok(snippetService.updateSnippet(updateSnippetDTO, jwt))
                } else {
                    throw Exception("User does not have permission to update snippet")
                }
            } else {
                throw Exception("Failed to get permissions")
            }
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
        TODO()
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

    private fun validateSnippet(snippetId: String, version: String, headers: HttpHeaders): ResponseEntity<PSValResponse> {
        val psUrl = "$BASE_URL$host:$psPort/$API_URL/validate"
        val requestPSEntity = HttpEntity(PSRequest(version, snippetId), headers)
        println("before")
        val resPrintsript = restTemplate.postForEntity(psUrl, requestPSEntity, PSValResponse::class.java)
        println(resPrintsript.statusCode.is4xxClientError)
        println(resPrintsript.statusCode.is5xxServerError)
        return when {
            resPrintsript.statusCode.is4xxClientError -> ResponseEntity.status(400).body(resPrintsript.body)
            resPrintsript.statusCode.is5xxServerError -> throw Exception("Failed to validate snippet in service")
            else -> resPrintsript
        }
    }

    private fun createPermissions(snippetId: String, headers: HttpHeaders) {
        val permUrl = "$BASE_URL$host:$permissionPort/$API_URL/create"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val resPermission = restTemplate.postForEntity(permUrl, requestPermEntity, PermissionResponse::class.java)

        if (resPermission.body == null) {
            throw Exception("Failed to create permissions")
        }
    }

    private fun generateSnippetDTO(snippetRequestCreate: SnippetRequestCreate): SnippetDTO {
        return SnippetDTO(
            UUID.randomUUID().toString(),
            snippetRequestCreate.version ,
            snippetRequestCreate.title,
            snippetRequestCreate.language
        )
    }

    private fun getUserIdFromJWT(jwt: Jwt): String {
        val auth = OAuth2ResourceServerSecurityConfiguration(
            System.getenv("AUTH0_AUDIENCE"),
            System.getenv("AUTH_SERVER_URI")
        ).jwtDecoder()
        return auth.decode(jwt.tokenValue).subject!!
    }

    private fun generateHeaders(jwt: Jwt): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Bearer ${jwt.tokenValue}")
            contentType = MediaType.APPLICATION_JSON
        }
    }
}