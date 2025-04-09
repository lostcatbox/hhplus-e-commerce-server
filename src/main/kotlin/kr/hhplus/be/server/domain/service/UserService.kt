package kr.hhplus.be.server.domain.service

import kr.hhplus.be.server.domain.model.User
import kr.hhplus.be.server.domain.port.out.UserRepository
import kr.hhplus.be.server.exceptions.UserNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getUserByUserId(userId: Long): User {
        return userRepository.findByUserId(userId)
            ?: throw UserNotFoundException(userId)
    }
}