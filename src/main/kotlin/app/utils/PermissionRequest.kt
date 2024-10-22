package com.example.springboot.app.utils

import org.springframework.security.oauth2.jwt.Jwt

data class PermissionRequest(
    val snippetId: String,
    val jwt: Jwt,
)