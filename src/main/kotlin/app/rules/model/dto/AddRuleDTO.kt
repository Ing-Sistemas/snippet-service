package com.example.springboot.app.rules.model.dto

data class AddRuleDTO(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val value: String?,
)