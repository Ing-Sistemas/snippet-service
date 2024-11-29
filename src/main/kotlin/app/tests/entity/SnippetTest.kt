package com.example.springboot.app.tests.entity

import com.example.springboot.app.tests.enums.TestStatus
import jakarta.persistence.*

@Entity
data class SnippetTest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    @Enumerated(EnumType.STRING)
    var status: TestStatus = TestStatus.PENDING,

    @ManyToOne
    @JoinColumn(name = "testId", nullable = false)
    val testCase: TestCase? = null,
)