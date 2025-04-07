package kr.hhplus.be.server.presentation.controller.product

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET 제품 리스트 조회 - 200 Ok`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/products"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `GET 인기 제품 top 5 조회 - 200 Ok`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/products/popular"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
} 