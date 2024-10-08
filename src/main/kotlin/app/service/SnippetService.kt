package app.service

import app.model.Snippet
import app.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class SnippetService(
    private val webClient: WebClient,
    @Value("\${external.service.permission-url}") private val permissionUrl: String,
) {
    private val snippetRepository = SnippetRepository()

    fun addSnippet(snippet: Snippet): Mono<Snippet> {
        // pegarle al permission y ver si ese snippet Id tiene permisos para tal usuario, si ya exisite
        return Mono.just(snippetRepository.save(snippet)) // fix
    }

    fun pingPermissionService(): Mono<String> {
        val endpoint = "/ping"
        val url = "$permissionUrl$endpoint"
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
    }

}