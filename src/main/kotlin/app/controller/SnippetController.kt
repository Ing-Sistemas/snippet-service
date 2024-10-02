package app.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SnippetController {

    @GetMapping("/")
    fun getHello(): String {
        return "Greetings from Spring Boot, this is snippet service!"
    }
}