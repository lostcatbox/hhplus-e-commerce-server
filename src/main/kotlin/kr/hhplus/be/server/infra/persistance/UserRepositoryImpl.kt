package kr.hhplus.be.server.infra.persistance

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import kr.hhplus.be.server.infra.persistance.jpa.UserJpaRepository
import kr.hhplus.be.server.infra.persistance.model.UserEntity
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(userId: Long): User? {
        return userJpaRepository.findById(userId).orElse(null)?.toDomain()
    }

    override fun save(user: User): User {
        val entity = UserEntity.from(user)
        val savedEntity = userJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
} 