package kr.hhplus.be.server.presentation.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.presentation.controller.user.dto.response.UserResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v1/users")
class UserController {
    @Operation(summary = "유저 id로 유저 조회", description = "userId=1, 200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("/{userId}")
    fun getUser(@PathVariable("userId") userId: Long): UserResponse {
        return UserResponse(1, "1234@naver.com", "김철수", true)
    }
}