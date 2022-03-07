package cn.jack.happyim.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author 神秘杰克
 * 公众号: Java菜鸟程序员
 * @date 2022/1/12
 * @Description 消息体
 */
@Data
public class IMMessage {

    /** 消息id */
    private String id;
    /** 消息发送类型 */
    private Integer code;
    /** 发送人用户id */
    private String sendUserId;
    /** 发送人用户名 */
    private String username;
    /** 接收人用户id，多个逗号分隔 */
    private String receiverUserId;
    /** 发送时间 */
    private Date sendTime;
    /** 消息类型 */
    private Integer type;
    /** 消息内容 */
    private String msg;
    /** 消息扩展内容 */
    private Map<String, Object> ext;

}
