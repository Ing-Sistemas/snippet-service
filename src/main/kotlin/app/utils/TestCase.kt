package com.example.springboot.app.utils

data class TestCase(
    val id: String,
    val name: String,
    val input: List<String>? = null,
    val output: List<String>? = null
)