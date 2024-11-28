package com.example.springboot.app.snippet.repository

import com.example.springboot.app.snippet.repository.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository

interface TestRepository: JpaRepository<TestCase, String> {
    fun findTestEntityById(id: String): TestCase
}