package com.example.springboot.app.external.redis.events

import com.example.springboot.app.rules.FormatRule

data class FormatEvent(
    val snippetId: String,
    val userId: String,
    val rule: FormatRule,
): Event