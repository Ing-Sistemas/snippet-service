package com.example.springboot.app.repository.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class SnippetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String?,
    @NotNull
    val title: String,
    @NotNull
    val language: String,
)