package com.example.springboot.app.tests

import com.example.springboot.app.snippets.SnippetRepository
import com.example.springboot.app.snippets.SnippetService
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.dto.TestCaseDTO
import com.example.springboot.app.tests.entity.SnippetTest
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestStatus
import com.example.springboot.app.tests.repository.SnippetTestRepository
import com.example.springboot.app.tests.repository.TestCaseRepository
import org.slf4j.LoggerFactory
import java.util.*

class TestService(
    private val testCaseRepository: TestCaseRepository,
    private val snippetRepository: SnippetRepository,
    private val snippetTestRepository: SnippetTestRepository,
) {

    private val logger = LoggerFactory.getLogger(SnippetService::class.java)

    fun getAllTests(snippetId: String): List<TestCaseDTO> {
        logger.info("Getting all tests for snippet with id: $snippetId")

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
        logger.info("Adding test to snippet with id: $sId")
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
        logger.info("Deleting test with id: $testId")
        val findTest = testCaseRepository.findById(testId)
            .orElseThrow { IllegalStateException("Test with id: $testId not found") }
        testCaseRepository.deleteById(testId)
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