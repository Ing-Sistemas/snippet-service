package app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SnippetController {

    @GetMapping("/ping")
    fun getHello(): ResponseEntity<String> {
        return ResponseEntity.ok("Greetings from Spring Boot, this is snippet service!")
    }
}