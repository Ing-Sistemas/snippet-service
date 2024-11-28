package com.example.springboot.app.snippet.repository

import com.example.springboot.app.snippet.repository.entity.TestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TestRepository: JpaRepository<TestEntity, String> {
    fun findTestEntityById(id: String): TestEntity
}