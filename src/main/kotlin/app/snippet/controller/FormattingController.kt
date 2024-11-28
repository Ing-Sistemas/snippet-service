package com.example.springboot.app.snippet.controller

import com.example.springboot.app.snippet.controller.ControllerUtils.generateHeaders
import com.example.springboot.app.snippet.controller.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.external.services.printscript.response.SnippetResponse
import com.example.springboot.app.external.redis.consumer.FormatEventConsumer
import com.example.springboot.app.external.redis.events.FormatEvent
import com.example.springboot.app.external.redis.producer.FormatEventProducer
import com.example.springboot.app.snippet.service.SnippetService
import com.example.springboot.app.rule.FormatRule
import com.example.springboot.app.snippet.dto.RuleDTO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class FormattingController @Autowired constructor(
    private val formatEventConsumer: FormatEventConsumer,
    private val formatEventProducer: FormatEventProducer,
    private val snippetService: SnippetService,
    private val printScriptService: PrintScriptService,
    private val permissionService: PermissionService
) {
	private val logger = LoggerFactory.getLogger(FormattingController::class.java)

	@GetMapping("/format/rules")
    fun getFormatRules(): ResponseEntity<List<RuleDTO>> {
        val rules = listOf(
            RuleDTO(id = "4", name = "spaceAfterColon", isActive = false, value = false),
            RuleDTO(id = "5", name = "spaceAroundEquals", isActive = false, value = false),
            RuleDTO(id = "6", name = "lineJumpBeforePrintln", isActive = false, value = 0),
            RuleDTO(id = "7", name = "lineJumpAfterSemicolon", isActive = false, value = true),
            RuleDTO(id = "8", name = "singleSpaceBetweenTokens", isActive = false, value = true),
            RuleDTO(id = "9", name = "spaceAroundOperators", isActive = false, value = true)
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
            val hasPermission = permissionService.hasPermission("WRITE", snippetId, generateHeaders(jwt))

            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to format snippet"))
            }

            val response = printScriptService.format(snippetId, generateHeaders(jwt))

            if (response.body == null) {
                throw Exception("Failed to format snippet")
            }
            ResponseEntity.ok().body(response.body!!.status)
        } catch (e: Exception) {
            logger.error("Error formatting snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

	@PostMapping("/format_all")
    suspend fun formatAllSnippets(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val snippetIds = permissionService.getAllSnippetsIdsForUser(generateHeaders(jwt)).snippets
            val userId = getUserIdFromJWT(jwt)
            formatEventConsumer.subscription()
            coroutineScope {
                snippetIds.map { snippetId ->
                    launch {
                        val event = FormatEvent(
                            snippetId = snippetId,
                            rule = FormatRule("TEMP_RULE"),
                            userId = userId
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

}
