package com.example.springboot.app.tests.repository

import com.example.springboot.app.tests.entity.SnippetTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetTestRepository: JpaRepository<SnippetTest, String> {
    fun findTestEntityById(id: String): SnippetTest
}