package kr.hhplus.be.server.presentation.controller.point

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.presentation.controller.point.dto.PointChargeRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(PointController::class)
class PointControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET 유저별 포인트 조회 - 200 Ok`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/1/points"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `POST 유저별 포인트 충전 - 200 Ok`() {
        val request = PointChargeRequest(10000L)
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/users/1/points")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
} 