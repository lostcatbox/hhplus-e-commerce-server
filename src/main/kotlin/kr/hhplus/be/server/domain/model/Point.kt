package kr.hhplus.be.server.domain.model

data class Point(
    val userId: Long,
    val amount: Long
) {
    fun usePoint(useAmount: Long): Point {
        val resultAmount = amount - useAmount
        require(resultAmount >= 0) { "금액은 0 이상이여야 합니다." }
        return this.copy(amount = resultAmount)
    }

    fun chargePoint(chargeAmount: Long): Point {
        require(chargeAmount > 0 && chargeAmount <= 1000000L) { "한번 충전 시 충전 금액은 0 초과 100만원 이하여야합니다." }
        return this.copy(amount = chargeAmount + amount)
    }

    companion object {
        fun EMPTY(userId: Long): Point {
            return Point(userId, 0)
        }
    }
}