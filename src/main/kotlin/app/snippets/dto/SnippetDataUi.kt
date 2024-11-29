package com.example.springboot.app.snippets.dto

data class SnippetDataUi(
    val id: String,
    val name: String,
    val content: String,
    val language: String,
    val extension: String,
    val compliance: String,
    val author: String,
)
