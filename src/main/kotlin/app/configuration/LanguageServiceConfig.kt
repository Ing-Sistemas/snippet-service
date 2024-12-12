package com.example.springboot.app.configuration

import com.example.springboot.app.external.services.printscript.LanguageService
import com.example.springboot.app.external.services.printscript.PrintScriptService
import com.example.springboot.app.utils.CodingLanguage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LanguageServiceConfig (
    private val printScriptService: PrintScriptService
){
    @Bean
    fun languageMap(): Map<CodingLanguage, LanguageService> {
        return mapOf(
            CodingLanguage.PRINTSCRIPT to printScriptService
        )
    }
}