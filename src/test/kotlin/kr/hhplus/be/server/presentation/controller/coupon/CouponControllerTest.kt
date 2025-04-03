package kr.hhplus.be.server.presentation.controller.coupon

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(CouponController::class)
class CouponControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET 쿠폰 리스트 조회 - 200 Ok`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/coupons"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `POST 쿠폰 발급 요청 - 200 Ok`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/coupons")
                .contentType("application/json")
                .content("""{"userId": 1, "couponId": 1}""")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }


} 