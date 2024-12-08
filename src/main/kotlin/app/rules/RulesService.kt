package com.example.springboot.app.rules

import com.example.springboot.app.external.redis.consumer.FormatEventConsumer
import com.example.springboot.app.external.redis.consumer.LintEventConsumer
import com.example.springboot.app.external.redis.events.FormatEvent
import com.example.springboot.app.external.redis.events.LintEvent
import com.example.springboot.app.external.redis.producer.FormatEventProducer
import com.example.springboot.app.external.redis.producer.LintEventProducer
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.rules.dto.AddRuleDTO
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.entity.Rule
import com.example.springboot.app.rules.entity.RulesUserEntity
import com.example.springboot.app.rules.enums.Compliance
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.repository.RuleRepository
import com.example.springboot.app.rules.repository.RuleUserRepository
import com.example.springboot.app.snippets.ControllerUtils.generateHeadersFromStr
import com.example.springboot.app.utils.FormatConfig
import com.example.springboot.app.utils.UserUtils
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.full.memberProperties

@Service
class RulesService
@Autowired constructor(
    private val ruleRepository: RuleRepository,
    private val ruleUserRepository: RuleUserRepository,
    private val permissionService: PermissionService,
    private val lintEventProducer: LintEventProducer,
    private val lintEventConsumer: LintEventConsumer,
    private val formatEventConsumer: FormatEventConsumer,
    private val formatEventProducer: FormatEventProducer,
    private val userUtils: UserUtils,
    ) {
    private val logger = LoggerFactory.getLogger(RulesService::class.java)

    fun getRules(ruleType: RulesetType, userId: String): List<RuleDTO> {
        logger.info("Getting rules for $ruleType")
        val rules = ruleRepository.findAllRulesByUserIdAndType(userId, ruleType)
        logger.info("Found rules ${rules.map {it.name}}")
        if (rules.isEmpty()) {
            val defaultRules = genDefaultRules(ruleType)
            defaultRules.forEach {
                logger.info("Saving rule ${it.name} with value ${it.value.toString()}")
                ruleRepository.save(
                    Rule(
                        id = it.id,
                        name = it.name,
                        value = it.value.toString(),
                        type = it.ruleType
                    )
                )
                val savedRule = ruleRepository.findRuleById(it.id)
                logger.info("Saving user rule ${savedRule.name}")
                ruleUserRepository.save(
                    RulesUserEntity(
                        userId = userId,
                        isActive = it.isActive,
                        rule = savedRule,
                    )
                )
            }
            return defaultRules
        } else {
            return rules
        }
    }

    suspend fun updateRules(
        ruleType: RulesetType,
        newRules: List<AddRuleDTO>,
        userId: String
    ) {
        logger.info("Updating rules for $ruleType and user with id $userId")
        newRules.forEach { rule ->
            val userRule = ruleUserRepository.findFirstByUserIdAndRuleId(userId, rule.id)
            if (userRule != null) {
                userRule.isActive = rule.isActive
                ruleUserRepository.save(userRule)
            } else {
                ruleUserRepository.save(
                    RulesUserEntity(
                        userId = userId,
                        isActive = rule.isActive,
                        rule = ruleRepository.findRuleById(rule.id),
                    ),
                )
            }
        }
        val message = when (ruleType) {
            RulesetType.FORMAT -> formatAllSnippets(userId)
            RulesetType.LINT -> lintAllSnippets(userId)
        }
        logger.info(message)
    }

    private fun genDefaultRules(ruleType: RulesetType): List<RuleDTO> {
        return when (ruleType) {
            RulesetType.FORMAT -> {
                val config = FormatConfig(
                    spaceBeforeColon = false,
                    spaceAfterColon = false,
                    spaceAroundEquals = false,
                    lineJumpBeforePrintln = 0,
                    lineJumpAfterSemicolon = true,
                    singleSpaceBetweenTokens = true,
                    spaceAroundOperators = true
                )
                FormatConfig::class.memberProperties.map { property ->
                    val name = property.name
                    val value = property.get(config).toString()
                    logger.info("Generating rule $name with value $value")

                    RuleDTO(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        isActive = false,
                        value = value,
                        ruleType = RulesetType.FORMAT,
                    )
                }
            }

            RulesetType.LINT -> {
                val availableRules = mapOf(
                    "identifier_format" to "none",
                    "mandatory-variable-or-literal-in-println" to false,
                    "mandatory-variable-or-literal-in-readInput" to false,
                )
                availableRules.map { (name, value)  ->
                    RuleDTO(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        isActive = false,
                        value = value, // "camel case" or "snake case"
                        ruleType = RulesetType.LINT,
                    )
                }
            }
        }
    }

    private suspend fun lintAllSnippets(
        userId: String
    ): String {
        return try {
//            logger.info("getting token")
//            val jwt = userUtils.getAuth0AccessToken()
//            logger.info("token gotten $jwt")
            val snippetIds = permissionService.getAllSnippetsIdsWithUserId(HttpHeaders(), userId)
            val lintRules = getRules(RulesetType.LINT, userId)
            lintEventConsumer.subscription()
            coroutineScope {
                snippetIds.map { snippetId ->
                    launch {
                        val event = LintEvent(
                            snippetId = snippetId,
                            userId = userId,
                            rules = lintRules
                        )
                        lintEventProducer.publish(event)
                    }
                }.forEach { it.join() }
            }
            "Started linting all snippets"
        } catch (e: Exception) {
            logger.error("Error linting all snippets: {}", e.message)
            "Error linting all snippets"
        }
    }

    private suspend fun formatAllSnippets(
        userId: String

    ): String {
        return try {
            val jwt = userUtils.getAuth0AccessToken()
            logger.info("token gotten $jwt")
            //val snippetIds = permissionService.getAllSnippetsIdsWithUserId(generateHeadersFromStr(jwt!!), userId)
            val snippetIds = permissionService.getAllSnippetsIdsWithUserId(HttpHeaders(), userId)
            val formatRules = getRules(RulesetType.FORMAT, userId)
            formatEventConsumer.subscription()
            coroutineScope {
                snippetIds.map { snippetId ->
                    launch {
                        val event = FormatEvent(
                            snippetId = snippetId,
                            userId = userId,
                            rules = formatRules
                        )
                        formatEventProducer.publish(event)
                    }
                }.forEach { it.join() }
            }

            "Started formatting all snippets"
        } catch (e: Exception) {
            logger.error("Error formatting all snippets: {}", e.message)
            "Error linting all snippets"
        }
    }

    fun changeUserRuleCompliance(
        userId: String,
        ruleId: String,
        compliance: Compliance
    ) {
        val userRule = ruleUserRepository.findFirstByUserIdAndRuleId(userId, ruleId)
        userRule?.let {
            userRule.compliance = compliance
            ruleUserRepository.save(userRule)
        }
    }
}