package com.example.springboot.app.external.request

import com.example.springboot.app.utils.FormatRule


data class FormatRequest(
    val snippetId: String,
    val rule: FormatRule
)