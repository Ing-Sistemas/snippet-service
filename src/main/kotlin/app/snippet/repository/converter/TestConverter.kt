//package com.example.springboot.app.repository.converter
//
//import com.example.springboot.app.testing.TestCase
//import com.fasterxml.jackson.core.type.TypeReference
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import jakarta.persistence.AttributeConverter
//import jakarta.persistence.Converter
//
//@Converter
//class TestConverter : AttributeConverter<List<TestCase>, String> {
//    private val objectMapper = jacksonObjectMapper()
//
//    override fun convertToDatabaseColumn(attribute: List<TestCase>?): String? {
//        if (attribute == null) {
//            return null
//        }
//        return objectMapper.writeValueAsString(attribute) ?: "[]"
//    }
//
//    override fun convertToEntityAttribute(dbData: String?): List<TestCase> {
//        return if (!dbData.isNullOrEmpty()) {
//            objectMapper.readValue(dbData, object : TypeReference<List<TestCase>>() {})
//        } else {
//            emptyList()
//        }
//    }
//}
