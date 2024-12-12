package com.example.springboot.app.snippets

import com.example.springboot.app.rules.enums.SnippetStatus
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class SnippetEntity(
    @Id
    val id: String,

    @NotNull
    val title: String,

    @NotNull
    val extension: String,

    @NotNull
    val language: String,

    @NotNull
    val version: String,

    @NotNull
    @Enumerated(EnumType.STRING)
    var status: SnippetStatus = SnippetStatus.PENDING,
)
