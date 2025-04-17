package kr.hhplus.be.server.domain.service.user

import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.exceptions.UserNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `getUserByUserId - 존재하는 사용자 조회 성공`() {
        // given
        val user = User(
            id = 0,
            email = "test@example.com",
            name = "Test User",
            password = "password",
            active = true
        )
        val savedUser = userRepository.save(user)

        // when
        val result = userService.getUserByUserId(savedUser.id)

        // then
        assertNotNull(result)
        assertEquals(savedUser.id, result.id)
        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.name)
    }

    @Test
    fun `getUserByUserId - 존재하지 않는 사용자 조회시 예외 발생`() {
        // when & then
        assertThrows<UserNotFoundException> {
            userService.getUserByUserId(999L)
        }
    }

    @Test
    fun `checkActiveUser - 활성 사용자 확인`() {
        // given
        val user = User(
            id = 0,
            email = "active@example.com",
            name = "Active User",
            password = "password",
            active = true
        )
        val savedUser = userRepository.save(user)

        // when
        val result = userService.checkActiveUser(savedUser.id)

        // then
        assertTrue(result)
    }

    @Test
    fun `checkActiveUser - 비활성 사용자 확인`() {
        // given
        val user = User(
            id = 0,
            email = "inactive@example.com",
            name = "Inactive User",
            password = "password",
            active = false
        )
        val savedUser = userRepository.save(user)

        // when
        val result = userService.checkActiveUser(savedUser.id)

        // then
        assertFalse(result)
    }

    @Test
    fun `checkActiveUser - 존재하지 않는 사용자 확인시 false 반환`() {
        // when
        val result = userService.checkActiveUser(999L)

        // then
        assertFalse(result)
    }
} 