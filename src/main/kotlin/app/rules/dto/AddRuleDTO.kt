package com.example.springboot.app.rules.dto

data class AddRuleDTO(
    val ruleId: String,
    val isActive: Boolean,
    val value: String,
)