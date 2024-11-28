import com.example.springboot.app.snippet.repository.RulesetType
import com.example.springboot.app.snippet.repository.entity.RulesUserEntity
import jakarta.persistence.*

@Entity
data class Rule (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    val type: RulesetType = RulesetType.LINT,

    @Column(name = "value")
    val value: Any? = null,

    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "rule")
    val userRules: List<RulesUserEntity> = listOf(),
    // que usuarios tienen esta regla
)
