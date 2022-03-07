package cn.jack.happyim.netty;

import cn.jack.happyim.constant.MessageTypeConstant;
import cn.jack.happyim.constant.WebSocketConstant;
import cn.jack.happyim.model.IMMessage;
import cn.jack.happyim.util.NettyAttrUtil;
import cn.jack.happyim.util.RequestParamUtil;
import cn.jack.happyim.util.SessionHolder;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.HttpStatus;

import java.util.*;

import static cn.jack.happyim.constant.MessageCodeConstant.*;

/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description 自定义handler
 */
@Slf4j
public class HappyIMHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 握手工厂类
     */
    private final WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(WebSocketConstant.WEB_SOCKET_URL, null, false);
    private WebSocketServerHandshaker shaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        if (o instanceof FullHttpRequest) {
            //处理客户端向服务端发起的HTTP请求
            handHttpRequest(ctx, (FullHttpRequest) o);
        } else if (o instanceof WebSocketFrame) {
            handWebSocketFrame(ctx, (WebSocketFrame) o);
        }
    }

    private void handWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            //关闭握手
            final Channel channel = ctx.channel();
            shaker.close(channel, (CloseWebSocketFrame) frame.retain());
            NettyAttrUtil.clearSession(channel);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            throw new RuntimeException("【" + this.getClass().getName() + "】不支持二进制消息");
        }
        //获取json数据
        String message = ((TextWebSocketFrame) frame).text();
        log.info("消息内容为:{}", message);
        JSONObject json = JSONObject.parseObject(message);
        try {
            final String uuid = UUID.randomUUID().toString();
            final String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            json.put("id", uuid);
            json.put("sendTime", time);

            int code = json.getIntValue("code");
            switch (code) {
                //群聊
                case GROUP_CHAT_CODE:
                //向连接上来的客户端广播消息
                case SYSTEM_MESSAGE_CODE:
                    SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(json)));
                    break;
                //私聊
                case PRIVATE_CHAT_CODE:
                    String receiveUserId = json.getString("receiverUserId");
                    String sendUserId = json.getString("sendUserId");
                    String msg = JSONObject.toJSONString(json);
                    // 点对点挨个给接收人发送消息
                    for (Map.Entry<String, Channel> entry : SessionHolder.channelMap.entrySet()) {
                        String userId = entry.getKey();
                        Channel channel = entry.getValue();
                        if (receiveUserId.equals(userId)) {
                            channel.writeAndFlush(new TextWebSocketFrame(msg));
                        }
                    }
                    //如果发给别人,给自己也发一条
                    if (!receiveUserId.equals(sendUserId)) {
                        SessionHolder.channelMap.get(sendUserId).writeAndFlush(new TextWebSocketFrame(msg));
                    }
                    break;
                //pong
                case PONG_CHAT_CODE:
                    Channel channel = ctx.channel();
                    // 更新心跳时间
                    NettyAttrUtil.refreshLastHeartBeatTime(channel);
                    break;
                default:
            }
        } catch (Exception e) {
            log.error("转发消息异常:", e);
        }

    }

    /**
     * 处理客户端向服务端发起 http 握手请求的业务
     * WebSocket在建立握手时，数据是通过HTTP传输的。但是建立之后，在真正传输时候是不需要HTTP协议的。
     * WebSocket 连接过程：
     * 首先，客户端发起http请求，经过3次握手后，建立起TCP连接；http请求里存放WebSocket支持的版本号等信息，如：Upgrade、Connection、WebSocket-Version等；
     * 然后，服务器收到客户端的握手请求后，同样采用HTTP协议回馈数据；
     * 最后，客户端收到连接成功的消息后，开始借助于TCP传输信道进行全双工通信。
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !("websocket".equals(request.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        //新建立握手
        shaker = factory.newHandshaker(request);
        if (shaker == null) {
            //为空说明 不支持的websocket版本
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            final Map<String, String> params = RequestParamUtil.urlSplit(request.uri());
            final String userId = params.get("userId");
            final Channel channel = ctx.channel();
            NettyAttrUtil.setUserId(channel, userId);
            NettyAttrUtil.refreshLastHeartBeatTime(channel);
            shaker.handshake(channel, request);
            SessionHolder.channelGroup.add(channel);
            SessionHolder.channelMap.put(userId, channel);
            log.info("握手成功...客户端uri为" + request.uri());

            //用户上线,更新客户端在线列表
            final Set<String> userList = SessionHolder.channelMap.keySet();
            IMMessage msg = new IMMessage();
            Map<String, Object> map = new HashMap<>();
            map.put("userList", userList);
            msg.setExt(map);
            msg.setCode(SYSTEM_MESSAGE_CODE);
            msg.setType(MessageTypeConstant.UPDATE_USER_LIST_SYSTEM_MESSAGE);
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, DefaultFullHttpResponse response) {
        if (response.status().code() != HttpStatus.OK.value()) {
            //创建源缓冲区
            ByteBuf byteBuf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
            //将源缓冲区的数据传送到此缓冲区
            response.content().writeBytes(byteBuf);
            //释放源缓冲区
            byteBuf.release();
        }
        //写入请求，服务端向客户端发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (response.status().code() != HttpStatus.OK.value()) {
            //如果请求失败，关闭 ChannelFuture
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("————客户端与服务端连接断开————");
        NettyAttrUtil.clearSession(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:", cause);
        ctx.close();
    }

    /**
     * 客户端与服务端创建连接的时候调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("————客户端与服务端连接开启————");
    }
}
