package com.example.springboot.app.rules

import com.example.springboot.app.external.redis.consumer.FormatEventConsumer
import com.example.springboot.app.external.redis.consumer.LintEventConsumer
import com.example.springboot.app.external.redis.events.FormatEvent
import com.example.springboot.app.external.redis.events.LintEvent
import com.example.springboot.app.external.redis.producer.FormatEventProducer
import com.example.springboot.app.external.redis.producer.LintEventProducer
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.external.services.printscript.response.SnippetResponse
import com.example.springboot.app.rules.dto.AddRuleDTO
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.snippets.SnippetService
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
class RuleController @Autowired constructor(
    private val rulesService: RulesService,
    private val snippetService: SnippetService,
    private val permissionService: PermissionService,
    private val printScriptService: PrintScriptService,
    private val lintEventProducer: LintEventProducer,
    private val lintEventConsumer: LintEventConsumer,
    private val formatEventConsumer: FormatEventConsumer,
    private val formatEventProducer: FormatEventProducer,
) {

    private val logger = LoggerFactory.getLogger(RuleController::class.java)

    @GetMapping("/{ruleType}/rules")
    fun getRules(
        @PathVariable ruleType: RulesetType,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<RuleDTO>> {
        return ResponseEntity.ok(rulesService.getRules(ruleType, getUserIdFromJWT(jwt)))
    }

    @PostMapping("/{ruleType}")
    fun editRule(
        @PathVariable ruleType: RulesetType,
        @RequestBody rules: List<AddRuleDTO>,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        return rulesService.updateRules(ruleType, rules, getUserIdFromJWT(jwt))
    }

    // TODO add to the editRule method
    @PostMapping("/lint_all")
    suspend fun lintAllSnippets(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val snippetIds = permissionService.getAllSnippetsIdsForUser(generateHeaders(jwt))
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

    //--------------------------------FORMAT----------------------------

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody snippet: SnippetRequestCreate,//TODO change request body class
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val snippetId = snippetService.findSnippetByTitle(snippet.title).snippetId
        return try {
            val hasPermission = permissionService.hasPermissionByTitle("WRITE", snippetId, generateHeaders(jwt))

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
            val snippetIds = permissionService.getAllSnippetsIdsForUser(generateHeaders(jwt))
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