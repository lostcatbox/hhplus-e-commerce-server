package kr.hhplus.be.server.presentation.controller.point

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kr.hhplus.be.server.application.point.PointFacade
import kr.hhplus.be.server.presentation.controller.point.dto.PointChargeRequest
import kr.hhplus.be.server.presentation.controller.point.dto.PointResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users/{userId}/points")
class PointController(
    private val pointFacade: PointFacade
) {
    @Operation(summary = "유저 포인트 조회", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @GetMapping("")
    fun getPoints(@PathVariable("userId") userId: Long): ResponseEntity<PointResponse> {
        val point = pointFacade.getPoint(userId)
        return ResponseEntity.ok(PointResponse.of(point))
    } 

    @Operation(summary = "유저 포인트 충전", description = "200 성공 테스트 가능")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 성공")
        ]
    )
    @PostMapping("")
    fun chargePoints(
        @PathVariable("userId") userId: Long,
        @RequestBody @Valid request: PointChargeRequest
    ): ResponseEntity<PointResponse> {
        val command = request.toCommand(userId)
        pointFacade.chargePoint(command.userId, command.amount)
        val updatedPoint = pointFacade.getPoint(userId)
        return ResponseEntity.ok(PointResponse.of(updatedPoint))
    }
}