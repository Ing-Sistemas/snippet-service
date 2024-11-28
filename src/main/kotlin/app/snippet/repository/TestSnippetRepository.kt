package com.example.springboot.app.snippet.repository

import TestSnippetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestSnippetRepository: JpaRepository<TestSnippetEntity, String> {
    fun findTestEntityBySnippetId(snippetId: String): TestSnippetEntity
    fun findTestEntityById(id: String): TestSnippetEntity
}