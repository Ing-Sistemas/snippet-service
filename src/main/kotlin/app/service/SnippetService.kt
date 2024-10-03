package app.service

import app.model.Snippet
import app.repository.SnippetRepository

class SnippetService(private val snippetRepository: SnippetRepository) {

    fun addSnippet(snippet: Snippet): Snippet {
        return snippetRepository.save(snippet)
    }

}