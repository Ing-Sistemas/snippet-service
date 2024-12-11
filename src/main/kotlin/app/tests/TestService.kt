package com.example.springboot.app.tests

import com.example.springboot.app.snippets.SnippetRepository
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.dto.TestCaseDTO
import com.example.springboot.app.tests.entity.SnippetTest
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestStatus
import com.example.springboot.app.tests.repository.SnippetTestRepository
import com.example.springboot.app.tests.repository.TestCaseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class TestService @Autowired constructor(
    private val testCaseRepository: TestCaseRepository,
    private val snippetRepository: SnippetRepository,
    private val snippetTestRepository: SnippetTestRepository,
) {


    fun getAllTests(snippetId: String): List<TestCaseDTO> {

        return testCaseRepository.findBySnippetId(snippetId).map { testCase ->
            val currentStatus = testCase.snippetTests.firstOrNull()?.status ?: TestStatus.PENDING

            TestCaseDTO(
                id = testCase.id,
                name = testCase.name,
                input = testCase.input,
                output = testCase.output,
                status = currentStatus,
            )
        }
    }

    fun addTest(test: AddTestCaseDTO, sId: String): TestCase {
        val snippet = snippetRepository.findSnippetEntityById(sId)

        val toSaveTest = TestCase(
            id = UUID.randomUUID().toString(),
            name = test.name,
            input = test.input ?: emptyList(),
            output =  test.output ?: emptyList(),
            snippet = snippet,
        )
        val savedTest = testCaseRepository.save(toSaveTest)
        relateWithSnippetTest(savedTest)
        return savedTest
    }

    fun deleteTest(testId: String) {
        val findTest = testCaseRepository.findById(testId)
            .orElseThrow { IllegalStateException("Test with id: $testId not found") }
        testCaseRepository.deleteById(testId)
    }

    fun existsById(testId: String): Boolean {
        return testCaseRepository.existsById(testId)
    }

    fun updateTest(testCase: AddTestCaseDTO): TestCase {
        val test = testCaseRepository.findById(testCase.id!!)
            .orElseThrow { IllegalStateException("Test with id: ${testCase.id} not found") }
        return testCaseRepository.save(
            test.copy(
                id = test.id,
                name = testCase.name,
                input = testCase.input ?: emptyList(),
                output = testCase.output ?: emptyList(),
            )
        )
    }

    private fun relateWithSnippetTest(testCase: TestCase) {
        snippetTestRepository.save(
            SnippetTest(
                id = UUID.randomUUID().toString(),
                status = TestStatus.PENDING,
                testCase = testCase,
            )
        )
    }
}