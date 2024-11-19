package com.example.springboot.app.utils

enum class TestCaseResult {
    SUCCESS,
    FAIL;

    override fun toString(): String {
        return name.lowercase()
    }
}