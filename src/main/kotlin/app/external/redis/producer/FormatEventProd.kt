package com.example.springboot.app.external.redis.producer

import com.example.springboot.app.external.redis.events.FormatEvent
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

interface FormatEventProd {
    suspend fun publish(event: FormatEvent)
}

@Component
class FormatEventProducer(
    @Value("\${stream.key.formatter}") streamKey: String,
    redis: ReactiveRedisTemplate<String, String>,
) : FormatEventProd, RedisStreamProducer(streamKey, redis) {
    override suspend fun publish(event: FormatEvent) {
        emit(event).awaitSingle()
    }
}
