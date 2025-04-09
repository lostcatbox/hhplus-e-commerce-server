package kr.hhplus.be.server.exceptions

open class ProductException : RuntimeException()
class ProductNotFoundException(val id: Long) : ProductException()
