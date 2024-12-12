import com.example.springboot.app.snippets.SnippetEntity
import com.example.springboot.app.snippets.SnippetRepository
import com.example.springboot.app.tests.TestService
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.entity.SnippetTest
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestCaseResult
import com.example.springboot.app.tests.enums.TestStatus
import com.example.springboot.app.tests.repository.SnippetTestRepository
import com.example.springboot.app.tests.repository.TestCaseRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class TestServiceTest {
    private val testCaseRepository: TestCaseRepository = mock(TestCaseRepository::class.java)
    private val snippetRepository: SnippetRepository = mock(SnippetRepository::class.java)
    private val snippetTestRepository: SnippetTestRepository = mock(SnippetTestRepository::class.java)

    private val testService =
        TestService(
            testCaseRepository,
            snippetRepository,
            snippetTestRepository,
        )

    @Test
    fun `should get all tests for a snippet`() {
        val testCase =
            TestCase(
                id = "1",
                name = "Test 1",
                input = listOf("input1"),
                output = listOf("output1"),
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

    @Test
    fun `should add a new test successfully`() {
        val snippet = mock(SnippetEntity::class.java)
        val addTestCaseDTO = AddTestCaseDTO(null, "New Test", listOf("input1"), listOf("output1"))
        `when`(snippetRepository.findSnippetEntityById("snippetId")).thenReturn(snippet)
        val fails = TestCaseResult.FAIL
        fails.toString()
        `when`(testCaseRepository.save(any(TestCase::class.java))).thenAnswer { it.getArgument(0) }

        val result = testService.addTest(addTestCaseDTO, "snippetId")

        assertNotNull(result)
        assertEquals("New Test", result.name)
        verify(snippetTestRepository).save(any(SnippetTest::class.java))
    }

    @Test
    fun `should throw exception when deleting non-existent test`() {
        `when`(testCaseRepository.findById("invalidTestId")).thenReturn(Optional.empty())
        val runTestDTO = RunTestDTO(null, null, emptyList(), emptyList(), TestStatus.FAIL)
        val exception =
            assertThrows<IllegalStateException> {
                testService.deleteTest("invalidTestId")
            }

        assertEquals("Test with id: invalidTestId not found", exception.message)
    }

    @Test
    fun `should check existence of test by id`() {
        `when`(testCaseRepository.existsById("testId")).thenReturn(true)

        val exists = testService.existsById("testId")

        assertTrue(exists)
        verify(testCaseRepository).existsById("testId")
    }

    @Test
    fun `should update an existing test successfully`() {
        val testCase = TestCase("1", "Old Test", listOf("oldInput"), listOf("oldOutput"))
        val addTestCaseDTO = AddTestCaseDTO("1", "Updated Test", listOf("newInput"), listOf("newOutput"))

        `when`(testCaseRepository.findById("1")).thenReturn(Optional.of(testCase))
        `when`(testCaseRepository.save(any(TestCase::class.java))).thenAnswer { it.getArgument(0) }

        val updatedTest = testService.updateTest(addTestCaseDTO)

        assertEquals("Updated Test", updatedTest.name)
        assertEquals(listOf("newInput"), updatedTest.input)
        assertEquals(listOf("newOutput"), updatedTest.output)
    }

    @Test
    fun `should throw exception when updating non-existent test`() {
        val addTestCaseDTO = AddTestCaseDTO("nonExistentId", "Test", listOf("input"), listOf("output"))
        `when`(testCaseRepository.findById("nonExistentId")).thenReturn(Optional.empty())

        val exception =
            assertThrows<IllegalStateException> {
                testService.updateTest(addTestCaseDTO)
            }

        assertEquals("Test with id: nonExistentId not found", exception.message)
    }

    @Test
    fun `should get all tests with correct status`() {
        val snippetTest = SnippetTest("1", TestStatus.SUCCESS, null)
        snippetTest.testCase
        snippetTest.id
        TestCaseResult.SUCCESS
        val testCase =
            TestCase(
                id = "1",
                name = "Test 1",
                input = listOf("input1"),
                output = listOf("output1"),
                snippetTests = listOf(snippetTest),
            )

        `when`(testCaseRepository.findBySnippetId("snippetId")).thenReturn(listOf(testCase))

        val result = testService.getAllTests("snippetId")

        assertEquals(1, result.size)
        assertEquals("Test 1", result[0].name)
        assertEquals(TestStatus.SUCCESS, result[0].status)
    }
}
