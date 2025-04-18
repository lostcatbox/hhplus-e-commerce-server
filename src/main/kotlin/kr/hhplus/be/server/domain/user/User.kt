package kr.hhplus.be.server.domain.user

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    val name: String,
    val email: String = "",
    var password: String = "",
    val active: Boolean
) {
    fun isActive(): Boolean {
        return active
    }

    //TODO : password 추후 암호화 필요
    fun checkPassword(password: String): Boolean {
        return password.equals(password)
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        if (!checkPassword(oldPassword)) {
            throw IllegalArgumentException("기존 비밀번호가 일치하지않습니다.")
        }
        this.password = newPassword
    }
}