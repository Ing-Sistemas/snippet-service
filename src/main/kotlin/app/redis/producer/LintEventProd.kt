package com.example.springboot.app.redis.producer

import com.example.springboot.app.redis.events.FormatEvent
import com.example.springboot.app.redis.events.LintEvent
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

interface LintEventProd {
    suspend fun publish(event: LintEvent)
}

@Component
class LintEventProducer(
    @Value("\${stream.key.linter}") streamKey: String,
    redis: ReactiveRedisTemplate<String, String>
) : LintEventProd, RedisStreamProducer(streamKey, redis) {

    override suspend fun publish(event: LintEvent) {
        println("Publishing on stream: $streamKey")
        emit(event).awaitSingle()
    }
}