package kr.hhplus.be.server.infra.persistance.model

import jakarta.persistence.*
import kr.hhplus.be.server.domain.user.User

@Entity(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    val name: String,
    
    val email: String = "",
    
    val password: String = "",
    
    val active: Boolean
) {
    // 도메인 모델로 변환
    fun toDomain(): User {
        return User(
            id = this.id,
            name = this.name,
            email = this.email,
            password = this.password,
            active = this.active
        )
    }
    
    companion object {
        // 도메인 모델로부터 엔티티 생성
        fun from(domain: User): UserEntity {
            return UserEntity(
                id = domain.id,
                name = domain.name,
                email = domain.email,
                password = domain.password,
                active = domain.active
            )
        }
    }
} 