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
import java.util.*

@RestController
@RequestMapping("/api")
class RuleController @Autowired constructor(
    private val rulesService: RulesService,
    private val snippetService: SnippetService,
    private val permissionService: PermissionService,
    private val printScriptService: PrintScriptService,
) {

    private val logger = LoggerFactory.getLogger(RuleController::class.java)

    @GetMapping("/{ruleType}/rules")
    fun getRules(
        @PathVariable ruleType: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<RuleDTO>> {
        val ruleSetType = RulesetType.valueOf(ruleType.uppercase(Locale.getDefault()))
        val rules = rulesService.getRules(ruleSetType, getUserIdFromJWT(jwt))
        logger.info("Returning rules : ${rules.map { it.name }}")
        return ResponseEntity.ok(rules)
    }

    @PostMapping("/{ruleType}")
    fun editRule(
        @PathVariable ruleType: RulesetType,
        @RequestBody rules: List<AddRuleDTO>,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        return rulesService.updateRules(ruleType, rules, getUserIdFromJWT(jwt))
    }

    //--------------------------------FORMAT----------------------------

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody snippet: SnippetRequestCreate,//TODO change request body class
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val snippetId = snippetService.findSnippetByTitle(snippet.title).id
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
}