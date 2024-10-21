package com.example.springboot.app.utils

data class SnippetRequestCreate(
    val userId: String,
    val title: String,
    val language: String,
    val description : String,
    val code: String
)