package com.example.springboot.app.rules.model.dto

import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.enums.ValueType

data class RuleDTO(
    val id: String,
    val name: String,
    val ruleType: RulesetType,
    val valueType: ValueType
)