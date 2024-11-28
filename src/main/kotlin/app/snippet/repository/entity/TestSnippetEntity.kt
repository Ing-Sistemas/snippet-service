import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class TestSnippetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    @NotNull
    val snippetId: String,

    @ElementCollection
    val tests: List<String>, // will be the tests id's (TestEntity)

)