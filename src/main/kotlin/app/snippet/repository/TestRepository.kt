package app.snippet.repository

import com.example.springboot.app.repository.entity.TestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestRepository: JpaRepository<TestEntity, String> {
    fun findTestEntityBySnippetId(snippetId: String): TestEntity
    fun findTestEntityById(id: String): TestEntity
}