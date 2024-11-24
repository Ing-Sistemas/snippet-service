package com.example.springboot.app.controller

import com.example.springboot.app.controller.ControllerUtils.generateHeaders
import com.example.springboot.app.controller.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.external.permission.PermissionService
import com.example.springboot.app.external.printscript.PrintScriptService
import com.example.springboot.app.external.request.SnippetRequestCreate
import com.example.springboot.app.external.response.SnippetResponse
import com.example.springboot.app.redis.consumer.FormatEventConsumer
import com.example.springboot.app.redis.events.FormatEvent
import com.example.springboot.app.redis.producer.FormatEventProducer
import com.example.springboot.app.service.SnippetService
import com.example.springboot.app.utils.FormatRule
import com.example.springboot.app.utils.Rule
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
