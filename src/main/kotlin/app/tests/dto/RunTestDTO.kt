package com.example.springboot.app.tests.dto

import com.example.springboot.app.tests.enums.TestStatus

data class RunTestDTO(
    val id: String?,
    val name: String,
    val input: List<String>?,
    val output: List<String>?,
    val status: TestStatus? = null
)
