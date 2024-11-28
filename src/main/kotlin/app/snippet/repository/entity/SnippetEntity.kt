package com.example.springboot.app.snippet.repository.entity

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
)
