import com.example.springboot.app.snippets.SnippetRepository
import com.example.springboot.app.tests.TestService
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.repository.SnippetTestRepository
import com.example.springboot.app.tests.repository.TestCaseRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TestServiceTest {

    private val testCaseRepository: TestCaseRepository = mock(TestCaseRepository::class.java)
    private val snippetRepository: SnippetRepository = mock(SnippetRepository::class.java)
    private val snippetTestRepository: SnippetTestRepository = mock(SnippetTestRepository::class.java)

    private val testService = TestService(
        testCaseRepository, snippetRepository, snippetTestRepository
    )

    @Test
    fun `should get all tests for a snippet`() {
        val testCase = TestCase(
            id = "1",
            name = "Test 1",
            input = listOf("input1"),
            output = listOf("output1")
        )
        val addTestCaseDTO = AddTestCaseDTO(null, "Test 1", emptyList(), emptyList())

        `when`(testCaseRepository.findBySnippetId("snippet1")).thenReturn(listOf(testCase))

        val result = testService.getAllTests("snippet1")

        assertEquals(1, result.size)
        assertEquals("Test 1", result[0].name)
    }



    @Test
    fun `should delete a test by id`() {
        val testCase = TestCase("1", "Test 1", listOf("input1"), listOf("output1"))
        `when`(testCaseRepository.findById("1")).thenReturn(java.util.Optional.of(testCase))

        testService.deleteTest("1")

        verify(testCaseRepository).deleteById("1")
    }
}
