package com.example.springboot.app.service

import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.dto.UpdateSnippetDTO
import com.example.springboot.app.repository.SnippetRepository
import com.example.springboot.app.repository.entity.SnippetEntity
import org.springframework.security.oauth2.jwt.Jwt
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

    fun updateSnippet(
        updateSnippetDTO: UpdateSnippetDTO,
        jwt: Jwt
    ): SnippetEntity {
        TODO()
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

    private fun translate(snippetDTO: SnippetDTO): SnippetEntity{
        return SnippetEntity(snippetDTO.id, snippetDTO.title, snippetDTO.language, snippetDTO.version)
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO {
        return SnippetDTO(snippetEntity.id,  snippetEntity.version ,snippetEntity.title, snippetEntity.language)
    }
}