package com.example.springboot.app.rules

import com.example.springboot.app.rules.dto.AddRuleDTO
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.entity.RulesUserEntity
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.repository.RuleRepository
import com.example.springboot.app.rules.repository.RuleUserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RulesService
@Autowired constructor(
    private val ruleRepository: RuleRepository,
    private val ruleUserRepository: RuleUserRepository

) {
    private val logger = LoggerFactory.getLogger(RulesService::class.java)

    fun getRules(ruleType: RulesetType, userId: String): List<RuleDTO> {
        logger.info("Getting rules for $ruleType")
        return ruleRepository.findAllRulesByUserIdAndType(userId, ruleType)
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
                        rule = ruleRepository.findById(rule.ruleId).get(),
                    ),
                )
            }
        }
        // TODO missing sending to the ps service part
    }

}