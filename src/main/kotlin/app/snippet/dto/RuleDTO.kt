package com.example.springboot.app.snippet.dto

data class RuleDTO(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val value: Any? = null,
)