package com.example.springboot.app.rules.model.entity

import com.example.springboot.app.rules.enums.SnippetStatus
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
    var isActive: Boolean = false,

    @Column(name = "value", nullable = true)
    var value: String? = null,

    @Column(name="compilace", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SnippetStatus = SnippetStatus.PENDING,

    @ManyToOne
    @JoinColumn(name = "ruleId", referencedColumnName = "id", nullable = false)
    val rule: Rule? = null,
)

