package com.example.springboot.app.external.redis.events

import com.example.springboot.app.rules.model.dto.CompleteRuleDTO
import com.example.springboot.app.rules.model.dto.RuleDTO
import org.springframework.security.oauth2.jwt.Jwt

data class LintEvent(
    val snippetId: String,
    val jwt: Jwt,
    val rules: List<CompleteRuleDTO>
): Event