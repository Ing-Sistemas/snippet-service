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
}