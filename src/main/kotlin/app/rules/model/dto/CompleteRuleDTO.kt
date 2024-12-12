package com.example.springboot.app.rules.model.dto

import com.example.springboot.app.rules.enums.RulesetType

data class CompleteRuleDTO(
    val id: String,
    val name: String,
    val ruleType: RulesetType,
    val userId: String,
    val isActive: Boolean,
    val value: Any,
)
