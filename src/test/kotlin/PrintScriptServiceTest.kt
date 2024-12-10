package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.tests.dto.RunTestDTO
import com.example.springboot.app.tests.enums.TestCaseResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.lang.reflect.Field

@ExtendWith(MockitoExtension::class)
class PrintScriptServiceTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    @InjectMocks
    private lateinit var printScriptService: PrintScriptService

    private lateinit var headers: HttpHeaders
    private lateinit var runTestDTO: RunTestDTO

    @BeforeEach
    fun setUp() {
        headers = HttpHeaders()
        runTestDTO = RunTestDTO(
            id = "sId123",
            name = "testExample",
            input = listOf("test input"),
            output = listOf("expected output"),
            status = null
        )

        // Inicializar psUrl mediante reflexión
        val psUrlField: Field = PrintScriptService::class.java.getDeclaredField("psUrl")
        psUrlField.isAccessible = true
        psUrlField.set(printScriptService, "http://mocked-url")
    }

    @Test
    fun `runTests should return SUCCESS when outputs match`() {
        val url = "http://mocked-url/test/run_tests/sId123"
        val response = ResponseEntity("expected output", HttpStatus.OK)

        Mockito.`when`(
            restTemplate.postForEntity(url, HttpEntity(runTestDTO, headers), String::class.java)
        ).thenReturn(response)

        val testCaseResult = TestCaseResult.SUCCESS

        val result = printScriptService.runTests(runTestDTO, headers, "sId123")
        assertEquals("success", testCaseResult.toString())
    }

    @Test
    fun `runTests should return FAIL when outputs do not match`() {
        val url = "http://mocked-url/test/run_tests/sId123"
        val response = ResponseEntity("unexpected output", HttpStatus.OK)

        Mockito.`when`(
            restTemplate.postForEntity(url, HttpEntity(runTestDTO, headers), String::class.java)
        ).thenReturn(response)

        val result = printScriptService.runTests(runTestDTO, headers, "sId123")
        assertEquals(TestCaseResult.FAIL, result)
    }

    @Test
    fun `runTests should throw exception when service fails`() {
        val url = "http://mocked-url/test/run_tests/sId123"

        Mockito.`when`(
            restTemplate.postForEntity(url, HttpEntity(runTestDTO, headers), String::class.java)
        ).thenThrow(RuntimeException("Service error"))

        val exception = assertThrows<RuntimeException> {
            printScriptService.runTests(runTestDTO, headers, "sId123")
        }
        assertEquals("Service error", exception.message)
    }
}
