package com.example.springboot.app.snippet.controller

import com.example.springboot.app.snippet.controller.ControllerUtils.generateHeaders
import com.example.springboot.app.snippet.controller.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.external.services.printscript.response.SnippetResponse
import com.example.springboot.app.external.redis.consumer.LintEventConsumer
import com.example.springboot.app.external.redis.events.LintEvent
import com.example.springboot.app.external.redis.producer.LintEventProducer
import com.example.springboot.app.snippet.service.SnippetService
import com.example.springboot.app.rule.LintRule
import com.example.springboot.app.rule.RulesService
import com.example.springboot.app.snippet.dto.RuleDTO
import com.example.springboot.app.snippet.repository.RulesetType
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

class LintingController @Autowired constructor(
    private val lintEventProducer: LintEventProducer,
    private val lintEventConsumer: LintEventConsumer,
    private val snippetService: SnippetService,
    private val permissionService: PermissionService,
    private val printScriptService: PrintScriptService,
    private val rulesService: RulesService
) {
	private val logger = LoggerFactory.getLogger(LintingController::class.java)

	@GetMapping("/{ruleType}/rules")
    fun getLintRules(
        @PathVariable ruleType: RulesetType,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<RuleDTO>> {
        //getallrules from Rule of RulesetType == LINT or FORMAT
        return ResponseEntity.ok(rulesService.getRules(ruleType, jwt.subject))
    }

	@PostMapping("/lint")
    fun lintSnippet(
        @RequestBody snippet: SnippetRequestCreate, // TODO change request body class
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val snippetId = snippetService.findSnippetByTitle(snippet.title).snippetId
        return try {
            val hasPermission = permissionService.hasPermission("WRITE", snippetId, generateHeaders(jwt))

            if(!hasPermission) {
                ResponseEntity.status(400).body(SnippetResponse(null, "User does not have permission to lint snippet"))
            }
            val response = printScriptService.lint(snippetId, generateHeaders(jwt))

            if (response.body == null) {
                throw Exception("Failed to lint snippet")
            }
            ResponseEntity.ok().body(response.body!!.status)
        } catch (e: Exception) {
            logger.error("Error linting snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    @PostMapping("/lint_all")
    suspend fun lintAllSnippets(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val snippetIds = permissionService.getAllSnippetsIdsForUser(generateHeaders(jwt)).snippets
            val userId = getUserIdFromJWT(jwt)
            lintEventConsumer.subscription()
            coroutineScope {
                snippetIds.map { snippetId ->
                    launch {
                        val event = LintEvent(
                            snippetId = snippetId,
                            rule = LintRule("TEMP_RULE"),
                            userId = userId
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
