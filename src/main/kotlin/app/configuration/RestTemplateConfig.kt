package com.example.springboot.app.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule() // Registrar módulo de Kotlin para soporte de data classes

        val messageConverter = MappingJackson2HttpMessageConverter(objectMapper)
        val restTemplate = RestTemplate(listOf(messageConverter))

        restTemplate.requestFactory = clientHttpRequestFactory()
        restTemplate.errorHandler =
            object : ResponseErrorHandler {
                // this avoids the default behavior of RestTemplate to throw exceptions for HTTP error status codes.
                override fun hasError(response: ClientHttpResponse): Boolean {
                    return response.statusCode.is4xxClientError || response.statusCode.is5xxServerError
                }

                override fun handleError(response: ClientHttpResponse) {
                }
            }
        return restTemplate
    }

    private fun clientHttpRequestFactory(): ClientHttpRequestFactory {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(10000)
        factory.setReadTimeout(10000)
        return factory
    }
}
