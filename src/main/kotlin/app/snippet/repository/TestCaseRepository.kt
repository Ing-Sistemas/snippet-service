package com.example.springboot.app.snippet.repository

import com.example.springboot.app.snippet.repository.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestCaseRepository: JpaRepository<TestCase, String> {
    fun findBySnippetId(sId: String): List<TestCase>
}