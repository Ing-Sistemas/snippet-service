package com.example.springboot.app.utils

import jakarta.persistence.*

@Embeddable
data class TestCase (
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