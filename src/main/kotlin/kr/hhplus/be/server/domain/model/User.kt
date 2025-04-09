package kr.hhplus.be.server.domain.model

data class User(
    val userId: Long,
    val name: String,
    val email: String,
    var password: String,
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
            throw IllegalArgumentException("Old password does not match new password")
        }
        this.password = newPassword
    }
}