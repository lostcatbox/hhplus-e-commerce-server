package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository {
    fun findByUserId(userId: Long): User?
}