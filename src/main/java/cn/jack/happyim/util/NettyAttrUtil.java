package cn.jack.happyim.util;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;


/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description netty工具类
 */
public class NettyAttrUtil {

    private static final AttributeKey<String> ATTR_KEY_LAST_HEARTBEAT_TIME = AttributeKey.valueOf("lastHeartBeatTime");

    private static final AttributeKey<String> ATTR_KEY_USER_ID = AttributeKey.valueOf("userId");

    /**
     * 刷新心跳时间
     */
    public static void refreshLastHeartBeatTime(Channel channel) {
        long now = System.currentTimeMillis();
        channel.attr(ATTR_KEY_LAST_HEARTBEAT_TIME).set(Long.toString(now));
    }

    /**
     * 获取最后一次心跳时间
     */
    public static Long getLastHeartBeatTime(Channel channel) {
        String value = getAttribute(channel, ATTR_KEY_LAST_HEARTBEAT_TIME);
        if (StringUtils.isNotBlank(value)) {
            return Long.valueOf(value);
        }
        return null;
    }

    public static void setUserId(Channel channel, String value) {
        channel.attr(ATTR_KEY_USER_ID).set(value);
    }

    public static String getUserId(Channel channel) {
        return getAttribute(channel, ATTR_KEY_USER_ID);
    }

    private static String getAttribute(Channel channel, AttributeKey<String> key) {
        Attribute<String> attr = channel.attr(key);
        return attr.get();
    }

    /**
     * 清除会话信息
     */
    public static void clearSession(Channel channel) {
        String userId = NettyAttrUtil.getUserId(channel);
        // 清除会话信息
        SessionHolder.channelGroup.remove(channel);
        SessionHolder.channelMap.remove(userId);
    }
}
