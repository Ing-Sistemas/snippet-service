package com.example.springboot.app.utils

import com.example.springboot.app.external.ui.SnippetData


data class Pagination(
    val page: Int,
    val pageSize: Int,
    val count: Int
)

data class User(
    val id: String,
    val name: String
)

data class PaginatedUsers(
    val pagination: Pagination,
    val users: List<User>
)

data class PaginatedSnippets(
    val pagination: Pagination,
    val snippets: List<SnippetData>
)