package com.example.springboot.app.snippets

import com.example.springboot.app.external.services.asset.AssetService
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.permission.request.ShareRequest
import com.example.springboot.app.external.services.printscript.LanguageService
import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.external.services.printscript.response.SnippetResponse
import com.example.springboot.app.rules.repository.RuleUserRepository
import com.example.springboot.app.snippets.ControllerUtils.generateFile
import com.example.springboot.app.snippets.ControllerUtils.generateFileFromData
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.generateSnippetDTO
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.snippets.dto.SnippetDTO
import com.example.springboot.app.snippets.dto.SnippetDataUi
import com.example.springboot.app.snippets.dto.UpdateSnippetDTO
import com.example.springboot.app.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api")
class SnippetController
    @Autowired
    constructor(
        private val snippetService: SnippetService,
        private val permissionService: PermissionService,
        private val assetService: AssetService,
        private val userRuleRepository: RuleUserRepository,
        private val languageService: Map<CodingLanguage, LanguageService>,
        private val correlationService: CorrelationService,
    ) {
        private val logger = LoggerFactory.getLogger(SnippetController::class.java)

        @PostMapping("/create")
        suspend fun create(
            @RequestBody snippetRequestCreate: SnippetRequestCreate,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetResponse> {
            return try {
                logger.trace("Creating snippet with name: ${snippetRequestCreate.title}")
                val cId = UUID.randomUUID().toString()
                correlationService.correlatePermission(cId, generateHeaders(jwt))
                correlationService.correlatePs(cId, generateHeaders(jwt))
                logger.info("Creating snippet with name: ${snippetRequestCreate.title}; $cId")
                val snippetDTO = generateSnippetDTO(snippetRequestCreate)
                val headers = generateHeaders(jwt)
                val snippetFile = generateFile(snippetRequestCreate)
                assetService.saveSnippet(snippetDTO.id, snippetFile)
                val lanService = languageService[CodingLanguage.valueOf(snippetDTO.language.uppercase(Locale.getDefault()))]!!
                val validation = lanService.validateSnippet(snippetDTO.id, snippetDTO.version, headers)
                if (validation.statusCode.is4xxClientError) {
                    return ResponseEntity.status(400).body(SnippetResponse(null, validation.body?.error))
                } else if (validation.statusCode.is5xxServerError) {
                    throw Exception("Failed to validate snippet in service")
                }
                permissionService.createPermission(snippetDTO.id, headers)

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
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDataUi> {
            return try {
                logger.trace("Updating snippet with id: $snippetId")
                val cId = UUID.randomUUID().toString()
                correlationService.correlatePermission(cId, generateHeaders(jwt))
                correlationService.correlatePs(cId, generateHeaders(jwt))
                logger.info("Updating snippet with id: $snippetId; $cId")
                val hasPermission = permissionService.hasPermissionBySnippetId("WRITE", snippetId, generateHeaders(jwt))
                if (!hasPermission) {
                    ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to write snippet"))
                }
                val code = updateSnippetDTO.content
                val snippet = snippetService.findSnippetById(snippetId)
                val toUpdateFile = generateFileFromData(snippet, code)
                assetService.deleteSnippet(snippetId)
                val updatedSnippet = assetService.saveSnippet(snippetId, toUpdateFile)
                if (updatedSnippet.statusCode.is5xxServerError) {
                    throw Exception("Failed to update snippet in asset service")
                }
//            val headers = generateHeaders(jwt)
//            val lintStatus = printScriptService.validateSnippet(snippetId, "1.1", headers).body?.message ?: "not-compliant"

                val snippetDataUi =
                    SnippetDataUi(
                        snippetId,
                        snippet.title,
                        code,
                        snippet.language,
                        snippet.extension,
                        "pending",
                        author = jwt.claims["email"].toString(),
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
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDataUi> {
            return try {
                logger.trace("Sharing snippet with id: ${shareRequest.snippetId}")
                val cId = UUID.randomUUID().toString()
                correlationService.correlatePermission(cId, generateHeaders(jwt))
                correlationService.correlatePs(cId, generateHeaders(jwt))
                logger.info("Sharing snippet with id: ${shareRequest.snippetId}; $cId")
                val hasPermission = permissionService.hasPermissionBySnippetId("SHARE", shareRequest.snippetId, generateHeaders(jwt))
                if (!hasPermission) {
                    ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to share snippet"))
                }
                if (shareRequest.userId == getUserIdFromJWT(jwt)) {
                    ResponseEntity.status(400).body(SnippetResponse(null, "User cannot share snippet with themselves"))
                }
                permissionService.shareSnippet(shareRequest.snippetId, shareRequest.userId.removePrefix("auth0|"), generateHeaders(jwt))
                val snippet = snippetService.findSnippetById(shareRequest.snippetId)
                val snippetCode = assetService.getSnippet(shareRequest.snippetId).body!!
                val snippetDataUi =
                    SnippetDataUi(
                        shareRequest.snippetId,
                        snippet.title,
                        String(snippetCode.bytes),
                        snippet.language,
                        snippet.extension,
                        snippet.status.name.lowercase(Locale.getDefault()),
                        jwt.claims["email"].toString(),
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
            @PathVariable snippetId: String,
        ): ResponseEntity<Void> {
            return try {
                logger.trace("Deleting snippet with id: $snippetId")
                val cId = UUID.randomUUID().toString()
                correlationService.correlatePermission(cId, generateHeaders(jwt))
                correlationService.correlatePs(cId, generateHeaders(jwt))
                logger.info("Deleting snippet with id: $snippetId; $cId")
                val hasPermission = permissionService.hasPermissionBySnippetId("WRITE", snippetId, generateHeaders(jwt))
                if (!hasPermission) {
                    return ResponseEntity.status(403).build()
                }
                snippetService.deleteSnippet(snippetId)
                permissionService.deleteFromPermission(snippetId, generateHeaders(jwt))
//            if (!permRm) return ResponseEntity.status(405).build()

                assetService.deleteSnippet(snippetId)
                ResponseEntity.status(200).build()
            } catch (e: Exception) {
                logger.error("Error deleting snippet: {}", e.message)
                ResponseEntity.status(500).build()
            }
        }

        @GetMapping("/get/{snippetId}")
        suspend fun getSnippet(
            @PathVariable snippetId: String,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDataUi> {
            logger.trace("Getting snippet with id: $snippetId")
            val cId = UUID.randomUUID().toString()
            correlationService.correlatePermission(cId, generateHeaders(jwt))
            correlationService.correlatePs(cId, generateHeaders(jwt))
            logger.info("Getting snippet with id: $snippetId; $cId")
            val headers = generateHeaders(jwt)
            val hasPermission = permissionService.hasPermissionBySnippetId("READ", snippetId, headers)
            if (hasPermission) {
                val snippet = snippetService.findSnippetById(snippetId)
                val code = String(assetService.getSnippet(snippetId).body!!.bytes)
                val author = jwt.claims["email"].toString()
                val snippetDataUi =
                    SnippetDataUi(
                        snippet.id,
                        snippet.title,
                        code,
                        snippet.language,
                        snippet.extension,
                        snippet.status.name.lowercase(Locale.getDefault()),
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
            @RequestParam(required = false, defaultValue = "") name: String,
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        ): ResponseEntity<PaginatedUsers> {
            return try {
                logger.trace("Getting all users")
                val cId = UUID.randomUUID().toString()
                correlationService.correlatePermission(cId, generateHeaders(jwt))
                correlationService.correlatePs(cId, generateHeaders(jwt))
                logger.info("Getting all users; $cId")
                val users = snippetService.getAllUsers(page, pageSize, name, jwt)
                ResponseEntity.ok(users)
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
            @RequestParam(required = false) snippetName: String?,
        ): ResponseEntity<PaginatedSnippets> {
            return try {
                logger.trace("Getting all snippets for user")
                val cId = UUID.randomUUID().toString()
                correlationService.correlatePermission(cId, generateHeaders(jwt))
                correlationService.correlatePs(cId, generateHeaders(jwt))
                logger.info("Getting all snippets for user; $cId")
                val headers = generateHeaders(jwt)
                val snippetIds = permissionService.getAllSnippetsIdsForUser(headers)

                if (snippetIds.isEmpty()) {
                    return ResponseEntity.ok(PaginatedSnippets(Pagination(page, pageSize, 0), emptyList()))
                }

                val snippets =
                    if (snippetName != null) {
                        snippetIds
                            .map { snippetService.findSnippetById(it) }
                            .filter { it.title.contains(snippetName, ignoreCase = true) }
                    } else {
                        snippetIds.map { snippetService.findSnippetById(it) }
                    }

                val resSnippets = snippets.map { convertSnippetDtoToSnippetData(it, jwt) }

                val totalCount = snippets.size
                val paginatedSnippets =
                    PaginatedSnippets(
                        pagination = Pagination(page, pageSize, totalCount),
                        snippets = resSnippets,
                    )
                ResponseEntity.ok(paginatedSnippets)
            } catch (e: Exception) {
                logger.error("Error getting snippets: {}", e.message)
                ResponseEntity.status(500).build()
            }
        }

        @GetMapping("/test/error")
        fun testNewRelicError(): ResponseEntity<Void> {
            logger.error("This is an error for testing")
            return ResponseEntity.status(400).build()
        }

        private fun convertSnippetDtoToSnippetData(
            snippetDto: SnippetDTO,
            jwt: Jwt,
        ): SnippetDataUi {
            val content = assetService.getSnippet(snippetDto.id)

            val author =
                if (permissionService.hasPermissionBySnippetId("WRITE", snippetDto.id, generateHeaders(jwt))) {
                    "you"
                } else {
                    "other"
                }
            return SnippetDataUi(
                id = snippetDto.id,
                name = snippetDto.title,
                content = String(content.body!!.bytes),
                language = snippetDto.language,
                extension = snippetDto.extension,
                compliance = snippetDto.status.name.lowercase(Locale.getDefault()),
                author = author,
            )
        }
    }
