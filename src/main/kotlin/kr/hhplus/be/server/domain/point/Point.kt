package kr.hhplus.be.server.domain.point

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "points")
class Point(
    @Id
    var userId: Long,
    var amount: Long
) {
    fun usePoint(useAmount: Long) {
        val resultAmount = amount - useAmount
        require(resultAmount >= 0) { "금액은 0 이상이여야 합니다." }
        amount = resultAmount
    }

    fun chargePoint(chargeAmount: Long) {
        require(chargeAmount > 0 && chargeAmount <= 1000000L) { "한번 충전 시 충전 금액은 0 초과 100만원 이하여야합니다." }
        amount = chargeAmount + amount
    }

    companion object {
        fun EMPTY(userId: Long): Point {
            return Point(userId, 0)
        }
    }
}