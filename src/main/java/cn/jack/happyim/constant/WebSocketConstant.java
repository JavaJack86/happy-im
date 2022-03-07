package cn.jack.happyim.constant;

/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description webSocket常量
 */
public class WebSocketConstant {

    public static final int WEB_SOCKET_PORT = 8888;

    private static final String WEB_SOCKET_IP = "localhost";

    public static final String WEB_SOCKET_URL = "ws://" + WEB_SOCKET_IP + ":"+ WEB_SOCKET_PORT +"/websocket";

}
