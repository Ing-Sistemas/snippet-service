package com.example.springboot.app.service

import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.repository.SnippetRepository
import com.example.springboot.app.repository.entity.SnippetEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class SnippetService @Autowired constructor(
    private val snippetRepository: SnippetRepository
) {
    fun createSnippet(snippetDTO: SnippetDTO): SnippetEntity {
        val snippetEntity = translate(snippetDTO)
        return snippetRepository.save(snippetEntity)
    }

    fun deleteSnippet(snippetId: String){
        return snippetRepository.deleteById(snippetId)
    }

    fun findSnippetById(id: Long): SnippetEntity {
        return snippetRepository.findById(id)
    }

    private fun translate(snippetDTO: SnippetDTO): SnippetEntity{
        return SnippetEntity(null, snippetDTO.title, snippetDTO.language)
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO{
        return SnippetDTO(snippetEntity.id, snippetEntity.title, snippetEntity.language)
    }
}