package com.example.springboot.app.tests.dto

import org.jetbrains.annotations.NotNull

data class AddTestCaseDTO(
    @NotNull
    val name: String,
    val input: List<String>?,
    val output: List<String>?,
)