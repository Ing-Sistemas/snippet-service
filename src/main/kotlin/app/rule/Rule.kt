package com.example.springboot.app.rule

import jakarta.persistence.Embeddable

@Embeddable
data class Rule (
    val id: String,
    val name: String,
    val isActive: Boolean,
    val value: Any? = null
)