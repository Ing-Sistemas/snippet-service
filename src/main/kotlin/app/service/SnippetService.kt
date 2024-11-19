package com.example.springboot.app.service

import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.repository.RulesetRepository
import com.example.springboot.app.repository.SnippetRepository
import com.example.springboot.app.repository.entity.RulesetType
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.utils.Rule
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class SnippetService (
    private val snippetRepository: SnippetRepository,
    private val rulesetRepository: RulesetRepository,
) {
    fun createSnippet(
        snippetDTO: SnippetDTO
    ): SnippetEntity {
        try{
            return  snippetRepository.save(translate(snippetDTO))
        } catch (e: Exception) {
            println(e.message)
           throw Exception("Failed to create snippet with error: ${e.message}")
        }
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
            snippetDTO.version
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
}