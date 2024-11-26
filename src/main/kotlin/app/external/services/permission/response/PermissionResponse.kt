package com.example.springboot.app.external.services.permission.response

import java.io.Serializable

data class PermissionResponse(
    val snippetId: String,
    val userId: String,
    val permissions: Set<Serializable>
)