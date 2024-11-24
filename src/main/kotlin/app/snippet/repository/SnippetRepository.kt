package com.example.springboot.app.snippet.repository

import com.example.springboot.app.snippet.repository.entity.SnippetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository: JpaRepository<SnippetEntity, String> {
    fun findSnippetEntityById(id: String): SnippetEntity
    fun findByLanguage(language: String): SnippetEntity
    fun findSnippetEntityByTitle(title: String): SnippetEntity
}