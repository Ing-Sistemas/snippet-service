package app.service

import app.repository.entity.Snippet
import app.repository.SnippetRepository

class SnippetService(private val snippetRepository: SnippetRepository) {

    fun addSnippet(snippet: Snippet): Snippet {
        return snippetRepository.save(snippet)
    }

}