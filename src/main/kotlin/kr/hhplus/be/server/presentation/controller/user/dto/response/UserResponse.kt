package kr.hhplus.be.server.presentation.controller.user.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 정보 응답")
data class UserResponse(
    val userId: Int,
    val email: String,
    val name: String,
    val active: Boolean
)
