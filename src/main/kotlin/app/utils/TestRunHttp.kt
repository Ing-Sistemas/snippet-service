package com.example.springboot.app.utils

import com.example.springboot.app.tests.dto.AddTestCaseDTO

data class TestRunHttp(
    val testCaseDTO: AddTestCaseDTO,
    val snippetId: String,
)
