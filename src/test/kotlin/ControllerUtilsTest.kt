import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.snippets.ControllerUtils
import com.example.springboot.app.snippets.dto.SnippetDataUi
import com.example.springboot.app.snippets.dto.SnippetsGroup
import com.example.springboot.app.snippets.dto.UpdateSnippetDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.oauth2.jwt.Jwt

class ControllerUtilsTest {
    @Test
    fun `generateSnippetDTO returns valid DTO`() {
        val request =
            SnippetRequestCreate(
                title = "Test Snippet",
                language = "PrintScript",
                extension = "ps",
                version = "1.1",
                code = "println(\"Hello, World!\")",
            )

        val dto = ControllerUtils.generateSnippetDTO(request)
        val update = UpdateSnippetDTO("println('adios');")

        val file = ControllerUtils.generateFileFromData(dto, "println(\"Hello, World!\")")

        assertEquals("Test Snippet", dto.title)
        assertEquals("PrintScript", dto.language)
        assertEquals("ps", dto.extension)
        assertEquals("1.1", dto.version)
    }

    @Test
    fun `generateHeaders creates correct headers`() {
        val jwt = mock(Jwt::class.java)
        val group = SnippetsGroup(emptyList())
        `when`(jwt.tokenValue).thenReturn("jwt-token")

        val headers = ControllerUtils.generateHeaders(jwt)

        assertEquals("Bearer jwt-token", headers["Authorization"]?.first())
        assertEquals("application/json", headers.contentType.toString())
    }

    @Test
    fun `generateFile creates MultipartFile correctly`() {
        val uiData = SnippetDataUi("2", "test 1", "println(5);", "printScript", "ps", "valid", "me")
        val request =
            SnippetRequestCreate(
                title = "Test File",
                language = "Kotlin",
                extension = "kt",
                version = "1.0",
                code = "fun main() {}",
            )

        val file = ControllerUtils.generateFile(request)

        assertEquals("Test File", file.name)
        assertEquals("kt", file.contentType)
        assertEquals("fun main() {}", String(file.bytes))
    }

    @Test
    fun `getFileContent returns correct content`() {
        val file =
            MockMultipartFile(
                "testFile",
                "testFile.kt",
                "text/plain",
                "Sample content".toByteArray(),
            )

        val content = ControllerUtils.getFileContent(file)

        assertEquals("Sample content", content)
    }
}
