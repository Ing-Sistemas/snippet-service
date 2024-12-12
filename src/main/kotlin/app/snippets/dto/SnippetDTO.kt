package com.example.springboot.app.snippets.dto

import com.example.springboot.app.rules.enums.SnippetStatus

class SnippetDTO(
    val id: String,
    val title: String,
    val language: String,
    val extension: String,
    val version: String,
    val status: SnippetStatus
)