package com.example.springboot.app.utils

data class PermissionResponse(
    val snippetId: String,
    val userId: String,
    val permissions: List<String>
)