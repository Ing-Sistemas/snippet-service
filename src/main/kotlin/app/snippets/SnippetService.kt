package com.example.springboot.app.snippets

import com.example.springboot.app.snippets.ControllerUtils.generateHeaders
import com.example.springboot.app.snippets.ControllerUtils.getUserIdFromJWT
import com.example.springboot.app.snippets.dto.SnippetDTO
import com.example.springboot.app.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class SnippetService @Autowired constructor(
    private val snippetRepository: SnippetRepository,
    private val userUtils: UserUtils,
) {
    private val logger = LoggerFactory.getLogger(SnippetService::class.java)

    fun createSnippet(
        snippetDTO: SnippetDTO
    ): SnippetEntity {
        return  snippetRepository.save(translate(snippetDTO))
    }

    fun deleteSnippet(snippetId: String){
        return snippetRepository.deleteById(snippetId)
    }

    fun findSnippetById(id: String): SnippetDTO {
        return translate(snippetRepository.findSnippetEntityById(id))
    }

    fun findSnippetByTitle(title: String): SnippetDTO {
        return translate(snippetRepository.findSnippetEntityByTitle(title))
    }

    fun getAllUsers(page: Int, pageSize: Int, name: String, jwt: Jwt): PaginatedUsers {
        return try {
            logger.info("Getting all users")

            val userId = getUserIdFromJWT(jwt)

            val usersRE = userUtils.getUsers(page, pageSize, name, jwt)

            val users = usersRE.body?.filter { it.user_id != userId } ?: emptyList()
            val userDTOList = users.map { UserDTOUI(it.user_id, it.nickname) }

            PaginatedUsers(
                pagination = Pagination(page, pageSize, users.size),
                usersDTOUI = userDTOList
            )
        } catch (e: HttpClientErrorException) {
            logger.error("HTTP error while getting users: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Failed to get users: ${e.message}")
            throw e
        }
    }


    private fun translate(snippetDTO: SnippetDTO): SnippetEntity {
        return SnippetEntity(
            snippetDTO.id,
            snippetDTO.title,
            snippetDTO.extension,
            snippetDTO.language,
            snippetDTO.version,
        )
    }

    private fun translate(snippetEntity: SnippetEntity): SnippetDTO {
        return SnippetDTO(
            snippetEntity.id,
            snippetEntity.title,
            snippetEntity.extension,
            snippetEntity.language,
            snippetEntity.version,
        )
    }

}