package com.example.springboot.app.snippet.model.entity

import com.example.springboot.app.snippet.repository.converter.RuleListConverter
import com.example.springboot.app.utils.Rule
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class SnippetEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,
    @NotNull
    @Column(name = "title", nullable = false)
    val title: String,
    @NotNull
    @Column(name = "extension", nullable = false)
    val extension: String,
    @NotNull
    @Column(name = "language", nullable = false)
    val language: String,
    @NotNull
    @Column(name = "version", nullable = false)
    val version: String,

    @Lob
    @NotNull
    @Column(name = "content", columnDefinition = "TEXT")
    val content: String,

    /*@NotNull
    val compliance: String,

    @NotNull
    val author: String,*/

    @Lob
    @Column(name = "rules", columnDefinition = "TEXT")
    @Convert(converter = RuleListConverter::class)
    val rules: List<Rule> = emptyList()
)

