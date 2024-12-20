package com.example.springboot.app.rules

import com.example.springboot.app.external.redis.consumer.FormatEventConsumer
import com.example.springboot.app.external.redis.consumer.LintEventConsumer
import com.example.springboot.app.external.redis.events.FormatEvent
import com.example.springboot.app.external.redis.events.LintEvent
import com.example.springboot.app.external.redis.producer.FormatEventProducer
import com.example.springboot.app.external.redis.producer.LintEventProducer
import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.enums.SnippetStatus
import com.example.springboot.app.rules.model.dto.AddRuleDTO
import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import com.example.springboot.app.rules.model.entity.RulesUserEntity
import com.example.springboot.app.rules.repository.RuleRepository
import com.example.springboot.app.rules.repository.RuleUserRepository
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import jakarta.transaction.Transactional
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class RulesService
    @Autowired
    constructor(
        private val ruleRepository: RuleRepository,
        private val ruleUserRepository: RuleUserRepository,
        private val permissionService: PermissionService,
        private val lintEventProducer: LintEventProducer,
        private val lintEventConsumer: LintEventConsumer,
        private val formatEventConsumer: FormatEventConsumer,
        private val formatEventProducer: FormatEventProducer,
    ) {
        private val logger = LoggerFactory.getLogger(RulesService::class.java)

        fun getRules(
            ruleType: RulesetType,
            userId: String,
        ): List<CompleteRuleDTO> {
            return ruleRepository.findAllRulesByUserIdAndType(userId, ruleType)
        }

        @Transactional
        suspend fun updateRules(
            ruleType: RulesetType,
            newRules: List<AddRuleDTO>,
            jwt: Jwt,
        ) {
            val userId = getUserIdFromJWT(jwt)
            newRules.forEach { rule ->
                val userRule = ruleUserRepository.findFirstByUserIdAndRuleId(userId, rule.id)
                if (userRule != null) {
                    userRule.isActive = rule.isActive
                    userRule.value = rule.value.toString()
                    ruleUserRepository.save(userRule)
                } else {
                    ruleUserRepository.save(
                        RulesUserEntity(
                            userId = userId,
                            isActive = rule.isActive,
                            rule = ruleRepository.findRuleById(rule.id),
                            value = rule.value.toString(),
                        ),
                    )
                }
            }
            when (ruleType) {
                RulesetType.FORMAT -> formatAllSnippets(jwt)
                RulesetType.LINT -> lintAllSnippets(jwt)
            }
        }

        private suspend fun lintAllSnippets(jwt: Jwt): String {
            return try {
                val userId = getUserIdFromJWT(jwt)
                val snippetIds = permissionService.getAllSnippetsIdsForUser(generateHeaders(jwt))
                val lintRules = getRules(RulesetType.LINT, userId)
                logger.info("New rules with values: ${lintRules.map { Pair(it.name ,it.value) }}")
                lintEventConsumer.subscription()
                coroutineScope {
                    snippetIds.map { snippetId ->
                        launch {
                            val event =
                                LintEvent(
                                    snippetId = snippetId,
                                    jwt = jwt,
                                    rules = lintRules,
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

        private suspend fun formatAllSnippets(jwt: Jwt): String {
            return try {
                val userId = getUserIdFromJWT(jwt)
                val snippetIds = permissionService.getAllSnippetsIdsForUser(generateHeaders(jwt))
                val formatRules = getRules(RulesetType.FORMAT, userId)
                logger.info("New rules with values: ${formatRules.map { Pair(it.name ,it.value) }}")
                formatEventConsumer.subscription()
                coroutineScope {
                    snippetIds.map { snippetId ->
                        launch {
                            val event =
                                FormatEvent(
                                    snippetId = snippetId,
                                    jwt = jwt,
                                    rules = formatRules,
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
            snippetStatus: SnippetStatus,
        ) {
            val userRule = ruleUserRepository.findFirstByUserIdAndRuleId(userId, ruleId)
            userRule?.let {
                userRule.status = snippetStatus
                ruleUserRepository.save(userRule)
            }
        }
    }
