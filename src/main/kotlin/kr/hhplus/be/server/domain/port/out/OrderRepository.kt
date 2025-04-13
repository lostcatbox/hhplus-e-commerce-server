import kr.hhplus.be.server.domain.model.Order
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository {
    fun save(order: Order): Order
}