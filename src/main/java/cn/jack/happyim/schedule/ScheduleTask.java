package cn.jack.happyim.schedule;

import cn.jack.happyim.util.NettyAttrUtil;
import cn.jack.happyim.util.SessionHolder;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description 定时任务触发netty
 */
@Component
@EnableAsync
@Slf4j
public class ScheduleTask {

    /**
     * 广播 ping 信息
     */
    @Async("scheduledTask")
    @Scheduled(cron = "0 */1 * * * ?")
    public void sendPing() {
        // TODO 未完成
        log.debug("一分钟广播ping一次");
    }

    /**
     * 从缓存中移除Channel，并且关闭Channel
     */
    @Async("scheduledTask")
    @Scheduled(cron = "0 */1 * * * ?")
    public void scanNotActiveChannel() {
        // TODO 未完成
        log.debug("一分钟移除channel一次");
    }

}
