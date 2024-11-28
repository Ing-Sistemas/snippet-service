package com.example.springboot.app.rules.repository

import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.entity.RulesUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RuleUserRepository: JpaRepository<RulesUserEntity, String> {
    fun findFirstByUserIdAndRuleId(userId: String, ruleId: String): RulesUserEntity?
}