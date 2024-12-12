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
import com.example.springboot.app.rules.enums.ValueType
import com.example.springboot.app.rules.model.dto.AddRuleDTO
import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import com.example.springboot.app.rules.model.dto.RuleDTO
import com.example.springboot.app.rules.model.dto.UserRuleDTO
import com.example.springboot.app.rules.model.entity.Rule
import com.example.springboot.app.rules.model.entity.RulesUserEntity
import com.example.springboot.app.rules.repository.RuleRepository
import com.example.springboot.app.rules.repository.RuleUserRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
class RulesServiceTesting {

    @Mock
    private lateinit var ruleRepository: RuleRepository

    @Mock
    private lateinit var ruleUserRepository: RuleUserRepository

    @Mock
    private lateinit var permissionService: PermissionService

    @Mock
    private lateinit var lintEventProducer: LintEventProducer

    @Mock
    private lateinit var lintEventConsumer: LintEventConsumer

    @Mock
    private lateinit var formatEventProducer: FormatEventProducer

    @Mock
    private lateinit var formatEventConsumer: FormatEventConsumer

    private lateinit var rulesService: RulesService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        rulesService = RulesService(
            ruleRepository, ruleUserRepository, permissionService,
            lintEventProducer, lintEventConsumer,
            formatEventConsumer, formatEventProducer
        )
    }

    @Test
    fun `getRules should return correct rules`() {
        val userId = "test-user"
        val ruleType = RulesetType.LINT
        val addRule = AddRuleDTO("1", "rule 1", false, null)
        val mockRules = listOf(
            CompleteRuleDTO("1", "Test Rule", ruleType, userId, true, "some value")
        )
        val userRule = UserRuleDTO("299", false, 4, SnippetStatus.COMPLIANT, "1")
        `when`(ruleRepository.findAllRulesByUserIdAndType(userId, ruleType)).thenReturn(mockRules)

        val result = rulesService.getRules(ruleType, userId)
        assertEquals(mockRules, result)
        verify(ruleRepository).findAllRulesByUserIdAndType(userId, ruleType)
    }


    @Test
    fun `changeUserRuleCompliance should update the rule status`() {
        val rule = RuleDTO("1", "rule-1", RulesetType.LINT, ValueType.STRING)
        val userId = "test-user"
        val ruleId = "rule-1"
        val mockEntity = RulesUserEntity(userId = userId, rule = Rule(ruleId, "Test Rule"))

        `when`(ruleUserRepository.findFirstByUserIdAndRuleId(userId, ruleId)).thenReturn(mockEntity)

        rulesService.changeUserRuleCompliance(userId, ruleId, SnippetStatus.COMPLIANT)

        assertEquals(SnippetStatus.COMPLIANT, mockEntity.status)
        verify(ruleUserRepository).save(mockEntity)
    }
}
