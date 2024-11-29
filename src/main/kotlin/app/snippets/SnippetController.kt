package com.example.springboot.app.snippets

import com.example.springboot.app.external.services.asset.AssetService
import com.example.springboot.app.snippets.ControllerUtils.generateFile
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.generateSnippetDTO
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.external.services.permission.request.ShareRequest
import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.external.services.printscript.response.SnippetResponse
import com.example.springboot.app.snippets.dto.SnippetDTO
import com.example.springboot.app.snippets.dto.SnippetDataUi
import com.example.springboot.app.snippets.dto.UpdateSnippetDTO
import com.example.springboot.app.utils.PaginatedSnippets
import com.example.springboot.app.utils.PaginatedUsers
import com.example.springboot.app.utils.Pagination
import com.example.springboot.app.utils.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
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
    ): ResponseEntity<SnippetDataUi> {
        return try {
            val hasPermission = permissionService.hasPermissionByTitle("WRITE", updateSnippetDTO.title , generateHeaders(jwt))
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
            val snippetDataUi = SnippetDataUi(
                snippetId,
                updateSnippetDTO.title,
                updateSnippetDTO.code,
                "prinscript",
                snippet.extension,
                compliance,
                author = jwt.claims["email"].toString()
            )

            ResponseEntity.ok(snippetDataUi)
        } catch (e: Exception) {
            logger.error("Error updating snippet: {}", e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/share")
    suspend fun share(
        @RequestBody shareRequest: ShareRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetDataUi> {
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
            val snippetDataUi = SnippetDataUi(
                shareRequest.snippetId,
                snippet.title,
                String(snippetCode.bytes),
                "prinscript",
                snippet.extension,
                compliance,
                jwt.claims["email"].toString()
            )
            ResponseEntity.ok(snippetDataUi)
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

    @GetMapping("/get/{snippetId}")
    suspend fun getSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetDataUi> {
        val headers = generateHeaders(jwt)
        val hasPermission = permissionService.hasPermissionBySnippetId("READ", snippetId, headers)
        if (hasPermission) {
            val snippet = snippetService.findSnippetById(snippetId)
            val code = String(assetService.getSnippet(snippetId).body!!.bytes)
            val author = jwt.claims["email"].toString()
            val compliance = printScriptService.validateSnippet(snippetId, snippet.version, headers).body?.message ?: "not-compliant"
            // TODO()
            val snippetDataUi = SnippetDataUi(
                snippet.snippetId,
                snippet.title,
                code,
                snippet.language,
                snippet.extension,
                compliance,
                author,
            )
            return ResponseEntity.ok(snippetDataUi)
        } else {
            return ResponseEntity.status(400).body(null)
        }
    }

    @GetMapping("/get_users")
    fun getUserFriends(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) name: String,
        @RequestParam(required = false) page: Int,
        @RequestParam(required = false) pageSize: Int,
    ): ResponseEntity<PaginatedUsers> {
        return try {
            // TODO
            val userFriends = emptyList<User>()
            val pag = Pagination(page, pageSize, pageSize)
            val res = PaginatedUsers(pag, userFriends)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            logger.error("Error getting user friends: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    @GetMapping("/get_all")
    fun getSnippets(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) pageSize: Int,
        @RequestParam(required = false) snippetName: String?
    ): ResponseEntity<PaginatedSnippets> {
        return try {
            val headers = generateHeaders(jwt)
            val snippetIds = permissionService.getAllSnippetsIdsForUser(headers)

            if (snippetIds.isEmpty()) {
                return ResponseEntity.ok(PaginatedSnippets(Pagination(page, pageSize, 0), emptyList()))
            }

            val snippets = if (snippetName != null) {
                snippetIds
                    .map { snippetService.findSnippetById(it) }
                    .filter { it.title.contains(snippetName, ignoreCase = true) }
            } else {
                snippetIds.map { snippetService.findSnippetById(it) }
            }
            if (snippets.isEmpty()) {
                return ResponseEntity.ok(PaginatedSnippets(Pagination(page, pageSize, 0), emptyList()))
            }

            val resSnippets = snippets.map { convertSnippetDtoToSnippetData(it, headers) }
            if (resSnippets.isEmpty()) {
                return ResponseEntity.ok(PaginatedSnippets(Pagination(page, pageSize, 0), emptyList()))
            }

            val totalCount = snippets.size
            val paginatedSnippets = PaginatedSnippets(
                pagination = Pagination(page, pageSize, totalCount),
                snippets = resSnippets
            )
            ResponseEntity.ok(paginatedSnippets)
        } catch (e: Exception) {
            logger.error("Error getting snippets: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    // TODO agregar el upload

    private fun convertSnippetDtoToSnippetData(snippetDto: SnippetDTO, headers: HttpHeaders): SnippetDataUi {

        val content = assetService.getSnippet(snippetDto.snippetId)
        val compliance = printScriptService.validateSnippet(snippetDto.snippetId, snippetDto.version, headers).body?.message ?: "not-compliant"
        val author = if (permissionService.hasPermissionBySnippetId("WRITE", snippetDto.snippetId, headers)) {
            "you"
        } else {
            "other"
        }
        return SnippetDataUi(
            snippetId = snippetDto.snippetId,
            name = snippetDto.title,
            content = String(content.body!!.bytes),
            language = snippetDto.language,
            extension = snippetDto.extension,
            compliance = compliance,
            author = author
        )
    }
}
