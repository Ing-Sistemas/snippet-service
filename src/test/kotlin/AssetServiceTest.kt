package com.example.springboot.app.asset

import com.example.springboot.app.external.services.asset.AssetService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono

class AssetServiceTest {

    private lateinit var assetService: AssetService
    private lateinit var mockWebClient: WebClient
    private lateinit var mockRequestBodySpec: RequestBodySpec
    private lateinit var mockRequestHeadersSpec: RequestHeadersSpec<*>
    private lateinit var mockResponseSpec: ResponseSpec

    private val assetURL = "http://example.com"

    @BeforeEach
    fun setup() {
        mockWebClient = mock(WebClient::class.java)
        mockRequestBodySpec = mock(WebClient.RequestBodySpec::class.java)
        mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec::class.java)
        mockResponseSpec = mock(WebClient.ResponseSpec::class.java)

        assetService = AssetService(assetURL)
        val clientField = assetService.javaClass.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(assetService, mockWebClient)
    }

    @Test
    fun `test saveSnippet successfully`() {
        val snippetId = "testSnippet"
        val mockFile = MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".toByteArray())
        val responseEntity = ResponseEntity.ok("Success")

        val mockRequestBodyUriSpec: WebClient.RequestBodyUriSpec = mock(WebClient.RequestBodyUriSpec::class.java)

        `when`(mockWebClient.put()).thenReturn(mockRequestBodyUriSpec)
        `when`(mockRequestBodyUriSpec.uri("/{container}/{snippetId}", "test-container", snippetId)).thenReturn(mockRequestBodySpec)
        `when`(mockRequestBodySpec.header("accept", "*/*")).thenReturn(mockRequestBodySpec)
        `when`(mockRequestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(mockRequestBodySpec)
        `when`(mockRequestBodySpec.bodyValue(mockFile.bytes)).thenReturn(mockRequestHeadersSpec)
        `when`(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec)
        `when`(mockResponseSpec.toEntity(String::class.java)).thenReturn(Mono.just(responseEntity))

        val response = assetService.saveSnippet(snippetId, mockFile)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Success", response.body)
        verify(mockWebClient).put()
        verify(mockRequestBodyUriSpec).uri("/{container}/{snippetId}", "test-container", snippetId)
    }

    @Test
    fun `test getSnippet successfully`() {
        val snippetId = "testSnippet"
        val mockFileBytes = "content".toByteArray()
        val mockRequestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*> = mock(WebClient.RequestHeadersUriSpec::class.java)

        `when`(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec)
        `when`(mockRequestHeadersUriSpec.uri("/{container}/{snippetId}", "test-container", snippetId)).thenReturn(mockRequestHeadersSpec)
        `when`(mockRequestHeadersSpec.accept(MediaType.APPLICATION_OCTET_STREAM)).thenReturn(mockRequestHeadersSpec)
        `when`(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec)
        `when`(mockResponseSpec.bodyToMono(ByteArray::class.java)).thenReturn(Mono.just(mockFileBytes))

        val response = assetService.getSnippet(snippetId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertArrayEquals(mockFileBytes, response.body!!.bytes)
        verify(mockWebClient).get()
        verify(mockRequestHeadersUriSpec).uri("/{container}/{snippetId}", "test-container", snippetId)
    }

    @Test
    fun `test saveSnippet throws exception during request building`() {
        val snippetId = "testSnippet"
        val mockFile = MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".toByteArray())

        val mockRequestBodyUriSpec: WebClient.RequestBodyUriSpec = mock(WebClient.RequestBodyUriSpec::class.java)

        `when`(mockWebClient.put()).thenReturn(mockRequestBodyUriSpec)
        `when`(mockRequestBodyUriSpec.uri("/{container}/{snippetId}", "test-container", snippetId))
            .thenThrow(RuntimeException("URI building failed"))

        val response = assetService.saveSnippet(snippetId, mockFile)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertTrue(response.body!!.contains("Failed to save snippet"))
        verify(mockWebClient).put()
    }

    @Test
    fun `test saveSnippet throws exception during request execution`() {
        val snippetId = "testSnippet"
        val mockFile = MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".toByteArray())

        val mockRequestBodyUriSpec: WebClient.RequestBodyUriSpec = mock(WebClient.RequestBodyUriSpec::class.java)

        `when`(mockWebClient.put()).thenReturn(mockRequestBodyUriSpec)
        `when`(mockRequestBodyUriSpec.uri("/{container}/{snippetId}", "test-container", snippetId))
            .thenReturn(mockRequestBodySpec)
        `when`(mockRequestBodySpec.header("accept", "*/*")).thenReturn(mockRequestBodySpec)
        `when`(mockRequestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(mockRequestBodySpec)
        `when`(mockRequestBodySpec.bodyValue(mockFile.bytes))
            .thenThrow(RuntimeException("Request execution failed"))

        val response = assetService.saveSnippet(snippetId, mockFile)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertTrue(response.body!!.contains("Failed to save snippet"))
        verify(mockWebClient).put()
    }

    @Test
    fun `test getSnippet throws exception during URI building`() {
        val snippetId = "testSnippet"
        val mockRequestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*> = mock(WebClient.RequestHeadersUriSpec::class.java)

        `when`(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec)
        `when`(mockRequestHeadersUriSpec.uri("/{container}/{snippetId}", "test-container", snippetId))
            .thenThrow(RuntimeException("URI building failed"))

        val response = assetService.getSnippet(snippetId)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNull(response.body)
        verify(mockWebClient).get()
    }

    @Test
    fun `test getSnippet throws exception during request execution`() {
        val snippetId = "testSnippet"
        val mockRequestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*> = mock(WebClient.RequestHeadersUriSpec::class.java)

        `when`(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec)
        `when`(mockRequestHeadersUriSpec.uri("/{container}/{snippetId}", "test-container", snippetId))
            .thenReturn(mockRequestHeadersSpec)
        `when`(mockRequestHeadersSpec.accept(MediaType.APPLICATION_OCTET_STREAM)).thenReturn(mockRequestHeadersSpec)
        `when`(mockRequestHeadersSpec.retrieve())
            .thenThrow(RuntimeException("Request execution failed"))

        val response = assetService.getSnippet(snippetId)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNull(response.body)
        verify(mockWebClient).get()
    }



}
