package com.example.springboot.app.rules

import com.example.springboot.app.rules.dto.AddRuleDTO
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.entity.Rule
import com.example.springboot.app.rules.entity.RulesUserEntity
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.repository.RuleRepository
import com.example.springboot.app.rules.repository.RuleUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class RulesServiceTest {

    @Mock
    lateinit var ruleRepository: RuleRepository

    @Mock
    lateinit var ruleUserRepository: RuleUserRepository

    @InjectMocks
    lateinit var rulesService: RulesService

    @Captor
    lateinit var ruleCaptor: ArgumentCaptor<RulesUserEntity>

    private val userId = "test-user"

    @Test
    fun `should get rules by user and type`() {
        val expectedRules = listOf(
            RuleDTO("1", "Test Rule 1", true, "value1", RulesetType.LINT),
            RuleDTO("2", "Test Rule 2", false, "value2", RulesetType.LINT)
        )

        `when`(ruleRepository.findAllRulesByUserIdAndType(userId, RulesetType.LINT))
            .thenReturn(expectedRules)

        val actualRules = rulesService.getRules(RulesetType.LINT, userId)

        assertEquals(expectedRules, actualRules)
    }

    @Test
    fun `should update existing rule`() {
        val existingRule = RulesUserEntity(
            id = "1",
            userId = userId,
            isActive = false,
            rule = Rule("1", "Test Rule", RulesetType.LINT, "value")
        )
        val lintRule = LintRule("camel case")
        val formatRule = FormatRule("rule 1")

        val updatedRuleDTO = AddRuleDTO("1", true, "new-value")

        `when`(ruleUserRepository.findFirstByUserIdAndRuleId(userId, updatedRuleDTO.ruleId))
            .thenReturn(existingRule)

        rulesService.updateRules(RulesetType.LINT, listOf(updatedRuleDTO), userId)

        verify(ruleUserRepository).save(ruleCaptor.capture())

        val savedRule = ruleCaptor.value
        assertEquals(updatedRuleDTO.ruleId, savedRule.rule?.id)
        assertEquals(updatedRuleDTO.isActive, savedRule.isActive)
    }

    @Test
    fun `should create new rule if not exists`() {
        val newRuleDTO = AddRuleDTO("2", true, "new-value")

        val newRule = Rule("2", "New Rule", RulesetType.LINT, "new-value")

        `when`(ruleUserRepository.findFirstByUserIdAndRuleId(userId, newRuleDTO.ruleId))
            .thenReturn(null)
        `when`(ruleRepository.findById(newRuleDTO.ruleId))
            .thenReturn(Optional.of(newRule))

        rulesService.updateRules(RulesetType.LINT, listOf(newRuleDTO), userId)

        verify(ruleUserRepository).save(ruleCaptor.capture())

        val savedRule = ruleCaptor.value
        assertNotNull(savedRule)
        assertEquals(newRuleDTO.ruleId, savedRule.rule?.id)
        assertEquals(newRuleDTO.isActive, savedRule.isActive)
    }
}
