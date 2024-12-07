package com.example.springboot.app.rules

import com.example.springboot.app.rules.dto.AddRuleDTO
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.entity.Rule
import com.example.springboot.app.rules.entity.RulesUserEntity
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.repository.RuleRepository
import com.example.springboot.app.rules.repository.RuleUserRepository
import com.example.springboot.app.utils.FormatConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.full.memberProperties

@Service
class RulesService
@Autowired constructor(
    private val ruleRepository: RuleRepository,
    private val ruleUserRepository: RuleUserRepository

) {
    private val logger = LoggerFactory.getLogger(RulesService::class.java)

    fun getRules(ruleType: RulesetType, userId: String): List<RuleDTO> {
        logger.info("Getting rules for $ruleType")
        val rules = ruleRepository.findAllRulesByUserIdAndType(userId, ruleType)

        if (rules.isEmpty()) {
            val defaultRules = genDefaultRules(ruleType)
            defaultRules.forEach {
                ruleRepository.save(
                    Rule(
                        id = it.id,
                        name = it.name,
                        value = it.value.toString(),
                        type = it.ruleType
                    )
                )
                ruleUserRepository.save(
                    RulesUserEntity(
                        userId = userId,
                        isActive = it.isActive,
                        rule = ruleRepository.findRuleById(it.id),
                    )
                )
            }
            return defaultRules
        } else {
            return rules
        }
    }

    fun updateRules(
        ruleType: RulesetType,
        newRules: List<AddRuleDTO>,
        userId: String
    ) {
        logger.info("Updating rules for $ruleType and user with id $userId")
        newRules.forEach { rule ->
            val userRule = ruleUserRepository.findFirstByUserIdAndRuleId(userId, rule.ruleId)
            if (userRule != null) {
                userRule.isActive = rule.isActive
                ruleUserRepository.save(userRule)
            } else {
                ruleUserRepository.save(
                    RulesUserEntity(
                        userId = userId,
                        isActive = rule.isActive,
                        rule = ruleRepository.findRuleById(rule.ruleId),
                    ),
                )
            }
        }
        // TODO missing sending to the ps service part
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
                    val value = property.get(config)?.toString() ?: "null"

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
                val availableRules = listOf(
                    "identifier_format",
                    "mandatory-variable-or-literal-in-println",
                    "mandatory-variable-or-literal-in-readInput",
                )
                availableRules.map { ruleName ->
                    RuleDTO(
                        id = UUID.randomUUID().toString(),
                        name = ruleName,
                        isActive = false,
                        value = null, // "camel case" or "snake case"
                        ruleType = RulesetType.LINT,
                    )
                }
            }
        }
    }
}