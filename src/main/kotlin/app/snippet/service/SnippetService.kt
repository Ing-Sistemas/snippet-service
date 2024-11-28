package com.example.springboot.app.snippet.service

import RuleEntity
import com.example.springboot.app.snippet.dto.RuleDTO
import com.example.springboot.app.snippet.dto.SnippetDTO
import com.example.springboot.app.snippet.repository.*
import com.example.springboot.app.snippet.repository.entity.SnippetEntity
import com.example.springboot.app.snippet.repository.entity.RulesetType
import com.example.springboot.app.snippet.repository.entity.TestEntity
import com.example.springboot.app.testing.TestCase
import org.springframework.stereotype.Service


@Service
class SnippetService(
    private val snippetRepository: SnippetRepository,
    private val ruleUserRepository: RuleUserRepository,
    private val testSnippetRepository: TestSnippetRepository,
    private val ruleRepository: RuleRepository,
    private val testRepository: TestRepository
) {
    fun createSnippet(
        snippetDTO: SnippetDTO
    ): SnippetEntity {
        return  snippetRepository.save(translate(snippetDTO))
    }

    fun deleteSnippet(snippetId: String){
        return snippetRepository.deleteById(snippetId)
    }

    fun findSnippetById(id: String): SnippetDTO {
        return translate(snippetRepository.findSnippetEntityById(id))
    }

    fun findSnippetByTitle(title: String): SnippetDTO {
        return translate(snippetRepository.findSnippetEntityByTitle(title))
    }

    private fun translate(snippetDTO: SnippetDTO): SnippetEntity {
        return SnippetEntity(
            snippetDTO.snippetId,
            snippetDTO.title,
            snippetDTO.extension,
            snippetDTO.language,
            snippetDTO.version,
        )
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO {
        return SnippetDTO(
            snippetEntity.id,
            snippetEntity.title,
            snippetEntity.extension,
            snippetEntity.language,
            snippetEntity.version,
        )
    }

    fun getLintingRules(
        type: RulesetType,
        userId: String
    ): List<RuleEntity> {
        val ruleset = ruleUserRepository.findByUserIdAndType(userId, type)
            ?: throw IllegalStateException("No Ruleset found for userId: $userId and type: $type")
        val lintingRules = ruleset.lintingRules.map { ruleRepository.findRuleById(it) }

        return lintingRules
    }

    fun getFormattingRules(
        type: RulesetType,
        userId: String
    ): List<RuleEntity> {
        val ruleset = ruleUserRepository.findByUserIdAndType(userId, type)
            ?: throw IllegalStateException("No Ruleset found for userId: $userId and type: $type")
        val formattingRules = ruleset.formattingRules.map { ruleRepository.findRuleById(it) }

        return formattingRules
    }

    fun modifyLintingRules(
        userId: String,
        type: RulesetType,
        newRuleset: List<RuleDTO>
    ): List<RuleDTO> {
        val rulesUserEntity = ruleUserRepository.findByUserIdAndType(userId, type)
            ?: throw IllegalStateException("No Ruleset found for userId: $userId and type: $type")

        // Crear y guardar las nuevas reglas
        val savedRules = newRuleset.map { rule ->
            val ofTypeRuleEntity = RuleEntity(
                rule.id,
                rule.name,
                rule.isActive,
                rule.value
            )
            val savedRule = ruleRepository.save(ofTypeRuleEntity)
            savedRule.id
        }


        val updatedRuleset = rulesUserEntity.copy(lintingRules = savedRules)

        ruleUserRepository.save(updatedRuleset)
        return newRuleset
    }

    fun modifyFormattingRules(
        userId: String,
        type: RulesetType,
        newRuleset: List<RuleDTO>
    ): List<RuleDTO> {
        val rulesUserEntity = ruleUserRepository.findByUserIdAndType(userId, type)
            ?: throw IllegalStateException("No Ruleset found for userId: $userId and type: $type")

        // Crear y guardar las nuevas reglas
        val savedRules = newRuleset.map { rule ->
            val ofTypeRuleEntity = RuleEntity(
                rule.id,
                rule.name,
                rule.isActive,
                rule.value
            )
            val savedRule = ruleRepository.save(ofTypeRuleEntity)
            savedRule.id
        }


        val updatedRuleset = rulesUserEntity.copy(formattingRules = savedRules)

        ruleUserRepository.save(updatedRuleset)
        return newRuleset
    }


    fun getAllTests(snippetId: String): List<TestCase> {
        val testsId = testSnippetRepository.findTestEntityBySnippetId(snippetId).tests
        val testSnippetEntity = testsId.map { testRepository.findTestEntityById(it) }
        return testSnippetEntity.map { TestCase(it.id, it.name, it.input, it.output) }
    }

    fun addTest(test: TestCase, userId: String, sId: String): TestCase {
        val testSnippetEntity = testSnippetRepository.findTestEntityBySnippetId(sId)
        val testSnippetIds = testSnippetEntity.tests

        val toSaveTest = TestEntity(
            test.id,
            test.name,
            test.input,
            test.output
        )

        testRepository.save(toSaveTest)

        val updatedTestsIdList = testSnippetIds.toMutableList().apply { add(test.id) }
        val updatedTestSnippetEntity = testSnippetEntity.copy(tests = updatedTestsIdList)
        testSnippetRepository.save(updatedTestSnippetEntity)

        return test
    }

    fun deleteTest(testId: String, userId: String) {
        val testEntity = testSnippetRepository.findTestEntityById(testId)
        val updatedTests = testEntity.tests.toMutableList().apply { removeIf { it == testId } }
        val updatedTestEntity = testEntity.copy(tests = updatedTests)
        testSnippetRepository.save(updatedTestEntity)
    }

}