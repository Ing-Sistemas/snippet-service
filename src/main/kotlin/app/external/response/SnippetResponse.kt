package com.example.springboot.app.external.response

import com.example.springboot.app.repository.entity.SnippetEntity


data class SnippetResponse (
    val snippetEntity: SnippetEntity?,
    val error: String?
)