package com.example.springboot.app.utils

import com.example.springboot.app.tests.dto.TestCaseDTO

data class TestRunHttp(
    val testCaseDTO: TestCaseDTO,
    val snippetId: String,
)
