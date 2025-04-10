import kr.hhplus.be.server.domain.model.OrderHistory
import org.springframework.stereotype.Repository

@Repository
interface OrderHistoryRepository {
    fun save(orderHistory: OrderHistory): OrderHistory
}