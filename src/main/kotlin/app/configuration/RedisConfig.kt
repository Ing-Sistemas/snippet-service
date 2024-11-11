package com.example.springboot.app.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class ConnectionFactory(
    @Value("\${spring.data.redis.host}") private val hostName: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
    private val objectMapper: ObjectMapper,//FIXME: Could not autowire. No beans of 'ObjectMapper' type found.
) {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration(hostName, port)
        )
    }

    @Bean
    fun reactiveRedisTemplate(connectionFactory: LettuceConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val typeFactory: TypeFactory = objectMapper.typeFactory
        val javaType = typeFactory.constructType(Any::class.java)

        // Configure Jackson2JsonRedisSerializer with TypeFactory for handling Java types
        val valueSerializer = Jackson2JsonRedisSerializer<Any>(javaType)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>(StringRedisSerializer())
            .value(valueSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}
