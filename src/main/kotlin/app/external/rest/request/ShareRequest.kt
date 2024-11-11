package com.example.springboot.app.external.rest.request

data class ShareRequest(
    val snippetId: String,
    val userId: String
)