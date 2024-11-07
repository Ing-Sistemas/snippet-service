package com.example.springboot.app.external.rest.request

data class SnippetRequestCreate(
    val title: String,
    val language: String,
    val description : String,
    val code: String,
    val version: String
)//todo add extension