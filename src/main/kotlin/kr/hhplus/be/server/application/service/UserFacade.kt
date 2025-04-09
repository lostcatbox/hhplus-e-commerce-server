package kr.hhplus.be.server.application.service

import kr.hhplus.be.server.application.service.dto.UserInfoResult
import kr.hhplus.be.server.domain.service.UserService
import org.springframework.stereotype.Service

// Facade는 응용 계층이다. 여러 도메인 service 를 통합한다.
// 도메인 비즈니스 로직은 도메인계층의 도메인모델과 도메인 서비스를 이용하자.
// Facade는 request = "~Criteria", response = "~Result"
// Service 계층은 request = "~Command", "~Info", response = "~Domain", "~Vo"

@Service
class UserFacade(
    private val userService: UserService
) {
    fun getUserBy(userId: Long): UserInfoResult {
        return UserInfoResult.of(userService.getUserByUserId(userId))
    }
}