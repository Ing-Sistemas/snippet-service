package com.example.springboot.app.repository

import com.example.springboot.app.repository.entity.SnippetEntity
import org.springframework.data.jpa.repository.JpaRepository


interface SnippetRepository: JpaRepository<SnippetEntity, String> {
    fun findById(id: Long): SnippetEntity
    fun findByLanguage(language: String): SnippetEntity
}