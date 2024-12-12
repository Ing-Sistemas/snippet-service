package com.example.springboot.app.utils

import com.example.springboot.app.snippets.dto.SnippetDataUi

data class Pagination(
    val page: Int,
    val page_size: Int,
    val count: Int,
)

data class User(
    val name: String,
    val id: String,
)

data class PaginatedUsers(
    val page: Int,
    val page_size: Int,
    val count: Int,
    val users: List<User>,
)

data class PaginatedSnippets(
    val pagination: Pagination,
    val snippets: List<SnippetDataUi>,
)
