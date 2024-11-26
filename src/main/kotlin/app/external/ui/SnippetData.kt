package com.example.springboot.app.external.ui

data class SnippetData(
    val snippetId: String,
    val name: String,
    val content: String,
    val language: String,
    val extension: String,
    val compliance: String,
    val author: String,
)

//diff con dto [title-name, content, compliance, author]