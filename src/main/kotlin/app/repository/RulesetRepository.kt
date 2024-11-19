package com.example.springboot.app.repository

import com.example.springboot.app.repository.entity.Ruleset
import com.example.springboot.app.repository.entity.RulesetType
import org.springframework.data.jpa.repository.JpaRepository

interface RulesetRepository: JpaRepository<Ruleset, String> {
    fun findByUserIdAndType(userId: String, type: RulesetType): Ruleset?
    fun existsByUserIdAndType(userId: String, type: RulesetType): Boolean
}