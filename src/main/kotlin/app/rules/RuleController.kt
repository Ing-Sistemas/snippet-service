package com.example.springboot.app.rules


import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.rules.model.dto.AddRuleDTO
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
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
    private val permissionService: PermissionService,
    private val printScriptService: PrintScriptService,
) {

    private val logger = LoggerFactory.getLogger(RuleController::class.java)

    @GetMapping("/{ruleType}/rules")
    fun getRules(
        @PathVariable ruleType: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<CompleteRuleDTO>> {
        logger.trace("Getting rules for $ruleType")
        val ruleSetType = RulesetType.valueOf(ruleType.uppercase(Locale.getDefault()))
        val rules = rulesService.getRules(ruleSetType, getUserIdFromJWT(jwt))
        return ResponseEntity.ok(rules)
    }

    @PutMapping("/{ruleType}")
    suspend fun editRule(
        @PathVariable ruleType: String,
        @RequestBody rules: List<AddRuleDTO>,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        logger.trace("Updating rules for $ruleType")
        val ruleTypeEnum = RulesetType.valueOf(ruleType)
        return rulesService.updateRules(ruleTypeEnum, rules, jwt)
    }

    //--------------------------------FORMAT----------------------------

    @PostMapping("/format/{snippetId}")
    fun formatSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        logger.trace("Formatting snippet $snippetId")
        return try {
            if (permissionService.hasPermissionBySnippetId("WRITE",snippetId, generateHeaders(jwt))) {

                printScriptService.format(snippetId, jwt)
            } else {
                ResponseEntity.status(403).build()
            }
        } catch (e: Exception) {
            logger.error("Error formatting snippet: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }
}