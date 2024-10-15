package app.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.jetbrains.annotations.NotNull

@Entity
@Table
data class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    val id: Int? = null,
    @NotNull
    val description: String,
    @NotNull
    val language: CodingLanguage,
    @NotNull
    val code: String
)