package kr.hhplus.be.server.domain.user

import org.springframework.stereotype.Repository

@Repository
interface UserRepository {
    fun findByUserId(userId: Long): User?
}