package com.example.springboot.app.external.services.printscript.request

import com.example.springboot.app.rules.model.dto.CompleteRuleDTO


data class FormatRequest(
    val snippetId: String,
    val config: List<CompleteRuleDTO>
)