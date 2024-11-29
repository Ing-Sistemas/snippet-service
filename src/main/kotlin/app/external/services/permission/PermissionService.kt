package com.example.springboot.app.external.services.permission

import com.example.springboot.app.external.services.permission.request.PermissionRequest
import com.example.springboot.app.external.services.permission.request.PermissionShare
import com.example.springboot.app.external.services.permission.response.PermissionResponse
import com.example.springboot.app.snippets.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

@Service
class PermissionService @Autowired constructor(
    private val restTemplate: RestTemplate,
    private val snippetService: SnippetService
) {
    @Value("\${spring.constants.permission_url}") private lateinit var permissionURL: String

    fun hasPermissionByTitle(
        permission: String,
        snippetTitle: String,
        headers: HttpHeaders
    ): Boolean {
        val snippetId = snippetService.findSnippetByTitle(snippetTitle).id
        val url = "$permissionURL?snippetId=$snippetId"
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            PermissionResponse::class.java
        )
        val permissions = response.body?.permissions ?: return false
        return permissions.map { it.name }.contains(permission)
    }

    fun hasPermissionBySnippetId(
        permission: String,
        snippetId: String,
        headers: HttpHeaders
    ): Boolean {
        val url = "$permissionURL?snippetId=$snippetId"
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            PermissionResponse::class.java
        )
        val permissions = response.body?.permissions ?: return false
        return permissions.map { it.name }.contains(permission)
    }

    fun deleteFromPermission(
        snippetId: String,
        headers: HttpHeaders
    ): Int {
        val url = "$permissionURL/delete"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val res = restTemplate.postForEntity(url, requestPermEntity, Int::class.java)
        return res.body!!
    }

    fun createPermission(
        snippetId: String,
        headers: HttpHeaders
    ) {
        val url = "$permissionURL/create"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val response = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to create permissions")
        }
    }

    fun shareSnippet(
        snippetId: String,
        friendId: String,
        headers: HttpHeaders
    ): PermissionResponse {
        val url = "$permissionURL/share"
        val shareRequest = HttpEntity(PermissionShare(snippetId, friendId), headers)
        val response = restTemplate.postForEntity(url, shareRequest, PermissionResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to share snippet")
        }
        return response.body!!
    }

    fun getAllSnippetsIdsForUser(
        headers: HttpHeaders
    ): List<String> {
        val url = "$permissionURL/get_all"

        val response = restTemplate.exchange(
            url, HttpMethod.GET,
            HttpEntity<Any>(headers),
            object : ParameterizedTypeReference<List<String>>() {}
        )

        println("perm response: ${response.body}")
        if (response.statusCode.is4xxClientError) {
            throw Exception("Failed to get all snippets")
        } else if (response.statusCode.is5xxServerError) {
            throw Exception("Server error")
        } else {
            return response.body!!
        }
    }
}