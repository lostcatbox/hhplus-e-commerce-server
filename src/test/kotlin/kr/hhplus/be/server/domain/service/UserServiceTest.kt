package kr.hhplus.be.server.domain.service

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.port.out.UserRepository
import kr.hhplus.be.server.domain.service.user.UserService
import kr.hhplus.be.server.exceptions.UserNotFoundException
import org.junit.jupiter.api.Test

class UserServiceTest {
    @Test
    fun `userId에 해당하는 User 없을때, UserNotFoundException`() {
        // given
        val userRepository = mockk<UserRepository>()
        val userService = UserService(userRepository)

        // when
        every { userRepository.findByUserId(any()) } returns null

        // then
        shouldThrow<UserNotFoundException> {
            userService.getUserByUserId(1L)
        }
    }
}