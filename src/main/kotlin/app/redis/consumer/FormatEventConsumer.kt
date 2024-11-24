package com.example.springboot.app.redis.consumer

import com.example.springboot.app.external.printscript.PrintScriptService
import com.example.springboot.app.redis.events.FormatEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class FormatEventConsumer @Autowired constructor(
    redis: ReactiveRedisTemplate<String, String>,
    @Value("\${stream.key.formatter}") streamKey: String,
    @Value("\${groups.formatter}") groupId: String,
    private val printScriptService: PrintScriptService
) : RedisStreamConsumer<FormatEvent>(streamKey, groupId, redis){

    private val logger = LoggerFactory.getLogger(FormatEventConsumer::class.java)

    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, FormatEvent>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofMillis(10000))// poll :frequency at which a client checks for new messages
            .targetType(FormatEvent::class.java)
            .build()
    }

    override fun onMessage(record: ObjectRecord<String, FormatEvent>) {
        Thread.sleep(1000 * 10)
        logger.info("Id: ${record.id}, Value: ${record.value}, Stream: ${record.stream}, Group: $groupId")
        //externalService.format(record.value, )
    }
}