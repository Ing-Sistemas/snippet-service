package com.example.springboot.app.testing

enum class TestCaseResult {
    SUCCESS,
    FAIL;

    override fun toString(): String {
        return name.lowercase()
    }
}