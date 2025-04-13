package kr.hhplus.be.server.presentation.controller.order

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.presentation.controller.order.dto.OrderLine
import kr.hhplus.be.server.presentation.controller.order.dto.OrderRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(OrderController::class)
class OrderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `POST 주문 요청 - 200 Ok`() {
        val request = OrderRequest(
            userId = 1L,
            couponId = 1L,
            orderLines = listOf(
                OrderLine(productId = 1L, quantity = 1),
                OrderLine(productId = 2L, quantity = 2)
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
} 