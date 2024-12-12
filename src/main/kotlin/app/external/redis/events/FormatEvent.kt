package com.example.springboot.app.external.redis.events

import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import org.springframework.security.oauth2.jwt.Jwt

data class FormatEvent(
    val snippetId: String,
    val jwt: Jwt,
    val rules: List<CompleteRuleDTO>,
) : Event
