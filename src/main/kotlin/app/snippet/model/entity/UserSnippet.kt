package com.example.springboot.app.snippet.model.entity
import jakarta.persistence.*

@Entity
class UserSnippet(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val userId: String,
    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: SnippetEntity,
)