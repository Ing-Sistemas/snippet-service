package com.example.springboot.app.utils

data class FormatConfig (
    val spaceBeforeColon: Boolean,
    val spaceAfterColon: Boolean,
    val spaceAroundEquals: Boolean,
    val lineJumpBeforePrintln: Int,
    val lineJumpAfterSemicolon: Boolean = true,
    val singleSpaceBetweenTokens: Boolean = true,
    val spaceAroundOperators: Boolean = true,
)