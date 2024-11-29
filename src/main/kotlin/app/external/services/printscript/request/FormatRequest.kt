package com.example.springboot.app.external.services.printscript.request

import com.example.springboot.app.rules.FormatRule


data class FormatRequest(
    val snippetId: String,
    val rule: FormatRule
)