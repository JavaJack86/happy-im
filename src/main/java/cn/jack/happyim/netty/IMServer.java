package cn.jack.happyim.netty;

import cn.jack.happyim.constant.WebSocketConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description netty服务
 */
@Component
@Slf4j
public class IMServer {

    private static class SingletonIMServer {
        static final IMServer INSTANCE = new IMServer();
    }

    public static IMServer getInstance() {
        return SingletonIMServer.INSTANCE;
    }

    private final ServerBootstrap server;
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    private IMServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new IMServerInitializer());
    }

    public void start() {
        try {
            final Channel channel = server.bind(WebSocketConstant.WEB_SOCKET_PORT).sync().channel();
            channel.closeFuture().sync();
            log.debug("happy im netty websocket server 启动成功...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.debug("happy im netty websocket server 关闭...");
        }
    }

}
