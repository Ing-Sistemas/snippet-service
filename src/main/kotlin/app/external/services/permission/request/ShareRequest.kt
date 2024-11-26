package com.example.springboot.app.external.services.permission.request

data class ShareRequest(
    val snippetId: String,
    val userId: String
)