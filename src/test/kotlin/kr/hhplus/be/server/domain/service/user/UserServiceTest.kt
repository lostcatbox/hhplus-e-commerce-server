package kr.hhplus.be.server.domain.service.user

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserRepository
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.exceptions.UserNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

    private val userId = 1L
    private lateinit var user: User
    private lateinit var inactiveUser: User

    @BeforeEach
    fun setUp() {
        user = User(
            id = userId,
            name = "테스트 사용자",
            active = true
        )

        inactiveUser = User(
            id = 2L,
            name = "비활성 사용자",
            active = false
        )

        every { userRepository.findByUserId(userId) } returns user
        every { userRepository.findByUserId(2L) } returns inactiveUser
        every { userRepository.findByUserId(999L) } returns null
    }

    @Test
    fun `사용자 ID로 사용자 정보 조회 - 존재하는 경우`() {
        // When
        val result = userService.getUserByUserId(userId)

        // Then
        assertEquals(userId, result.id)
        assertEquals("테스트 사용자", result.name)
        assertTrue(result.active)
        verify(exactly = 1) { userRepository.findByUserId(userId) }
    }

    @Test
    fun `사용자 ID로 사용자 정보 조회 - 존재하지 않는 경우`() {
        // When & Then
        assertThrows<UserNotFoundException> {
            userService.getUserByUserId(999L)
        }
        verify(exactly = 1) { userRepository.findByUserId(999L) }
    }

    @Test
    fun `활성 사용자 확인 - 활성 상태인 경우`() {
        // Given
//        every { user.isActive() } returns true

        // When
        val result = userService.checkActiveUser(userId)

        // Then
        assertTrue(result)
        verify(exactly = 1) { userRepository.findByUserId(userId) }
//        verify(exactly = 1) { user.isActive() }
    }

    @Test
    fun `활성 사용자 확인 - 비활성 상태인 경우`() {
        // Given
//        every { inactiveUser.isActive() } returns false

        // When
        val result = userService.checkActiveUser(2L)

        // Then
        assertFalse(result)
        verify(exactly = 1) { userRepository.findByUserId(2L) }
//        verify(exactly = 1) { inactiveUser.isActive() }
    }

    @Test
    fun `활성 사용자 확인 - 사용자가 존재하지 않는 경우`() {
        // When
        val result = userService.checkActiveUser(999L)

        // Then
        assertFalse(result)
        verify(exactly = 1) { userRepository.findByUserId(999L) }
    }
} 