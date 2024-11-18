package com.example.springboot.app.external.rest.response

import com.example.springboot.app.snippet.model.entity.SnippetEntity


data class SnippetResponse (
    val snippetEntity: SnippetEntity?,
    val error: String?
)