package kr.hhplus.be.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.context.annotation.Bean
import java.util.concurrent.Executor

/**
 * 비동기 처리를 위한 설정
 */
@Configuration
@EnableAsync
class AsyncConfig {

    /**
     * 이벤트 처리용 스레드 풀 설정
     */
    @Bean
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 25
        executor.setThreadNamePrefix("event-executor-")
        executor.initialize()
        return executor
    }
} 