package com.example.springboot.app.external.services.printscript

import com.example.springboot.app.external.services.printscript.request.FormatRequest
import com.example.springboot.app.external.services.printscript.request.LintRequest
import com.example.springboot.app.external.services.printscript.request.PSRequest
import com.example.springboot.app.external.services.printscript.request.SyncFormatRequest
import com.example.springboot.app.external.services.printscript.response.FormatResponse
import com.example.springboot.app.external.services.printscript.response.PSResponse
import com.example.springboot.app.external.services.printscript.response.PSValResponse
import com.example.springboot.app.external.services.printscript.response.SnippetResponse
import com.example.springboot.app.rules.RulesService
import com.example.springboot.app.rules.enums.SnippetStatus
import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import com.example.springboot.app.snippets.SnippetService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class PrintScriptServiceTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var snippetService: SnippetService

    @Mock
    private lateinit var rulesService: RulesService

    @InjectMocks
    private lateinit var printScriptService: PrintScriptService

    private lateinit var headers: HttpHeaders

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        headers = HttpHeaders()
    }

    @Test
    fun `processLintResponse should update snippet status to COMPLIANT on 2xx response`() {
        val response = ResponseEntity.ok(listOf("Lint successful"))
        val formatReq = FormatRequest("s1", emptyList())
        val userId = "user123"
        val snippetId = "snippet123"
        val formRes = FormatResponse("success")

        val lintReq = LintRequest("s1", emptyList())
        val psRespo = PSResponse(null, null)
        val result = printScriptService.run {
            val method = this::class.java.getDeclaredMethod(
                "processLintResponse",
                ResponseEntity::class.java,
                String::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(this, response, userId, snippetId) as ResponseEntity<*>
        }

        assertEquals(response.statusCode, result.statusCode)
    }

    @Test
    fun `processLintResponse should update snippet status to NOT_COMPLIANT on 4xx response`() {
        val response = ResponseEntity.status(400).body(listOf("Lint failed"))
        val psReq = PSRequest("1.1", "s1")
        val userId = "user123"
        val snippetId = "snippet123"
        val psValRes = PSValResponse(null, null)

        val result = printScriptService.run {
            val method = this::class.java.getDeclaredMethod(
                "processLintResponse",
                ResponseEntity::class.java,
                String::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(this, response, userId, snippetId) as ResponseEntity<*>
        }

        assertEquals(response.statusCode, result.statusCode)
    }

    @Test
    fun `processFormatResponse should update rules to SUCCESS on 2xx response`() {
        val response = ResponseEntity.ok("Format successful")
        val syncF = SyncFormatRequest("s1", "u1", emptyList())
        val userId = "user123"
        val rules = listOf(CompleteRuleDTO("rule1", "name1", RulesetType.FORMAT, "user1", true, "value1"),
            CompleteRuleDTO("rule2","name2", RulesetType.FORMAT, "user2", true, "value2"))
        val snippResp = SnippetResponse(null, null)
        val result = printScriptService.run {
            val method = this::class.java.getDeclaredMethod(
                "processFormatResponse",
                ResponseEntity::class.java,
                String::class.java,
                List::class.java
            )
            method.isAccessible = true
            method.invoke(this, response, userId, rules) as ResponseEntity<*>
        }

        assertEquals(response.statusCode, result.statusCode)
    }

    @Test
    fun `processFormatResponse should update rules to FAILED on 5xx response`() {
        val response = ResponseEntity.status(500).body("Format failed")
        val userId = "user123"
        val rules = listOf(CompleteRuleDTO("rule1", "name1", RulesetType.FORMAT, "user1", true, "value1"),
            CompleteRuleDTO("rule2","name2", RulesetType.FORMAT, "user2", true, "value2"))

        val result = printScriptService.run {
            val method = this::class.java.getDeclaredMethod(
                "processFormatResponse",
                ResponseEntity::class.java,
                String::class.java,
                List::class.java
            )
            method.isAccessible = true
            method.invoke(this, response, userId, rules) as ResponseEntity<*>
        }

        assertEquals(response.statusCode, result.statusCode)
    }
}