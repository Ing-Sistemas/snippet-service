package com.example.springboot.app.external.redis.consumer

import com.example.springboot.app.external.redis.events.FormatEvent
import com.example.springboot.app.external.services.printscript.LanguageService
import com.example.springboot.app.snippets.SnippetService
import com.example.springboot.app.utils.CodingLanguage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.Locale

@Component
class FormatEventConsumer
    @Autowired
    constructor(
        redis: ReactiveRedisTemplate<String, String>,
        @Value("\${stream.key.formatter}") streamKey: String,
        @Value("\${groups.formatter}") groupId: String,
        @Lazy private val languageService: Map<CodingLanguage, LanguageService>,
        @Lazy private val snippetService: SnippetService,
    ) : RedisStreamConsumer<FormatEvent>(streamKey, groupId, redis) {
        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, FormatEvent>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(10000)) // poll :frequency at which a client checks for new messages
                .targetType(FormatEvent::class.java)
                .build()
        }

        override fun onMessage(record: ObjectRecord<String, FormatEvent>) {
            Thread.sleep(1000 * 10)
            val eventValue = record.value
            val snippet = snippetService.findSnippetById(eventValue.snippetId)
            println("Formatting snippet ${snippet.language.uppercase(Locale.getDefault())}")
            languageService[
                CodingLanguage.valueOf(
                    snippet.language.uppercase(Locale.getDefault()),
                ),
            ]!!.autoFormat(eventValue.snippetId, eventValue.jwt, eventValue.rules)
        }
    }
