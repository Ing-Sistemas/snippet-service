package com.example.springboot.app.rules.model.entity

import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.enums.ValueType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.OneToMany
import jakarta.persistence.CascadeType
import org.jetbrains.annotations.NotNull

@Entity
data class Rule(
    @Id
    @NotNull
    val id: String,
    @NotNull
    val name: String,
    @Enumerated(EnumType.STRING)
    val type: RulesetType = RulesetType.LINT,
    @NotNull
    @Enumerated(EnumType.STRING)
    val valueType: ValueType = ValueType.STRING,
    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "rule")
    val userRules: List<RulesUserEntity> = listOf(),
    // que usuarios tienen esta regla
)
