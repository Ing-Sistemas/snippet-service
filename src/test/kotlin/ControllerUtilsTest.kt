import com.example.springboot.app.external.services.printscript.request.SnippetRequestCreate
import com.example.springboot.app.snippets.ControllerUtils
import com.example.springboot.app.snippets.dto.SnippetDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.mock.web.MockMultipartFile

class ControllerUtilsTest {

    @Test
    fun `generateSnippetDTO returns valid DTO`() {
        val request = SnippetRequestCreate(
            title = "Test Snippet",
            language = "Kotlin",
            extension = "kt",
            version = "1.0",
            code = "print(\"Hello, World!\")"
        )

        val dto = ControllerUtils.generateSnippetDTO(request)

        assertEquals("Test Snippet", dto.title)
        assertEquals("Kotlin", dto.language)
        assertEquals("kt", dto.extension)
        assertEquals("1.0", dto.version)
    }

    @Test
    fun `generateHeaders creates correct headers`() {
        val jwt = mock(Jwt::class.java)
        `when`(jwt.tokenValue).thenReturn("jwt-token")

        val headers = ControllerUtils.generateHeaders(jwt)

        assertEquals("Bearer jwt-token", headers["Authorization"]?.first())
        assertEquals("application/json", headers.contentType.toString())
    }

    @Test
    fun `generateFile creates MultipartFile correctly`() {
        val request = SnippetRequestCreate(
            title = "Test File",
            language = "Kotlin",
            extension = "kt",
            version = "1.0",
            code = "fun main() {}"
        )

        val file = ControllerUtils.generateFile(request)

        assertEquals("Test File", file.name)
        assertEquals("kt", file.contentType)
        assertEquals("fun main() {}", String(file.bytes))
    }

    @Test
    fun `getFileContent returns correct content`() {
        val file = MockMultipartFile(
            "testFile",
            "testFile.kt",
            "text/plain",
            "Sample content".toByteArray()
        )

        val content = ControllerUtils.getFileContent(file)

        assertEquals("Sample content", content)
    }
}
