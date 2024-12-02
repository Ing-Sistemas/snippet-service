package com.example.springboot.app.external.services.printscript.request

import com.example.springboot.app.rules.dto.RuleDTO


data class FormatRequest(
    val snippetId: String,
    val userId: String,
    val rules: List<RuleDTO>
)