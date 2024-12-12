package com.example.springboot.app.external.redis.consumer

import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.external.redis.events.LintEvent
import com.example.springboot.app.external.services.printscript.LanguageService
import com.example.springboot.app.snippets.SnippetService
import com.example.springboot.app.utils.CodingLanguage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration
import org.springframework.context.annotation.Lazy
import java.util.*

@Component
class LintEventConsumer @Autowired constructor(
    redis: ReactiveRedisTemplate<String, String>,
    @Value("\${stream.key.linter}") streamKey: String,
    @Value("\${groups.linter}") groupId: String,
    @Lazy private val languageService: Map<CodingLanguage, LanguageService>,
    @Lazy private val snippetService: SnippetService
) : RedisStreamConsumer<LintEvent>(streamKey, groupId, redis){


    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintEvent>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofMillis(10000))// poll :frequency at which a client checks for new messages
            .targetType(LintEvent::class.java)
            .build()
    }

    override fun onMessage(record: ObjectRecord<String, LintEvent>) {
        Thread.sleep(1000 * 10)
        val eventValue = record.value
        val snippet = snippetService.findSnippetById(eventValue.snippetId)
        println("Linting snippet ${snippet.language.uppercase(Locale.getDefault())}")
        languageService[CodingLanguage.valueOf(snippet.language.uppercase(Locale.getDefault()))]!!.autoLint(eventValue.snippetId, eventValue.jwt, eventValue.rules)
    }
}