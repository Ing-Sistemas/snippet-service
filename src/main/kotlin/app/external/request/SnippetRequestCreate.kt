package com.example.springboot.app.external.request

data class SnippetRequestCreate(
    val title: String,
    val language: String,
    val extension : String,
    val code: String,
    val version: String,
)