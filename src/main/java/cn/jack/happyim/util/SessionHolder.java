package cn.jack.happyim.util;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description netty会话管理
 */
public class SessionHolder {

    /**
     * 存储每个客户端接入进来时的 channel 对象
     * 主要用于使用 writeAndFlush 方法广播信息
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 用于客户端和服务端握手时存储用户id和netty Channel对应关系
     */
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

}
