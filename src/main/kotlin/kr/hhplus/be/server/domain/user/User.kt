package kr.hhplus.be.server.domain.user

// 순수 도메인 모델로 변경
class User(
    val id: Long = 0L,
    val name: String,
    val email: String = "",
    val password: String = "",
    val active: Boolean
) {
    fun isActive(): Boolean {
        return active
    }

    //TODO : password 추후 암호화 필요
    fun checkPassword(password: String): Boolean {
        return this.password == password
    }

    fun changePassword(oldPassword: String, newPassword: String): User {
        if (!checkPassword(oldPassword)) {
            throw IllegalArgumentException("기존 비밀번호가 일치하지않습니다.")
        }
        return User(
            id = this.id,
            name = this.name,
            email = this.email,
            password = newPassword,
            active = this.active
        )
    }
}