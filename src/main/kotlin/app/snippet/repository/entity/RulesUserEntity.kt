package com.example.springboot.app.snippet.repository.entity

import jakarta.persistence.*
import java.util.*

@Entity
data class RulesUserEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val type: RulesetType,

    @ElementCollection
    val lintingRules: List<String>, // will be the rules id's

    @ElementCollection
    val formattingRules: List<String>
)

enum class RulesetType {
    FORMAT,
    LINT
}