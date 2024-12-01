package com.example.springboot.app.utils

import com.example.springboot.app.snippets.dto.SnippetDataUi


data class Pagination(
    val page: Int,
    val page_size: Int,
    val count: Int
)

data class UserDTOUI(
    val id: String,
    val name: String
)

data class PaginatedUsers(
    val pagination: Pagination,
    val usersDTOUI: List<UserDTOUI>
)

data class PaginatedSnippets(
    val pagination: Pagination,
    val snippets: List<SnippetDataUi>
)