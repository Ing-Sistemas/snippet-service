//package com.example.springboot.app.rules
//
//import com.example.springboot.app.rules.model.dto.AddRuleDTO
//import com.example.springboot.app.rules.model.dto.RuleDTO
//import com.example.springboot.app.rules.model.entity.Rule
//import com.example.springboot.app.rules.model.entity.RulesUserEntity
//import com.example.springboot.app.rules.enums.RulesetType
//import com.example.springboot.app.rules.enums.SnippetStatus
//import com.example.springboot.app.rules.enums.ValueType
//import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
//import com.example.springboot.app.rules.model.dto.UserRuleDTO
//import com.example.springboot.app.rules.repository.RuleRepository
//import com.example.springboot.app.rules.repository.RuleUserRepository
//import kotlinx.coroutines.test.runTest
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.ArgumentCaptor
//import org.mockito.Captor
//import org.mockito.InjectMocks
//import org.mockito.Mock
//import org.mockito.Mockito.*
//import org.mockito.junit.jupiter.MockitoExtension
//import org.springframework.security.oauth2.jwt.Jwt
//import java.util.Optional
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//
//@ExtendWith(MockitoExtension::class)
//class RulesServiceTest {
//
//    @Mock
//    lateinit var ruleRepository: RuleRepository
//
//    @Mock
//    lateinit var ruleUserRepository: RuleUserRepository
//
//    @InjectMocks
//    lateinit var rulesService: RulesService
//
//    @Captor
//    lateinit var ruleCaptor: ArgumentCaptor<RulesUserEntity>
//
//    private val userId = "test-user"
//
//    @Test
//    fun `should get rules by user and type`() {
//        val expectedRules = listOf(
//            CompleteRuleDTO(
//                id = "1",
//                name = "Test Rule",
//                ruleType = RulesetType.LINT,
//                userId = userId,
//                isActive = true,
//                value = "value",
//            ),
//            CompleteRuleDTO(
//                id = "2",
//                name = "Test Rule 2",
//                ruleType = RulesetType.LINT,
//                userId = userId,
//                isActive = false,
//                value = "value 2",
//            )
//        )
//
//        // Mocking the RuleDTOs
//        val ruleDTOs = listOf(
//            RuleDTO(id = "1", name = "Test Rule", ruleType = RulesetType.LINT, valueType = ValueType.STRING),
//            RuleDTO(id = "2", name = "Test Rule 2", ruleType = RulesetType.LINT, valueType = ValueType.STRING)
//        )
//
//        // Mocking the UserRuleDTOs
//        val userRuleDTOs = listOf(
//            UserRuleDTO(userId = userId, isActive = true, value = "value", status = SnippetStatus.SUCCESS, ruleId = "1"),
//            UserRuleDTO(userId = userId, isActive = false, value = "value 2", status = SnippetStatus.SUCCESS, ruleId = "2")
//        )
//
//        // Mocking repository calls
//        `when`(ruleRepository.findAllRulesByUserIdAndType(userId, RulesetType.LINT)).thenReturn(ruleDTOs)
//        `when`(ruleUserRepository.findAllByUserIdAndRuleType(userId, RulesetType.LINT)).thenReturn(userRuleDTOs)
//
//        val actualRules = rulesService.getRules(RulesetType.LINT, userId)
//
//
//        assertEquals(expectedRules, actualRules)
//    }
//
//    @Test
//    fun `should update existing rule`() = runTest {
//        val jwt = mock(Jwt::class.java)
//        val existingRule = RulesUserEntity(
//            id = "1",
//            userId = userId,
//            isActive = false,
//            rule = Rule("1", "Test Rule", RulesetType.LINT, "value")
//        )
//
//        val updatedRuleDTO = AddRuleDTO("1", "Test Rule",true, "new-value")
//
//        `when`(ruleUserRepository.findFirstByUserIdAndRuleId(userId, updatedRuleDTO.id))
//            .thenReturn(existingRule)
//
//        rulesService.updateRules(RulesetType.LINT, listOf(updatedRuleDTO), jwt)
//
//        verify(ruleUserRepository).save(ruleCaptor.capture())
//
//        val savedRule = ruleCaptor.value
//        assertEquals(updatedRuleDTO.id, savedRule.rule?.id)
//        assertEquals(updatedRuleDTO.isActive, savedRule.isActive)
//    }
//
//    @Test
//    fun `should create new rule if not exists`() = runTest {
//        val jwt = mock(Jwt::class.java)
//        `when`(jwt.tokenValue).thenReturn("jwt-token")
//        val newRuleDTO = AddRuleDTO("2", "New Rule", true,"new-value")
//
//        val newRule = Rule("2", "New Rule", RulesetType.LINT, "new-value")
//
//        `when`(ruleUserRepository.findFirstByUserIdAndRuleId(userId, newRuleDTO.id))
//            .thenReturn(null)
//        `when`(ruleRepository.findById(newRuleDTO.id))
//            .thenReturn(Optional.of(newRule))
//
//        rulesService.updateRules(RulesetType.LINT, listOf(newRuleDTO), jwt)
//
//        verify(ruleUserRepository).save(ruleCaptor.capture())
//
//        val savedRule = ruleCaptor.value
//        assertNotNull(savedRule)
//        assertEquals(newRuleDTO.id, savedRule.rule?.id)
//        assertEquals(newRuleDTO.isActive, savedRule.isActive)
//    }
//}
