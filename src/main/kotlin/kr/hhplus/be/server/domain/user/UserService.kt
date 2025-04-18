package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.exceptions.UserNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getUserByUserId(userId: Long): User {
        return userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)
    }

    fun checkActiveUser(userId: Long): Boolean {
        return userRepository.findById(userId)?.isActive() ?: false
    }
}