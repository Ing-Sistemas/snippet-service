package com.example.springboot.app.rules.model.seed

import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.enums.ValueType
import com.example.springboot.app.rules.model.entity.Rule
import com.example.springboot.app.rules.repository.RuleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*


@Component
class RuleSeeder @Autowired constructor(
    private val ruleRepository: RuleRepository
) : CommandLineRunner {
    val rulesMap = mapOf(
        "spaceBeforeColon" to Pair("FORMAT", "BOOLEAN"),
        "spaceAroundEquals" to Pair("FORMAT", "BOOLEAN"),
        "spaceAfterColon" to Pair("FORMAT", "BOOLEAN"),
        "lineJumpBeforePrintln" to Pair("FORMAT", "NUMBER"),
        "lineJumpAfterSemicolon" to Pair("FORMAT", "BOOLEAN"),
        "singleSpaceBetweenTokens" to Pair("FORMAT", "BOOLEAN"),
        "spaceAroundOperators" to Pair("FORMAT", "BOOLEAN"),
        "identifier_format" to Pair("LINT", "STRING"),
        "mandatory-variable-or-literal-in-println" to Pair("LINT", "BOOLEAN"),
        "mandatory-variable-or-literal-in-readInput" to Pair("LINT", "BOOLEAN")
    )

    private val logger = LoggerFactory.getLogger(RuleSeeder::class.java)
    override fun run(vararg args: String?) {
        logger.info("Seeding rules after application startup...")

        rulesMap.forEach { (name, type) ->
            val existingRule = ruleRepository.findByNameAndType(name, RulesetType.valueOf(type.first))
            if (existingRule == null) {
                logger.info("Seeding rule: $name")
                val newRule = Rule(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    type = RulesetType.valueOf(type.first),
                    valueType = ValueType.valueOf(type.second)
                )
                ruleRepository.save(newRule)
                logger.info("Seeded rule: ${newRule.name} with ID: ${newRule.id}")
            } else {
                logger.info("Rule already exists: ${existingRule.name}")
            }
        }
    }
}