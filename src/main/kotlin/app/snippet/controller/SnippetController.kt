package com.example.springboot.app.snippet.controller

import com.example.springboot.app.external.services.asset.AssetService
import com.example.springboot.app.snippet.controller.ControllerUtils.generateFile
import com.example.springboot.app.snippet.controller.ControllerUtils.generateHeaders
import com.example.springboot.app.snippet.controller.ControllerUtils.generateSnippetDTO
import com.example.springboot.app.snippet.dto.UpdateSnippetDTO
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.snippet.repository.entity.SnippetEntity
import com.example.springboot.app.snippet.service.SnippetService
import com.example.springboot.app.external.request.ShareRequest
import com.example.springboot.app.external.request.SnippetRequestCreate
import com.example.springboot.app.external.response.SnippetResponse
import com.example.springboot.app.external.ui.SnippetData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api")
class SnippetController @Autowired constructor(
    private val snippetService: SnippetService,
    private val printScriptService: PrintScriptService,
    private val permissionService: PermissionService,
    private val assetService: AssetService,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    @PostMapping("/create")
    suspend fun create(
        @RequestBody snippetRequestCreate: SnippetRequestCreate,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetResponse> {
        return try {
            val snippetDTO = generateSnippetDTO(snippetRequestCreate)
            val headers = generateHeaders(jwt)
            val snippetFile = generateFile(snippetRequestCreate)
            logger.info("saving snippet file: $snippetFile")
            assetService.saveSnippet(snippetDTO.snippetId, snippetFile)
            logger.info("saved snippet file!")

            val validation = printScriptService.validateSnippet(snippetDTO.snippetId, snippetDTO.version, headers)
            if (validation.statusCode.is4xxClientError) {
                return ResponseEntity.status(400).body(SnippetResponse(null, validation.body?.error))
            } else if (validation.statusCode.is5xxServerError) {
                throw Exception("Failed to validate snippet in service")
            }
            permissionService.createPermission(snippetDTO.snippetId, headers)

            ResponseEntity.ok().body(SnippetResponse(snippetService.createSnippet(snippetDTO), null))
        } catch (e: Exception) {
            logger.error(" The error is: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PutMapping("/update/{snippetId}")
    fun update(
        @PathVariable snippetId: String,
        @RequestBody updateSnippetDTO: UpdateSnippetDTO,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetData> {
        return try {
            val hasPermission = permissionService.hasPermission("WRITE", updateSnippetDTO.title , generateHeaders(jwt))
            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to write snippet"))
            }
            //val updatedSnippet = assetService.saveSnippet(snippetId, updateSnippetDTO.code)
//            if (updatedSnippet.statusCode.is5xxServerError) {
//                throw Exception("Failed to update snippet in asset service")
//            }
            val headers = generateHeaders(jwt)
            val compliance = printScriptService.validateSnippet(snippetId, "1.1", headers).body?.message ?: "not-compliant"
            val snippet = snippetService.findSnippetById(snippetId)
            val snippetData = SnippetData(
                snippetId,
                updateSnippetDTO.title,
                updateSnippetDTO.code,
                snippet.extension,
                compliance,
                jwt.claims["name"].toString()
            )
            ResponseEntity.ok(snippetData)
        } catch (e: Exception) {
            logger.error("Error updating snippet: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/share")
    suspend fun share(
        @RequestBody shareRequest: ShareRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetData> {
        // this sends the userId to the Perm service, check if the user can share and if so
        // the Perm adds the user to the snippet permissions
        return try {
            val hasPermission = permissionService.hasPermissionBySnippetId("SHARE", shareRequest.snippetId, generateHeaders(jwt))
            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to share snippet"))
            }
            permissionService.shareSnippet(shareRequest.snippetId, shareRequest.userId, generateHeaders(jwt))
            val snippet = snippetService.findSnippetById(shareRequest.snippetId)
            val snippetCode = assetService.getSnippet(shareRequest.snippetId).body!!
            val compliance = printScriptService.validateSnippet(shareRequest.snippetId, snippet.version, generateHeaders(jwt)).body?.message ?: "not-compliant"
            val snippetData = SnippetData(
                shareRequest.snippetId,
                snippet.title,
                String(snippetCode.bytes),
                snippet.extension,
                compliance,
                jwt.claims["name"].toString()
            )
            ResponseEntity.ok(snippetData)
        } catch (e: Exception) {
            logger.error("Error sharing snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }


    @DeleteMapping("/delete/{snippetId}")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable snippetId: String
    ): ResponseEntity<Void> {
        // this sends the userId to the Perm service, check if the user can delete and if so
        // the Perm deletes the snippet from its db, the asset deletes de file
        // and the SnippetS deletes the snippet from its db
        return try {
            val hasPermission = permissionService.hasPermissionBySnippetId("WRITE", snippetId, generateHeaders(jwt))
            if (!hasPermission) {
                return ResponseEntity.status(400).build()
            }
            snippetService.deleteSnippet(snippetId)
            permissionService.deleteFromPermission(snippetId, generateHeaders(jwt))
            assetService.deleteSnippet(snippetId)
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

    @GetMapping("/get/{snippetId}")
    suspend fun getSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetData> {
        // val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
        // check if the user can read the snippet
        // should send the ids to the asset and get all snippets

        val headers = generateHeaders(jwt)
        val hasPermission = permissionService.hasPermissionBySnippetId("READ", snippetId, headers)
        if (hasPermission) {
            val snippet = snippetService.findSnippetById(snippetId)
            val code = assetService.getSnippet(snippetId)
            val author = jwt.claims["name"].toString()
            val compliance = printScriptService.validateSnippet(snippetId, snippet.version, headers).body?.message ?: "not-compliant"
//            val snippetData = SnippetData(
//                snippet.snippetId,
//                snippet.title,
//                code.body!!,
//                snippet.extension,
//                compliance,
//                author,
//            )
//            return ResponseEntity.ok(snippetData)
            TODO()
        } else {
            return ResponseEntity.status(400).body(null)
        }
    }   
}
