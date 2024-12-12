package com.example.springboot.app.rules.model.dto

import com.example.springboot.app.rules.enums.SnippetStatus

data class UserRuleDTO(
    val userId: String,
    val isActive: Boolean,
    val value: Any,
    val status: SnippetStatus,
    val ruleId: String,
)
