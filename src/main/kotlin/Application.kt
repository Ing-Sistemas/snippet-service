package com.example.springboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnippetServiceApplication

fun main(args: Array<String>) {
	runApplication<SnippetServiceApplication>(*args)
}
