package kr.hhplus.be.server.presentation.controller

import kr.hhplus.be.server.exceptions.ProductNotFoundException
import kr.hhplus.be.server.exceptions.UserNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ErrorResponse(val code: String, val message: String)

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)


    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFoundExceptionException(e: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "Product를 찾을 수 없습니다 fail Product.id : ${e.id}"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundExceptionException(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "User를 찾을 수 없습니다 fail User.id : ${e.id}"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다."),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}