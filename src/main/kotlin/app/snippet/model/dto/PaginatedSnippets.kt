package com.example.springboot.app.snippet.model.dto

import com.example.springboot.app.external.rest.ui.SnippetData

data class PaginatedSnippets(
    val snippets: List<SnippetData>, // Lista de snippets en la página actual
    val totalItems: Int,         // Número total de snippets disponibles
    val totalPages: Int,         // Número total de páginas
    val currentPage: Int         // Número de la página actual
)
