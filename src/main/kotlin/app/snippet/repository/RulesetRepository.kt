package com.example.springboot.app.snippet.repository

import com.example.springboot.app.snippet.repository.entity.RulesetEntity
import com.example.springboot.app.snippet.repository.entity.RulesetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RulesetRepository: JpaRepository<RulesetEntity, String> {
    fun findByUserIdAndType(userId: String, type: RulesetType): RulesetEntity?
    fun existsByUserIdAndType(userId: String, type: RulesetType): Boolean
}