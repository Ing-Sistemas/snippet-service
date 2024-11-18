package com.example.springboot.app.snippet.model.entity

import com.example.springboot.app.snippet.repository.converter.RuleListConverter
import com.example.springboot.app.utils.Rule
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class SnippetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,
    @NotNull
    val title: String,
    @NotNull
    val extension: String,
    @NotNull
    val language: String,
    @NotNull
    val version: String,

    @Lob
    @NotNull
    val content: String,

    @NotNull
    val compliance: String,

    @NotNull
    val author: String,

    @Lob
    @Column(name = "rules", columnDefinition = "TEXT")
    @Convert(converter = RuleListConverter::class)
    val rules: List<Rule> = emptyList()
)
