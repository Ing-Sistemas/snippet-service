package com.example.springboot.app.external.rest

import com.example.springboot.app.external.rest.request.PSRequest
import com.example.springboot.app.external.rest.request.PermissionRequest
import com.example.springboot.app.external.rest.request.PermissionShare
import com.example.springboot.app.external.rest.response.PSValResponse
import com.example.springboot.app.external.rest.response.PermissionResponse
import com.example.springboot.app.service.SnippetService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class ExternalService(
    private val restTemplate: RestTemplate,
    private val snippetService: SnippetService,
    @Value("\${permission_url}")
    private val permUrl: String,
    @Value("\${print_script_url}")
    private val psUrl: String,
    @Value("\${asset_url")
    private val assetUrl: String
) {
    fun hasPermission(
        permission: String,
        snippetTitle: String,
        headers: HttpHeaders
    ): Boolean {
        val url = "$permUrl/get"
        val snippetId = snippetService.findSnippetByTitle(snippetTitle).snippetId
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val response = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
        return response.body!!.permissions.contains(permission)
    }

    fun createPermissions(
        snippetId: String,
        headers: HttpHeaders
    ) {
        val url = "$permUrl/create"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val resPermission = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
        if (resPermission.body == null) {
            throw Exception("Failed to create permissions")
        }
    }

    fun validateSnippet(
        snippetId: String,
        version: String,
        headers: HttpHeaders
    ): ResponseEntity<PSValResponse> {
        val url = "$psUrl/validate"
        val requestPSEntity = HttpEntity(PSRequest(version, snippetId), headers)
        println("before")
        val resPrintScript = restTemplate.postForEntity(url, requestPSEntity, PSValResponse::class.java)
        println(resPrintScript.statusCode.is4xxClientError)
        println(resPrintScript.statusCode.is5xxServerError)
        return when {
            resPrintScript.statusCode.is4xxClientError -> ResponseEntity.status(400).body(resPrintScript.body)
            resPrintScript.statusCode.is5xxServerError -> throw Exception("Failed to validate snippet in service")
            else -> resPrintScript
        }
    }

    fun shareSnippet(
        snippetTitle: String,
        friendId: String,
        headers: HttpHeaders
    ): ResponseEntity<PermissionResponse> {
        val url = "$permUrl/share"
        val shareRequest = HttpEntity(PermissionShare(snippetService.findSnippetByTitle(snippetTitle).snippetId, friendId), headers)
        val response = restTemplate.postForEntity(url, shareRequest, PermissionResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to share snippet")
        }
        return response
    }
}