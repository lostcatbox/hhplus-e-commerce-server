package kr.hhplus.be.server.domain.port.out

import kr.hhplus.be.server.domain.model.Point
import org.springframework.stereotype.Repository

@Repository
interface PointRepository {
    fun findById(userId: Long): Point?
    fun save(point: Point)
}