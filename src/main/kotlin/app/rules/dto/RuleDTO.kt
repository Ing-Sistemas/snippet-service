package com.example.springboot.app.rules.dto

import com.example.springboot.app.rules.enums.RulesetType

data class RuleDTO(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val value: Any? = null,
    val ruleType: RulesetType
)