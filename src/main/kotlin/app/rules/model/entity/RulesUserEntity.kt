package com.example.springboot.app.rules.model.entity

import com.example.springboot.app.rules.enums.SnippetStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.ManyToOne
import jakarta.persistence.JoinColumn
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

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
    var value: String = " ",
    @Column(name = "compilace", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SnippetStatus = SnippetStatus.PENDING,
    @ManyToOne
    @JoinColumn(name = "ruleId", referencedColumnName = "id", nullable = false)
    val rule: Rule? = null,
)
