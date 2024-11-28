import com.example.springboot.app.snippet.repository.TestStatus
import com.example.springboot.app.snippet.repository.entity.TestCase
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

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