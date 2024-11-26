package app.snippet.repository

import app.snippet.repository.entity.RulesetEntity
import app.snippet.repository.entity.RulesetType
import org.springframework.data.jpa.repository.JpaRepository

interface RulesetRepository: JpaRepository<RulesetEntity, String> {
    fun findByUserIdAndType(userId: String, type: RulesetType): RulesetEntity?
    fun existsByUserIdAndType(userId: String, type: RulesetType): Boolean
}