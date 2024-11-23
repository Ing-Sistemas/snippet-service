package com.example.springboot.app.redis.events

import com.example.springboot.app.utils.FormatRule

data class FormatEvent(
    val snippetId: String,
    val userId: String,
    val rule: FormatRule,
): Event