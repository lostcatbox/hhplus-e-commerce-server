package kr.hhplus.be.server.presentation.controller.user.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.application.user.dto.UserInfoResult

@Schema(description = "유저 정보 응답")
data class UserResponse(
    val userId: Int,
    val email: String,
    val name: String,
    val active: Boolean
) {
    companion object {
        fun of(userInfo: UserInfoResult): UserResponse {
            return UserResponse(
                userId = userInfo.userId.toInt(),
                email = userInfo.email,
                name = userInfo.userName,
                active = true // 정보가 없으므로 기본값으로 설정
            )
        }
    }
}
