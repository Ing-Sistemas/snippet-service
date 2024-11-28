package com.example.springboot.app.snippet.dto

import org.jetbrains.annotations.NotNull

data class AddTestCaseDTO(
    @NotNull
    val name: String,
    val input: List<String>?,
    val output: List<String>?,
)