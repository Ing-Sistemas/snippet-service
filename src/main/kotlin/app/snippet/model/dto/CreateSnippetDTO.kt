package com.example.springboot.app.snippet.model.dto

import org.jetbrains.annotations.NotNull

data class CreateSnippetDTO(
    @field:NotNull("Title is required")
    val title: String,
    @field:NotNull("Language is required")
    val language: String,
    @field:NotNull("Content is required")
    val content: String,
)