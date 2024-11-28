package com.example.springboot.app.snippet.repository

import RuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RuleRepository: JpaRepository<RuleEntity, String> {
    fun findRuleById(id: String): RuleEntity
}