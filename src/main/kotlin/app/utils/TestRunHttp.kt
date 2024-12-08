package com.example.springboot.app.utils

import com.example.springboot.app.tests.dto.RunTestDTO

data class TestRunHttp(
    val runTestCaseDTO: RunTestDTO,
    val snippetId: String,
)
