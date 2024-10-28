package com.example.springboot.app.repository

import com.example.springboot.app.repository.entity.SnippetEntity
import org.springframework.data.jpa.repository.JpaRepository


interface SnippetRepository: JpaRepository<SnippetEntity, String> {
    fun findSnippetEntityById(id: String): SnippetEntity
    fun findByLanguage(language: String): SnippetEntity
    fun findSnippetEntityByTitle(title: String): SnippetEntity
}