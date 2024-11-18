package com.example.springboot.app.snippet.service

import com.example.springboot.app.snippet.model.dto.SnippetDTO
import com.example.springboot.app.snippet.repository.SnippetRepository
import com.example.springboot.app.snippet.model.entity.SnippetEntity
import org.springframework.stereotype.Service


@Service
class SnippetService (
    private val snippetRepository: SnippetRepository,
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
            snippetDTO.version,
            snippetDTO.content,
            snippetDTO.compliance,
            snippetDTO.author,

        )
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO {
        return SnippetDTO(
            snippetEntity.id,
            snippetEntity.title,
            snippetEntity.extension,
            snippetEntity.language,
            snippetEntity.version,
            snippetEntity.content,
            snippetEntity.compliance,
            snippetEntity.author,
        )
    }
}