package com.example.springboot.app.snippets

import com.example.springboot.app.rules.enums.SnippetStatus
import com.example.springboot.app.snippets.dto.SnippetDTO
import com.example.springboot.app.utils.UserUtils
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.any
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SnippetServiceTest {
    @Mock
    private lateinit var snippetRepository: SnippetRepository

    @Mock
    private lateinit var userUtils: UserUtils

    @InjectMocks
    private lateinit var snippetService: SnippetService

    @Test
    fun `should create snippet successfully`() {
        val snippetDTO = SnippetDTO("1", "TestSnippet", "Kotlin", ".kt", "1.0", SnippetStatus.PENDING)
        val snippetEntity = SnippetEntity("1", "TestSnippet", ".kt", "Kotlin", "1.0")

        `when`(snippetRepository.save(any(SnippetEntity::class.java))).thenReturn(snippetEntity)

        val result = snippetService.createSnippet(snippetDTO)

        assertNotNull(result)
        assertEquals(snippetDTO.id, result.id)
        assertEquals(snippetDTO.title, result.title)
        verify(snippetRepository).save(any(SnippetEntity::class.java))
    }

    @Test
    fun `should delete snippet successfully`() {
        val snippetId = "1"

        doNothing().`when`(snippetRepository).deleteById(snippetId)

        snippetService.deleteSnippet(snippetId)

        verify(snippetRepository).deleteById(snippetId)
    }

    @Test
    fun `should find snippet by id successfully`() {
        val snippetEntity = SnippetEntity("1", "TestSnippet", ".kt", "Kotlin", "1.0")
        `when`(snippetRepository.findSnippetEntityById("1")).thenReturn(snippetEntity)

        val result = snippetService.findSnippetById("1")

        assertNotNull(result)
        assertEquals(snippetEntity.id, result.id)
        assertEquals(snippetEntity.title, result.title)
        verify(snippetRepository).findSnippetEntityById("1")
    }

    @Test
    fun `should find snippet by title successfully`() {
        val snippetEntity = SnippetEntity("1", "TestSnippet", ".kt", "Kotlin", "1.0")
        `when`(snippetRepository.findSnippetEntityByTitle("TestSnippet")).thenReturn(snippetEntity)

        val result = snippetService.findSnippetByTitle("TestSnippet")

        assertNotNull(result)
        assertEquals(snippetEntity.id, result.id)
        assertEquals(snippetEntity.title, result.title)
        verify(snippetRepository).findSnippetEntityByTitle("TestSnippet")
    }

    @Test
    fun `should change snippet status successfully`() {
        val snippetEntity = SnippetEntity("1", "TestSnippet", ".kt", "Kotlin", "1.0")

        `when`(snippetRepository.findSnippetEntityById("1")).thenReturn(snippetEntity)

        snippetService.changeSnippetStatus("1", SnippetStatus.COMPLIANT)

        assertEquals(SnippetStatus.COMPLIANT, snippetEntity.status)
        verify(snippetRepository).findSnippetEntityById("1")
        verify(snippetRepository).save(snippetEntity)
    }
}
