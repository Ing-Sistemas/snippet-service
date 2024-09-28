import app.SnippetSearcherApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [SnippetSearcherApplication::class])
class SnippetSearcherApplicationTests {

	@Test
	fun contextLoads() {
	}
}