package com.example.springboot.app.rules.entity

import com.example.springboot.app.rules.enums.RulesetType
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class Rule (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    val id: String,

    @NotNull
    val name: String,

    @Enumerated(EnumType.STRING)
    val type: RulesetType = RulesetType.LINT,

    val value: String? = null,

    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "rule")
    val userRules: List<RulesUserEntity> = listOf(),
    // que usuarios tienen esta regla
)
