package app.snippet.repository.entity

import com.example.springboot.app.rule.Rule
import com.example.springboot.app.snippet.repository.converter.RuleListConverter
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "rulesets")
data class RulesetEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val type: RulesetType,

    @Lob
    @Column(name = "rules", columnDefinition = "TEXT")
    @Convert(converter = RuleListConverter::class)
    val rules: List<Rule> = emptyList()
)

enum class RulesetType {
    FORMAT,
    LINT
}