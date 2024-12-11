package com.example.springboot.app.external.services.printscript.request

data class SnippetRequestCreate(
    val title: String,
    val language: String,
    val extension : String,
    val code: String,
    val version: String,
)