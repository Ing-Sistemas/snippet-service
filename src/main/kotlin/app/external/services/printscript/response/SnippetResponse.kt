package com.example.springboot.app.external.services.printscript.response

import com.example.springboot.app.snippets.SnippetEntity


data class SnippetResponse (
    val snippetEntity: SnippetEntity?,
    val error: String?
)