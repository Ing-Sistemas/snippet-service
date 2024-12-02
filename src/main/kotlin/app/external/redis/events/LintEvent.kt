package com.example.springboot.app.external.redis.events

import com.example.springboot.app.rules.dto.RuleDTO

data class LintEvent(
    val snippetId: String,
    val userId: String,
    val rules: List<RuleDTO>
): Event