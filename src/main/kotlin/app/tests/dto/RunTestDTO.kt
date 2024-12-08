package com.example.springboot.app.tests.dto

data class RunTestDTO(
    val id: String?,
    val name: String,
    val input: List<String>?,
    val output: List<String>?,
)
