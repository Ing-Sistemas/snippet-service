package com.example.springboot.app.controller

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.auth.OAuth2ResourceServerSecurityConfiguration
import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.dto.UpdateSnippetDTO
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.service.SnippetService
import com.example.springboot.app.utils.PSRequest
import com.example.springboot.app.utils.rest.request.PermissionRequest
import com.example.springboot.app.utils.rest.request.PermissionShare
import com.example.springboot.app.utils.rest.request.ShareRequest
import com.example.springboot.app.utils.rest.request.SnippetRequestCreate
import com.example.springboot.app.utils.rest.response.PSValResponse
import com.example.springboot.app.utils.rest.response.PermissionResponse
import com.example.springboot.app.utils.rest.response.SnippetResponse
import com.example.springboot.app.utils.rest.ui.SnippetData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
    private val assetService = AssetService(restTemplate, bucketUrl)

    @Value("\${asset_url")
    private lateinit var bucketUrl: String
    @Value("\${permission_url}")
    private lateinit var permUrl: String
    @Value("\${print_script_url}")
    private lateinit var psUrl: String

    @PostMapping("/create")
    fun create(
        @RequestBody snippetRequestCreate: SnippetRequestCreate,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetResponse> {
        return try {
            val snippetDTO = generateSnippetDTO(snippetRequestCreate)
            val headers = generateHeaders(jwt)

            val validation  = validateSnippet(snippetDTO.snippetId, snippetDTO.version, headers)
            if (validation.statusCode.is4xxClientError) {
                return ResponseEntity.status(400).body(SnippetResponse(null, validation.body?.error))
            } else if (validation.statusCode.is5xxServerError) {
                throw Exception("Failed to validate snippet in service")
            }

            createPermissions(snippetDTO.snippetId, headers)
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
    ): ResponseEntity<SnippetResponse> {
        return try {
            if(!hasPermission("WRITE", updateSnippetDTO.title , generateHeaders(jwt))) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to write snippet"))
            }
            val temp = SnippetEntity("a","a","a", "a")//snippetService.updateSnippet(updateSnippetDTO) not necessary to handle update in service since the values of the snippet are not updated
            // because the snippet file is handled by the asset service, the update is sent there

            ResponseEntity.ok(SnippetResponse(temp, null))
        } catch (e: Exception) {
            logger.error("Error updating snippet: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/share")
    fun share(
        @RequestBody shareRequest: ShareRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetResponse> {
        // this sends the userId to the Perm service, check if the user can share and if so
        // the Perm adds the user to the snippet permissions
        return try {

            if(!hasPermission("SHARE", shareRequest.title, generateHeaders(jwt))) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to share snippet"))
            }

            val response = shareSnippet(shareRequest.title, shareRequest.friendId, generateHeaders(jwt))
            if (response.body == null) {
                throw Exception("Failed to share snippet")
            }
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error("Error sharing snippet: {}", e.message)
            ResponseEntity.status(500).build()
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
    //todo difference between get a snippet, get all my snippets and get all snippets i have access to

    // gets all snippets from the user
    @GetMapping("/get")
    fun getSnippets(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetEntity> {
        //val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
        //check if the user can read the snippet
        // should send the ids to the asset and get all snippets
        TODO()
    }

    // gets a snippet by title
    @GetMapping("/get/{title}")
    fun getSnippet(
        @PathVariable title: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetData> {
        // val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
        // check if the user can read the snippet
        // should send the ids to the asset and get all snippets

        val headers = generateHeaders(jwt)
        val hasPermission = hasPermission("READ", title, headers)
        if (hasPermission) {
            val snippet = snippetService.findSnippetByTitle(title)
            val code = assetService.getSnippet(snippet.snippetId)
            val snippetData = SnippetData(snippet.snippetId, snippet.title, snippet.language, snippet.version, code.body!!)
            return ResponseEntity.ok(snippetData)
        } else {
            return ResponseEntity.status(400).body(null)
        }
    }

    private fun shareSnippet(snippetTitle : String, friendId: String, headers: HttpHeaders): ResponseEntity<PermissionResponse> {
        val url = "$permUrl/share"
        val shareRequest = PermissionShare(snippetService.findSnippetByTitle(snippetTitle).snippetId, friendId)
        val response = restTemplate.postForEntity(url, shareRequest, PermissionResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to share snippet")
        }
        return response
    }

    private fun validateSnippet(
        snippetId: String, version: String, headers: HttpHeaders
    ): ResponseEntity<PSValResponse> {
        val url = "$psUrl/validate"
        val requestPSEntity = HttpEntity(PSRequest(version, snippetId), headers)
        println("before")
        val resPrintScript = restTemplate.postForEntity(url, requestPSEntity, PSValResponse::class.java)
        println(resPrintScript.statusCode.is4xxClientError)
        println(resPrintScript.statusCode.is5xxServerError)
        return when {
            resPrintScript.statusCode.is4xxClientError -> ResponseEntity.status(400).body(resPrintScript.body)
            resPrintScript.statusCode.is5xxServerError -> throw Exception("Failed to validate snippet in service")
            else -> resPrintScript
        }
    }

    private fun hasPermission(
        permission: String,
        snippetTitle: String,
        headers: HttpHeaders
    ): Boolean {
        val url = "$permUrl/get"
        val snippetId = snippetService.findSnippetByTitle(snippetTitle).snippetId
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val response = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
        return response.body!!.permissions.contains(permission)
    }

    private fun createPermissions(snippetId: String, headers: HttpHeaders) {
        val url = "$permUrl/create"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val resPermission = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
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