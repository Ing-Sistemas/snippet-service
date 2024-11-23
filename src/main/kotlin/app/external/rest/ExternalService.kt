package com.example.springboot.app.external.rest

import com.example.springboot.app.external.rest.request.FormatRequest
import com.example.springboot.app.external.rest.request.PSRequest
import com.example.springboot.app.external.rest.request.PermissionRequest
import com.example.springboot.app.external.rest.request.PermissionShare
import com.example.springboot.app.external.rest.response.PSResponse
import com.example.springboot.app.external.rest.response.PSValResponse
import com.example.springboot.app.external.rest.response.PermissionResponse
import com.example.springboot.app.external.rest.ui.SnippetsGroup
import com.example.springboot.app.service.SnippetService
import com.example.springboot.app.utils.FormatRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ExternalService @Autowired constructor(
    private val restTemplate: RestTemplate,
    private val snippetService: SnippetService,
) {
    @Value("\${spring.constants.permission_url}") private lateinit var permUrl: String
    @Value("\${spring.constants.print_script_url}") private lateinit var psUrl: String

    //CHANGE, these methods shouldn't return a response entity
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

    fun hasPermissionBySnippetId(
        permission: String,
        snippetId: String,
        headers: HttpHeaders
    ): Boolean {
        val url = "$permUrl/get"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val response = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
        return response.body!!.permissions.contains(permission)
    }

    fun deleteFromPermission(
        snippetId: String,
        headers: HttpHeaders
    ) {
        val url = "$permUrl/delete"
        val requestPermEntity = HttpEntity(PermissionRequest(snippetId), headers)
        val response = restTemplate.postForEntity(url, requestPermEntity, PermissionResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to delete permission")
        }
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
        val resPrintScript = restTemplate.postForEntity(url, requestPSEntity, PSValResponse::class.java)
        return when {
            resPrintScript.statusCode.is4xxClientError -> ResponseEntity.status(400).body(resPrintScript.body)
            resPrintScript.statusCode.is5xxServerError -> throw Exception("Failed to validate snippet in service")
            else -> resPrintScript
        }
    }

    fun shareSnippet(
        snippetId: String,
        friendId: String,
        headers: HttpHeaders
    ): ResponseEntity<PermissionResponse> {
        val url = "$permUrl/share"
        val shareRequest = HttpEntity(PermissionShare(snippetId, friendId), headers)
        val response = restTemplate.postForEntity(url, shareRequest, PermissionResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to share snippet")
        }
        return response
    }

    fun getAllSnippetsIdsForUser(
        headers: HttpHeaders
    ): ResponseEntity<SnippetsGroup> {
        val url = "$permUrl/get_all"
        val response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Any>(headers), SnippetsGroup::class.java)//todo, change request to exchange
        if (response.statusCode.is4xxClientError) {
            throw Exception("Failed to get all snippets")
        } else if (response.statusCode.is5xxServerError) {
            throw Exception("Server error")
        } else {
            return response
        }
    }
    //TODO fix response type in format/lint methods, and adapt printScript methods to them (in ps service of course)

    // for sync formatting
    fun format(
        snippetId: String,
        headers: HttpHeaders
    ): ResponseEntity<PSResponse> {
        val url = "$psUrl/format"
        val requestEntity = HttpEntity(snippetId, headers)
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to format snippet")
        }
        return response
    }

    fun lint(
        snippetId: String,
        headers: HttpHeaders
    ): ResponseEntity<PSResponse> {
        val url = "$psUrl/lint"
        val requestEntity = HttpEntity(snippetId, headers)
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to lint snippet")
        }
        return response
    }

    // for async formatting
    fun autoFormat(
        snippetId: String,
        rule: FormatRule,
    ){
        val url = "$psUrl/auto_format"
        val requestEntity = HttpEntity(FormatRequest(snippetId, rule))
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to format snippet")//todo, instead of throwing, change status in asset service maybe (?)
        }
    }

    fun autoLint(
        snippetId: String,
    ){
        val url = "$psUrl/auto_lint"
        val requestEntity = HttpEntity(snippetId)
        val response = restTemplate.postForEntity(url, requestEntity, PSResponse::class.java)
        if (response.body == null) {
            throw Exception("Failed to lint snippet")//todo same as above jijiji
        }
    }
}