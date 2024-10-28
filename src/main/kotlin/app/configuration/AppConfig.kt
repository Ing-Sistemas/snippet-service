package com.example.springboot.app.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Configuration
    class RestTemplateConfig {

        @Bean
        fun restTemplate(): RestTemplate {
            val restTemplate = RestTemplate()
            restTemplate.errorHandler = object : ResponseErrorHandler {
                override fun hasError(response: ClientHttpResponse): Boolean {
                    return response.statusCode.is4xxClientError || response.statusCode.is5xxServerError
                }

                override fun handleError(response: ClientHttpResponse) {
                }
            }
            return restTemplate
        }
    }
}