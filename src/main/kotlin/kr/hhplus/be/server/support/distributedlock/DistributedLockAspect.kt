package kr.hhplus.be.server.support.distributedlock

import kr.hhplus.be.server.exceptions.LockAcquisitionFailedException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // 가장 높은 우선순위로 실행되도록 설정
class DistributedLockAspect(
    private val redissonClient: RedissonClient
) {
    companion object {
        private val PARAMETER_NAME_DISCOVERER = DefaultParameterNameDiscoverer()
    }

    @Around("@annotation(kr.hhplus.be.server.support.distributedlock.DistributedLock)")
    fun distributedLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)
        
        val keyPrefix = "LOCK:"
        val lockKey = keyPrefix + parseKey(distributedLock.key, method, joinPoint.args)
        val lock = redissonClient.getLock(lockKey)
        
        return executeLocked(
            lock = lock,
            joinPoint = joinPoint,
            waitTime = distributedLock.waitTime,
            leaseTime = distributedLock.leaseTime,
            timeUnit = distributedLock.timeUnit
        )
    }
    
    private fun executeLocked(
        lock: RLock,
        joinPoint: ProceedingJoinPoint,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit
    ): Any? {
        try {
            val acquired = lock.tryLock(waitTime, leaseTime, timeUnit)
            if (!acquired) {
                throw LockAcquisitionFailedException("분산락 획득에 실패했습니다. (Lock key: ${lock.name})")
            }
            
            return joinPoint.proceed()
        } finally {
            // 락을 획득한 쓰레드만 락을 해제할 수 있음
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
    
    private fun parseKey(key: String, method: Method, args: Array<Any>): String {
        // 간단한 파라미터 치환 방식으로 변경
        // userId 파라미터를 찾아서 치환
        val parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method) ?: return key
        
        var resultKey = key
        for (i in parameterNames.indices) {
            val pattern = "#{" + parameterNames[i] + "}"
            if (resultKey.contains(pattern)) {
                resultKey = resultKey.replace(pattern, args[i].toString())
            }
        }
        
        return resultKey
    }
} 