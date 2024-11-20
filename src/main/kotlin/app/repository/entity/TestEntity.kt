package com.example.springboot.app.repository.entity

import com.example.springboot.app.repository.converter.TestListConverter
import com.example.springboot.app.utils.TestCase
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import org.jetbrains.annotations.NotNull

@Entity
data class TestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    @NotNull
    val snippetId: String,

    @Lob
    @Column(name = "tests", columnDefinition = "TEXT")
    @Convert(converter = TestListConverter::class)
    val tests: List<TestCase> = emptyList()
)