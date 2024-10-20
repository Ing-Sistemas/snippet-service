package com.example.springboot.app.controller

import com.example.springboot.app.dto.SnippetDTO
import com.example.springboot.app.repository.entity.SnippetEntity
import com.example.springboot.app.service.SnippetService
import com.example.springboot.app.utils.URLs.API_URL
import com.example.springboot.app.utils.URLs.BASE_URL
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api")
class SnippetController(
    private val snippetService: SnippetService,
    private val restTemplate: RestTemplate,
) {
    private val host = System.getenv().getOrDefault("HOST", "localhost")
    private val permissionPort = System.getenv().getOrDefault("PERMISSION_SERVICE_PORT", "none")

    @PostMapping("/create")
    fun save(
        @RequestParam userId:String,
        @RequestParam title: String,
        @RequestParam language:String,
        @RequestParam code:String
    ): ResponseEntity<SnippetEntity> {
        //send userId to Perm service and save the snippet to its table (with owner perms)
        val snippetDTO = SnippetDTO(null, title, language)
        val savedSnippet = snippetService.createSnippet(snippetDTO)
        val permURL = "$BASE_URL$host:$permissionPort/$API_URL/save"
        val assetURL = "$BASE_URL$host:nose/"
        try {
            val response = restTemplate.put(permURL, userId, savedSnippet.id)
            val assetResponse = restTemplate.put(assetURL, code, savedSnippet.id)
            //first, create the perms in the db
            //then, create the snippet file bucket (the asset receives the title as key, but it would be better to save it
            // with the snippet_id)
            return ResponseEntity.ok(savedSnippet)
        } catch (e: Exception){
            println(e.message)
            return ResponseEntity.status(500).body(savedSnippet)
        }
    }

    @PutMapping("/update")
    fun update(@RequestBody userId:String, @RequestBody snippetId: Long, @RequestBody title: String): ResponseEntity<String> {
        // this sends the userId to the Perm service, check if the user can w, and then send the update
        // to the asset service
        return ResponseEntity.ok("Snippet updated")
    }

    @GetMapping("/get")
    fun get(@RequestBody userId:String, @RequestBody snippetId: Long): ResponseEntity<SnippetEntity> {
        val snippet = snippetService.findSnippetById(snippetId)
        val permURL = "$BASE_URL$host:$permissionPort/$API_URL/get"
        //check if the user can read the snippet
        return ResponseEntity.ok(snippet)
    }

    @DeleteMapping("/delete")
    fun delete(@RequestBody userId:String, @RequestBody snippetId: String){
        // this sends the userId to the Perm service, check if the user can delete and if so
        // the Perm deletes the snippet from its db, the asset deletes de file
        // and the SnippetS deletes the snippet from its db
        snippetService.deleteSnippet(snippetId)
    }
}