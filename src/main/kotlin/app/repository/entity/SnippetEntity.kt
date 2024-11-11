package com.example.springboot.app.repository.entity

import com.example.springboot.app.utils.Rule
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class SnippetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,
    @NotNull
    val name: String,
    @NotNull
    val extension: String,
    @NotNull
    val language: String,
    @NotNull
    val version: String,
    @ElementCollection
    @CollectionTable(name = "snippet_rules", joinColumns = [JoinColumn(name = "snippet_id")])
    val rules: List<Rule> = emptyList()
)

// TODO missing properties: content, compliance, author