package com.example.springboot.app.rule

import com.example.springboot.app.snippet.controller.SnippetController
import com.example.springboot.app.snippet.dto.RuleDTO
import com.example.springboot.app.snippet.repository.RuleRepository
import com.example.springboot.app.snippet.repository.RulesetType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RulesService
@Autowired constructor(
    private val ruleRepository: RuleRepository

) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    fun getRules(ruleType: RulesetType, userId: String): List<RuleDTO> {
        logger.info("Getting rules for $ruleType")
        return ruleRepository.findAllRulesByUserIdAndType(userId, ruleType)
    }

}