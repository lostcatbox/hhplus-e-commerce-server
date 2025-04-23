package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import kr.hhplus.be.server.domain.point.Point

@Entity(name = "points")
class PointEntity(
    @Id
    val userId: Long,
    
    val amount: Long
) {
    // 도메인 모델로 변환
    fun toDomain(): Point {
        return Point(
            userId = this.userId,
            amount = this.amount
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: Point): PointEntity {
            return PointEntity(
                userId = domain.userId,
                amount = domain.amount
            )
        }
    }
} 