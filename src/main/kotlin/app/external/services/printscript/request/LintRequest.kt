package com.example.springboot.app.external.services.printscript.request

import com.example.springboot.app.rules.model.dto.CompleteRuleDTO

data class LintRequest(
    val snippetId: String,
    val rules: List<CompleteRuleDTO>
)