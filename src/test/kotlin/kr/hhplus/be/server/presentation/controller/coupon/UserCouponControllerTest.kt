package kr.hhplus.be.server.presentation.controller.coupon

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(UserCouponController::class)
class UserCouponControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET 유저별 보유한 쿠폰 리스트 조회 - 200 Ok`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/1/coupons"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
} 