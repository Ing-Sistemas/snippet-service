package com.example.springboot.app.snippet.repository

import com.example.springboot.app.snippet.repository.entity.RulesUserEntity
import com.example.springboot.app.snippet.repository.entity.RulesetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RuleUserRepository: JpaRepository<RulesUserEntity, String> {
    fun findByUserIdAndType(userId: String, type: RulesetType): RulesUserEntity?
    fun existsByUserIdAndType(userId: String, type: RulesetType): Boolean
}