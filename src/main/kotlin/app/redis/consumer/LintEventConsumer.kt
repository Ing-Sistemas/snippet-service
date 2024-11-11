package com.example.springboot.app.redis.consumer

import com.example.springboot.app.external.rest.ExternalService
import com.example.springboot.app.redis.events.LintEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LintEventConsumer @Autowired constructor(
    redis: ReactiveRedisTemplate<String, String>,
    @Value("\${stream.key.linter}") streamKey: String,
    @Value("\${groups.linter}") groupId: String,
    private val externalService: ExternalService
) : RedisStreamConsumer<LintEvent>(streamKey, groupId, redis){
    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintEvent>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofMillis(10000))// poll :frequency at which a client checks for new messages
            .targetType(LintEvent::class.java)
            .build()
    }
    override fun onMessage(record: ObjectRecord<String, LintEvent>) {
        Thread.sleep(1000 * 10)
        println("Id: ${record.id}, Value: ${record.value}, Stream: ${record.stream}, Group: $groupId")
        //externalService.lint(record.value, )
    }
}