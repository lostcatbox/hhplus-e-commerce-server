package kr.hhplus.be.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class LockAcquisitionFailedException(message: String = "분산락 획득에 실패했습니다.") : RuntimeException(message) 