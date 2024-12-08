package com.example.springboot.app.utils

import com.example.springboot.app.tests.dto.RunTestDTO

data class ValidateTestRunRequest(
    val testCaseDTO: RunTestDTO,
    val sId: String
)
