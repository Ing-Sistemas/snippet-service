package com.example.springboot.app.utils.rest.ui

data class SnippetData(
    val snippetId: String,
    val title: String,
    val language: String,
    val version: String,
    val code: String
)