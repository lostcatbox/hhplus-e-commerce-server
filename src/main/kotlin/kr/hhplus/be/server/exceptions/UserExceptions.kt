package kr.hhplus.be.server.exceptions

open class UserException : RuntimeException()
class UserNotFoundException(val id: Long) : UserException()
