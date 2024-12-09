package com.example.springboot.app.external.redis.events

import com.example.springboot.app.rules.dto.RuleDTO
import org.springframework.security.oauth2.jwt.Jwt

data class LintEvent(
    val snippetId: String,
    val jwt: Jwt,
    val rules: List<RuleDTO>
): Event