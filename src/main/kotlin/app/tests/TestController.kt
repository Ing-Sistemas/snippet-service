package com.example.springboot.app.tests

import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.tests.dto.AddTestCaseDTO
import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.dto.TestCaseDTO
import com.example.springboot.app.tests.entity.TestCase
import com.example.springboot.app.tests.enums.TestCaseResult
import com.example.springboot.app.tests.enums.TestStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import kotlin.math.log

@RestController
@RequestMapping("/api")
class TestController @Autowired constructor(
    private val printScriptService: PrintScriptService,
    private val permissionService: PermissionService,
    private val testService: TestService,
){
    private val logger = LoggerFactory.getLogger(TestController::class.java)

    @GetMapping("/test/{snippetId}")
    fun getTestCases(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable snippetId: String
    ): ResponseEntity<List<TestCaseDTO>> {
        return try {
            logger.trace("Getting test cases for snippet with id: {}", snippetId)
            val hasPermission = permissionService.hasPermissionBySnippetId("READ", snippetId, generateHeaders(jwt))
            if (!hasPermission) return ResponseEntity.status(403).build()
            val testList = testService.getAllTests(snippetId)
            ResponseEntity.ok(testList)
        } catch (e: Exception) {
            logger.error("Error getting test cases: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    @PutMapping("/test")
    fun postTestCase(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody testCase: AddTestCaseDTO,
        @RequestParam sId: String
    ): ResponseEntity<TestCaseDTO> {
        return try {
            logger.trace("Adding test case with name: ${testCase.name}")
            if (testCase.id != null && testService.existsById(testCase.id)) {
                val test = testService.updateTest(testCase)
                val testCaseDTO = TestCaseDTO(
                    id = test.id,
                    name = test.name,
                    input = test.input,
                    output = test.output,
                    status = test.snippetTests.firstOrNull()?.status ?: TestStatus.PENDING
                )
                return ResponseEntity.ok(testCaseDTO)
            }

            val test = testService.addTest(testCase, sId)
            val testCaseDTO = TestCaseDTO(
                id = test.id,
                name = test.name,
                input = test.input,
                output = test.output,
                status = test.snippetTests.firstOrNull()?.status ?: TestStatus.PENDING
            )
            ResponseEntity.ok(testCaseDTO)
        } catch (e: Exception) {
            logger.error("Error adding test case: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }

    @DeleteMapping("/test/{id}")
    fun deleteTestCase(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String
    ): ResponseEntity<Void> {
        return try {
            logger.trace("Deleting test case with id: {}", id)
            testService.deleteTest(id)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error("Error deleting test case: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }


    @PutMapping("/test/run_tests/{sId}")
    fun runTests(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable sId: String,
        @RequestBody runTestDTO: RunTestDTO
    ): ResponseEntity<TestCaseResult> {
        return try {
            logger.trace("Running tests for snippet with id: {}", sId)
            val headers = generateHeaders(jwt)
            val result = printScriptService.runTests(runTestDTO, headers, sId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error running tests: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }
}