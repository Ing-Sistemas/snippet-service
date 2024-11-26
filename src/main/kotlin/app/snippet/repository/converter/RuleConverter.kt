package com.example.springboot.app.snippet.repository.converter

import com.example.springboot.app.rule.Rule
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class RuleListConverter : AttributeConverter<List<Rule>, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Rule>?): String {
        return attribute?.let { objectMapper.writeValueAsString(it) } ?: "[]"
    }

    override fun convertToEntityAttribute(dbData: String?): List<Rule> {
        return if (dbData!!.isNotEmpty()) {
            objectMapper.readValue(dbData, object : TypeReference<List<Rule>>() {})
        } else {
            emptyList()
        }
    }
}