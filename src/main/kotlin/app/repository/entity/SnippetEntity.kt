package com.example.springboot.app.repository.entity

import jakarta.persistence.*

@Entity
data class SnippetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long?,
    val title: String,
    val language: String,
)