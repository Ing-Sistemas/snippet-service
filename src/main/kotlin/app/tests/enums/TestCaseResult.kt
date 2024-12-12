package com.example.springboot.app.tests.enums

enum class TestCaseResult {
    SUCCESS,
    FAIL,
    ;

    override fun toString(): String {
        return name.lowercase()
    }
}
