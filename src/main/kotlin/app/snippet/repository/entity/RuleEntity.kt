import jakarta.persistence.*

@Entity
data class RuleEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean,

    @Column(name = "value")
    val value: Any? = null
)
