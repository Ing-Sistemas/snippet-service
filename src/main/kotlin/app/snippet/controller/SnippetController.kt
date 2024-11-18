package com.example.springboot.app.snippet.controller

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.snippet.controller.ControllerUtils.generateHeaders
import com.example.springboot.app.snippet.controller.ControllerUtils.generateSnippetDTO
import com.example.springboot.app.snippet.model.dto.UpdateSnippetDTO
import com.example.springboot.app.external.rest.ExternalService
import com.example.springboot.app.snippet.model.entity.SnippetEntity
import com.example.springboot.app.snippet.service.SnippetService
import com.example.springboot.app.external.rest.request.ShareRequest
import com.example.springboot.app.external.rest.request.SnippetRequestCreate
import com.example.springboot.app.external.rest.response.SnippetResponse
import com.example.springboot.app.external.rest.ui.SnippetData
import com.example.springboot.app.utils.Rule
import com.example.springboot.app.redis.consumer.FormatEventConsumer
import com.example.springboot.app.redis.consumer.LintEventConsumer
import com.example.springboot.app.redis.events.FormatEvent
import com.example.springboot.app.redis.events.LintEvent
import com.example.springboot.app.redis.producer.FormatEventProd
import com.example.springboot.app.redis.producer.LintEventProducer
import com.example.springboot.app.utils.FormatRule
import com.example.springboot.app.utils.LintRule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api")
class SnippetController @Autowired constructor(
    private val snippetService: SnippetService,
    private val restTemplate: RestTemplate, //no se usa
    private val externalService: ExternalService,
    private val formatEventProducer: FormatEventProd,
    private val formatEventConsumer: FormatEventConsumer,
    private val lintEventProducer: LintEventProducer,
    private val lintEventConsumer: LintEventConsumer,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)
    @Value("\${asset_url}")
    private var bucketUrl: String = System.getenv("ASSET_URL")
    private val assetService = AssetService(restTemplate, bucketUrl)


    @PostMapping("/create")
    fun create(
        @RequestBody snippetRequestCreate: SnippetRequestCreate,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetResponse> {
        return try {
            val snippetDTO = generateSnippetDTO(snippetRequestCreate)
            val headers = generateHeaders(jwt)
            val validation = externalService.validateSnippet(snippetDTO.snippetId, snippetDTO.version, headers)
            if (validation.statusCode.is4xxClientError) {
                return ResponseEntity.status(400).body(SnippetResponse(null, validation.body?.error))
            } else if (validation.statusCode.is5xxServerError) {
                throw Exception("Failed to validate snippet in service")
            }

            externalService.createPermissions(snippetDTO.snippetId, headers)
            assetService.saveSnippet(snippetDTO.snippetId, snippetRequestCreate.code)
            ResponseEntity.ok().body(snippetService.createSnippet(snippetDTO))
            return  ResponseEntity.ok().body(SnippetResponse(snippetService.createSnippet(snippetDTO), null))
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
            val hasPermission = externalService.hasPermission("WRITE", updateSnippetDTO.title , generateHeaders(jwt))
            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to write snippet"))
            }
            val updatedSnippet = assetService.saveSnippet(snippetId, updateSnippetDTO.code)
            if (updatedSnippet.statusCode.is5xxServerError) {
                throw Exception("Failed to update snippet in asset service")
            }
            val headers = generateHeaders(jwt)
            val compliance = externalService.validateSnippet(snippetId, "1.1", headers).body?.message ?: "not-compliant"
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
    fun share(
        @RequestBody shareRequest: ShareRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetData> {
        // this sends the userId to the Perm service, check if the user can share and if so
        // the Perm adds the user to the snippet permissions
        return try {
            val hasPermission = externalService.hasPermissionBySnippetId("SHARE", shareRequest.snippetId, generateHeaders(jwt))
            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to share snippet"))
            }
            val response = externalService.shareSnippet(shareRequest.snippetId, shareRequest.userId, generateHeaders(jwt))
            if (response.body == null) {
                throw Exception("Failed to share snippet")
            }
            val snippet = snippetService.findSnippetById(shareRequest.snippetId)
            val snippetCode = assetService.getSnippet(shareRequest.snippetId)
            val compliance = externalService.validateSnippet(shareRequest.snippetId, snippet.version, generateHeaders(jwt)).body?.message ?: "not-compliant"
            val snippetData = SnippetData(
                shareRequest.snippetId,
                snippet.title,
                snippetCode.body!!,
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
            val hasPermission = externalService.hasPermissionBySnippetId("WRITE", snippetId, generateHeaders(jwt))
            if (!hasPermission) {
                return ResponseEntity.status(400).build()
            }
            snippetService.deleteSnippet(snippetId)
            externalService.deleteFromPermission(snippetId, generateHeaders(jwt))
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
    fun getSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<SnippetData> {
        // val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
        // check if the user can read the snippet
        // should send the ids to the asset and get all snippets

        val headers = generateHeaders(jwt)
        val hasPermission = externalService.hasPermissionBySnippetId("READ", snippetId, headers)
        if (hasPermission) {
            val snippet = snippetService.findSnippetById(snippetId)
            val code = assetService.getSnippet(snippetId)
            val author = jwt.claims["name"].toString()
            val compliance = externalService.validateSnippet(snippetId, snippet.version, headers).body?.message ?: "not-compliant"
            val snippetData = SnippetData(
                snippet.snippetId,
                snippet.title,
                code.body!!,
                snippet.extension,
                compliance,
                author,
            )
            return ResponseEntity.ok(snippetData)
        } else {
            return ResponseEntity.status(400).body(null)
        }
    }

    @GetMapping("/lint/rules")
    fun getLintRules(): ResponseEntity<List<Rule>> {
        val rules = listOf(
            Rule(id = "1", name = "identifierFormat", isActive = false, value = "camel case"),
            Rule(id = "2", name = "mandatory-variable-or-literal-in-println", isActive = false, value = false),
            Rule(id = "3", name = "mandatory-variable-or-literal-in-readInput", isActive = false, value = false)
        )
        return ResponseEntity.ok(rules)
    }

    @GetMapping("/format/rules")
    fun getFormatRules(): ResponseEntity<List<Rule>> {
        val rules = listOf(
            Rule(id = "4", name = "spaceAfterColon", isActive = false, value = false),
            Rule(id = "5", name = "spaceAroundEquals", isActive = false, value = false),
            Rule(id = "6", name = "lineJumpBeforePrintln", isActive = false, value = 0),
            Rule(id = "7", name = "lineJumpAfterSemicolon", isActive = false, value = true),
            Rule(id = "8", name = "singleSpaceBetweenTokens", isActive = false, value = true),
            Rule(id = "9", name = "spaceAroundOperators", isActive = false, value = true)
        )
        return ResponseEntity.ok(rules)
    }
    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody snippet: SnippetRequestCreate,//TODO change request body class
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val snippetId = snippetService.findSnippetByTitle(snippet.title).snippetId
        return try {
            val hasPermission = externalService.hasPermission("WRITE", snippetId, generateHeaders(jwt))

            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to format snippet"))
            }
            val response = externalService.format(snippetId, generateHeaders(jwt))

            if (response.body == null) {
                throw Exception("Failed to format snippet")
            }
            ResponseEntity.ok().body(response.body!!.status)
        } catch (e: Exception) {
            logger.error("Error formatting snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    @PostMapping("/lint")
    fun lintSnippet(
        @RequestBody snippet: SnippetRequestCreate,//TODO change request body class
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val snippetId = snippetService.findSnippetByTitle(snippet.title).snippetId
        return try {
            val hasPermission = externalService.hasPermission("WRITE", snippetId, generateHeaders(jwt))

            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to lint snippet"))
            }
            val response = externalService.lint(snippetId, generateHeaders(jwt))

            if (response.body == null) {
                throw Exception("Failed to lint snippet")
            }
            ResponseEntity.ok().body(response.body!!.status)
        } catch (e: Exception) {
            logger.error("Error linting snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }


    @PostMapping("/format_all")
    suspend fun formatAllSnippets(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val snippetIds = externalService.getAllSnippetsIdsForUser(generateHeaders(jwt)).body?.snippets ?: emptyList()
            formatEventConsumer.subscription()
            coroutineScope {
                snippetIds.map { snippetId ->
                    launch {
                        val event = FormatEvent(
                            snippetId = snippetId,
                            rule = FormatRule("TEMP_RULE"),
                            timestamp = System.currentTimeMillis()
                        )
                        formatEventProducer.publish(event)
                    }
                }.forEach { it.join() }
            }

            ResponseEntity.ok().body("Started formatting all snippets")
        } catch (e: Exception) {
            logger.error("Error formatting all snippets: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    @PostMapping("/lint_all")
    suspend fun lintAllSnippets(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val snippetIds = externalService.getAllSnippetsIdsForUser(generateHeaders(jwt)).body?.snippets ?: emptyList()
            lintEventConsumer.subscription()
            coroutineScope {
                snippetIds.map { snippetId ->
                    launch {
                        val event = LintEvent(
                            snippetId = snippetId,
                            rule = LintRule("TEMP_RULE"),
                            timestamp = System.currentTimeMillis()
                        )
                        lintEventProducer.publish(event)
                    }
                }.forEach { it.join() }
            }

            ResponseEntity.ok().body("Started linting all snippets")
        } catch (e: Exception) {
            logger.error("Error linting all snippets: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }
}