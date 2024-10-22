package com.example.springboot.app.utils

import org.springframework.security.oauth2.jwt.Jwt

data class PermissionRequest(
    val snippetId: Long,
    val jwt: Jwt,
)