package com.example.springboot.app.rules.repository

import com.example.springboot.app.rules.enums.RulesetType
import com.example.springboot.app.rules.dto.RuleDTO
import com.example.springboot.app.rules.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RuleRepository: JpaRepository<Rule, String> {
    fun findRuleById(id: String): Rule

    @Query(
        """
        SELECT new com.example.springboot.app.rules.dto.RuleDTO(
            r.id,
            r.name,
            COALESCE(ur.isActive, false),
            COALESCE(r.value, NULL),
            r.type
        )
        FROM Rule r
        LEFT JOIN r.userRules ur ON r.id = ur.rule.id AND ur.userId = :userId
        WHERE r.type = :ruleType
    """,
        nativeQuery = false,
    )
    fun findAllRulesByUserIdAndType(userId: String, ruleType: RulesetType): List<RuleDTO>
}