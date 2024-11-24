package com.example.springboot.app.external.ui

data class SnippetData(
    val snippetId: String,
    val name: String,
    val content: String,
    val extension: String,
    val compliance: String,
    val author: String,
)