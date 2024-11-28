package com.example.springboot.app.snippet.repository.entity

import Rule
import jakarta.persistence.*
import java.util.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["userId", "ruleId"])])
data class RulesUserEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "userId", nullable = false)
    val userId: String,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "ruleId", referencedColumnName = "id", nullable = false)
    val rule: Rule? = null,
)

