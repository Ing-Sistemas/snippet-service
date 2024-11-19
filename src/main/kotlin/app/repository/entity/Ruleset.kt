package com.example.springboot.app.repository.entity

import com.example.springboot.app.utils.Rule
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "rulesets")
data class Ruleset(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val type: RulesetType,

    @ElementCollection
    @CollectionTable(
        name = "rules",
        joinColumns = [JoinColumn(name = "ruleset_id")]
    )
    var rules: List<Rule> = emptyList()
)

enum class RulesetType {
    FORMAT,
    LINT
}