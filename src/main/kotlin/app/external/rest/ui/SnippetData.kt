package com.example.springboot.app.external.rest.ui

data class SnippetData(
    val snippetId: String,
    val title: String,
    val content: String,
    val extension: String,
    //val compliance: String,
    //val author: String,
)