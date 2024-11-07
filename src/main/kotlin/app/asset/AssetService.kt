package com.example.springboot.app.asset

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AssetService
    @Autowired
    constructor(
        private val restTemplate: RestTemplate,
        @Value("\${asset_url")
        private val bucketUrl: String
    ) {
        fun saveSnippet(snippetId: String, code: String): ResponseEntity<String> {
            try {
                val request = HttpEntity(code, HttpHeaders())
                restTemplate.put("$bucketUrl/$snippetId", request)
                return ResponseEntity.ok(null)
            } catch (e: Exception) {
                return ResponseEntity.badRequest().build()
            }
        }

        fun getSnippet(snippetId: String): ResponseEntity<String> {
            return ResponseEntity.ok(
                restTemplate.getForEntity(
                    "$bucketUrl/$snippetId", String::class.java
                ).body!!
            )
        }
    }