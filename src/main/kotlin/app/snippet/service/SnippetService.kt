package com.example.springboot.app.snippet.service

import com.example.springboot.app.snippet.dto.SnippetDTO
import com.example.springboot.app.snippet.repository.SnippetRepository
import com.example.springboot.app.snippet.repository.entity.SnippetEntity
import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.repository.RulesetRepository
import com.example.springboot.app.repository.SnippetRepository
import com.example.springboot.app.repository.TestRepository
import com.example.springboot.app.repository.entity.RulesetType
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.utils.Rule
import com.example.springboot.app.utils.TestCase
import com.example.springboot.app.utils.TestCaseResult
import org.springframework.stereotype.Service


@Service
class SnippetService (
    private val snippetRepository: SnippetRepository,
    private val rulesetRepository: RulesetRepository,
    private val testRepository: TestRepository,
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

    fun getRules(
        type: RulesetType,
        userId: String
    ): List<Rule> {
        val ruleset = rulesetRepository.findByUserIdAndType(userId, type)
            ?: throw IllegalStateException("No Ruleset found for userId: $userId and type: $type")
        return ruleset.rules
    }

    fun modifyRules(
        userId: String,
        type: RulesetType,
        newRules: List<Rule>
    ): List<Rule> {
        val ruleset = rulesetRepository.findByUserIdAndType(userId, type)
            ?: throw IllegalStateException("No Ruleset found for userId: $userId and type: $type")

        val updatedRuleset = ruleset.copy(rules = newRules)
        rulesetRepository.save(updatedRuleset)

        return updatedRuleset.rules
    }

    fun getAllTests(snippetId: String): List<TestCase> {
        return testRepository.findTestEntityBySnippetId(snippetId).tests
    }

    fun addTest(test: TestCase, userId: String): TestCase {
        val testEntity = testRepository.findTestEntityById(test.id)

        val updatedTests = testEntity.tests.toMutableList().apply { add(test) }
        val updatedTestEntity = testEntity.copy(tests = updatedTests)
        testRepository.save(updatedTestEntity)

        return test
    }

    fun deleteTest(testId: String, userId: String) {
        val testEntity = testRepository.findTestEntityById(testId)
        val updatedTests = testEntity.tests.toMutableList().apply { removeIf { it.id == testId } }
        val updatedTestEntity = testEntity.copy(tests = updatedTests)
        testRepository.save(updatedTestEntity)
    }
}