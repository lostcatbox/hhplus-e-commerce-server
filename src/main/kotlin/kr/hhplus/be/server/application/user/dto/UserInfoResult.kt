package kr.hhplus.be.server.application.user.dto

import kr.hhplus.be.server.domain.model.User

data class UserInfoResult(
    val userId: Long,
    val userName: String,
    val email: String
) {
    companion object {
        fun of(user: User): UserInfoResult {
            return UserInfoResult(
                userId = user.id,
                userName = user.name,
                email = user.email
            )
        }
    }
}
