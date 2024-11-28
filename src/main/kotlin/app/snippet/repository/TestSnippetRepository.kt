package com.example.springboot.app.snippet.repository

import SnippetTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestSnippetRepository: JpaRepository<SnippetTest, String> {
    fun findTestEntityBySnippetId(snippetId: String): SnippetTest
    fun findTestEntityById(id: String): SnippetTest
}