package com.example.springboot.app.repository

import com.example.springboot.app.repository.entity.RulesetEntity
import com.example.springboot.app.repository.entity.RulesetType
import org.springframework.data.jpa.repository.JpaRepository

interface RulesetRepository: JpaRepository<RulesetEntity, String> {
    fun findByUserIdAndType(userId: String, type: RulesetType): RulesetEntity?
    fun existsByUserIdAndType(userId: String, type: RulesetType): Boolean
}