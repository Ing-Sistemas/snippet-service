package app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnippetSearcherApplication

fun main(args: Array<String>) {
	runApplication<SnippetSearcherApplication>(*args)
}