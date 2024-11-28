package com.example.springboot.app.snippet.dto

import com.example.springboot.app.snippet.repository.RulesetType

data class RuleDTO(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val value: Any? = null,
    val ruleType: RulesetType
)