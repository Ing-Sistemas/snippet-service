package com.example.springboot.app.snippet.service

import com.example.springboot.app.external.rest.ui.SnippetData
import com.example.springboot.app.snippet.model.dto.PaginatedSnippets
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
            snippetDTO.content
            //snippetDTO.compliance,
            //snippetDTO.author,
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
            //snippetEntity.compliance,
            //snippetEntity.author,
        )
    }

    fun getPaginatedSnippets(page: Int, size: Int, title: String): PaginatedSnippets {

        val snippets = snippetRepository.findAll()
        val paginatedSnippets = snippets.subList(
            (page - 1) * size,
            if (page * size > snippets.size) snippets.size else page * size
        ).map { translate(it) }
        val paginatedSnippetData = paginatedSnippets.map { translateToSnippetData(it) }
        return PaginatedSnippets(
            paginatedSnippetData,
            snippets.size,
            totalPages = snippets.size / 10,
            snippets.indices.first,

        )
    }

    private fun translateToSnippetData(snippetDTO: SnippetDTO): SnippetData {
        return SnippetData(
            snippetDTO.snippetId,
            snippetDTO.title,
            snippetDTO.content,
            snippetDTO.extension,
            //snippetDTO.compliance,
            //snippetDTO.author,
        )
    }
}
