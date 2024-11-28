package com.example.springboot.app.snippet.dto

import com.example.springboot.app.snippet.repository.TestStatus

data class TestCaseDTO(
    val id: String,
    val name: String,
    val input: List<String>,
    val output: List<String>,
    val status: TestStatus?,
)