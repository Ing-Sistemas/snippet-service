package com.example.springboot.app.utils

data class PermissionResponse(
    val snippetId: Long,
    val userId: String,
    val permissions: List<String>
)