package com.example.springboot.app.tests.entity

import com.example.springboot.app.snippets.SnippetEntity
import jakarta.persistence.*

@Entity
data class TestCase(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    val name: String,

    @ElementCollection
    @CollectionTable(name = "test_input", joinColumns = [JoinColumn(name = "test_id")])
    @Column(name = "input")
    val input: List<String> = listOf(),

    @ElementCollection
    @CollectionTable(name = "test_output", joinColumns = [JoinColumn(name = "test_id")])
    @Column(name = "output")
    val output: List<String> = listOf(),

    @ManyToOne
    @JoinColumn(name = "snippetId", referencedColumnName = "id", nullable = false)
    val snippet: SnippetEntity? = null,

    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "testCase")
    val snippetTests: List<SnippetTest> = listOf(),
)