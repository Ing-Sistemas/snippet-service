package com.example.springboot.app.snippet.dto

data class AddRuleDTO(
    val ruleId: String,
    val isActive: Boolean,
    val value: String,
)