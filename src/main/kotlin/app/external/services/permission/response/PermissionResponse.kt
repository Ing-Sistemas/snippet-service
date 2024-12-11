package com.example.springboot.app.external.services.permission.response

data class PermissionResponse(
    val snippetId: String,
    val userId: String,
    val permissions: Set<PermissionType>
)

enum class PermissionType {
    READ, WRITE, EXECUTE, SHARE
}