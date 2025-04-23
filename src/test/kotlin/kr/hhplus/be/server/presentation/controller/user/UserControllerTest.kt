package kr.hhplus.be.server.presentation.controller.user

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET 유저 정보 조회 - 200 Ok`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/1"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
} 