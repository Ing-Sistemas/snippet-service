package com.example.springboot.app.repository.entity

import com.example.springboot.app.repository.converter.RuleListConverter
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
    @Column(name = "rules", columnDefinition = "TEXT")
    @Convert(converter = RuleListConverter::class)
    val rules: List<Rule> = emptyList()
)
