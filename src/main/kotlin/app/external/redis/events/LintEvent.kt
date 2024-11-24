package com.example.springboot.app.external.redis.events

import com.example.springboot.app.utils.LintRule

data class LintEvent(
    val snippetId: String,
    val rule: LintRule,
    val userId: String
): Event