package cn.jack.happyim.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static cn.jack.happyim.constant.ThreadPoolConstant.*;
/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description 自定义线程池配置
 */
@Component
public class AsyncScheduledTaskConfig {

    @Bean
    public Executor scheduledTask(){
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("clean-pool-%d").build();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_THREAD_SIZE);
        executor.setMaxPoolSize(MAX_CORE_THREAD_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadFactory(threadFactory);
        executor.setKeepAliveSeconds(KEEPALIVE_TIME_SECONDS);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

}
