package com.example.springboot.app.tests

import com.example.springboot.app.external.services.permission.PermissionService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.tests.dto.AddTestCaseDTO
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
            // TODO validate permission instead
            if (testCase.id != null && testService.existsById(testCase.id)) {
                // update the test case
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
            // TODO check write permissions
            testService.deleteTest(id)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error("Error deleting test case: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }


    // TODO finish
    @GetMapping("/test")
    fun runTests(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam testCase: TestCaseDTO, // TODO tiene q ser dto
        @RequestParam snippetId: String
    ): ResponseEntity<TestCaseResult> {
        return try {
            val userId = getUserIdFromJWT(jwt)
            val result = printScriptService.runTests(testCase, userId, snippetId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error running tests: {}", e.message)
            ResponseEntity.status(500).build()
        }
    }
}