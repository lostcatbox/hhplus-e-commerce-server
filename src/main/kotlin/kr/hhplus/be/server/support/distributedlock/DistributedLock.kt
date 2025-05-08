package kr.hhplus.be.server.support.distributedlock

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    /**
     * 락의 이름을 정의합니다. SpEL을 지원합니다.
     * 예: "user_#{ #userId }" 또는 "order_#{ #orderId }"
     */
    val key: String,
    
    /**
     * 락 획득 대기 시간 (밀리초)
     * waitTime이 지나면 락 획득에 실패하고 LockAcquisitionFailedException이 발생합니다.
     */
    val waitTime: Long = 5000,
    
    /**
     * 락 유지 시간 (밀리초)
     * 이 시간이 지나면 락이 자동으로 해제됩니다.
     * 메서드 실행이 끝나기 전에 락이 해제되지 않도록 충분히 길게 설정해야 합니다.
     */
    val leaseTime: Long = 10000,
    
    /**
     * 락 획득을 위해 대기할 시간 단위
     */
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
) 