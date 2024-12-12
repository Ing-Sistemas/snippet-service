package com.example.springboot.app.utils

import com.example.springboot.app.external.services.permission.response.PermissionResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CorrelationService @Autowired constructor(
    private val restTemplate: RestTemplate,
) {

    @Value("\${spring.constants.permission_url}") private lateinit var permissionURL: String

    @Value("\${spring.constants.print_script_url}") private lateinit var psUrl: String


    fun correlatePermission(cId: String, headers: HttpHeaders) {
        val url = "$permissionURL/correlate/$cId"
        restTemplate.exchange(
            url,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            Void::class.java
        )
    }

    fun correlatePs(cId: String, headers: HttpHeaders) {
        val url = "$psUrl/correlate/$cId"
        restTemplate.exchange(
            url,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            Void::class.java
        )
    }
}