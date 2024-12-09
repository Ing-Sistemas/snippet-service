package com.example.springboot.app.external.services.printscript.request

import com.example.springboot.app.utils.FormatConfig


data class FormatRequest(
    val snippetId: String,
    val config: FormatConfig
)