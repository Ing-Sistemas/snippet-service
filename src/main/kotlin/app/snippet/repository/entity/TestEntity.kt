package com.example.springboot.app.snippet.repository.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class TestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    val name: String,

    @ElementCollection
    @CollectionTable(
        name = "test_case_inputs",
        joinColumns = [JoinColumn(name = "test_case_id")]
    )
    val input: List<String>? = null,

    @ElementCollection
    @CollectionTable(
        name = "test_case_outputs",
        joinColumns = [JoinColumn(name = "test_case_id")]
    )
    val output: List<String>? = null

)