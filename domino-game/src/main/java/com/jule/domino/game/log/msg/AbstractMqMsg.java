package com.jule.domino.game.log.msg;

import lombok.Getter;
import lombok.Setter;

/**
 * 统一日志说明
 * 一、目的
 *   为了更好的记录平台运行情况，更方便地定位问题，更方便的进行统计分析，平台内各个部分需要记录日志
 *
 * 二、日志机制
 *   1、平台日志采用消息队列记录日志，消息队列采用RabbitMQ，可以在github上找到对应的开发语言的教程（https://github.com/rabbitmq/rabbitmq-tutorials）。
 *   2、建议采用异步的方式将日志投入对应的消息队列
 * 三、日志内容
 *   1、用户访问日志
 *   2、为便于定位问题，可以记录流程中关键信息
 *   3、错误信息
 * 四、开发规范
 *   1、开发过程中，以每个游戏的ID作为RabbitMQ中 exchange 的名称， exchange 的类型使用 topic 类型；举例 游戏的ID为1001，以下说明均使用 1001
 *   2、以 info.1001 作为RabbitMQ中 queue 的名称；
 *   3、绑定 queue 名称 和 exchange 名称 为 info.1001， 即 routing_key = info.1001；
 *   4、将日志投递到 exchange=1001， routing_key = info.1001 的队列中；
 *   5、开发完成后，需要联系运维人员，获取 RabbitMQ 的参数信息；
 *   6、游戏提供方只负责成功生产日志， 消费日志的工作不需要处理；
 *   7、为了便于日后运维工作，RabbitMQ服务器的参数信息需保存在各游戏的参数文件中；
 * 五、日志格式
 *   1、日志为json格式的字符串；
 *   2、json格式中必须包括如下几项：
 *   DATETIME：字符串类型，日志记录时间，精确到毫秒（且毫秒必须是3位，如果不足3位需左侧补0），
 *   SERVER_TYPE：字符串类型，游戏类型ID，
 *   SERVER_ID： 字符串类型，游戏服务器ID，
 *   LOCAL_IP：字符串类型，本地IP，
 *   REMOTE_IP：字符串类型，远端IP，
 *   LOG_TYPE：字符串类型，日志类型， 访问日志：access_log、错误日志：error_log、账户日志：account_log、自定义日志：custom_log
 *   UID：字符串类型，用户ID，没有用户ID时写0，
 *   ACTION：字符串类型，此字段可以是用户行为、消息编号、错误码、对应的class、function名称
 *   MSG：字符串类型，原始的日志，如果游戏是pb消息结构，可以将pb消息结构转成json字符串存入MSG字段
 *
 *   举例说明：
 *   {
 *     "ACTION": "FireReq",
 *     "LOCAL_IP": "0.0.0.0",
 *     "REMOTE_IP": "",
 *     "UID": "1_1TIO20JY",
 *     "MSG": "{\"degree\":-30946,\"lockedFishID\":2031,\"bulletID\":208}",
 *     "DATETIME": "2019-01-19 20:26:54.596",
 *     "SERVER_TYPE": "1001",
 *     "SERVER_ID": "100101",
 *     "LOG_TYPE": "access_log"
 *   }
 *
 * @author
 * @since 2019/1/20 16:40
 */
@Getter@Setter
public class AbstractMqMsg {

    //字符串类型，此字段可以是用户行为、消息编号、错误码、对应的class、function名称
    private String ACTION;

    //字符串类型，本地IP
    private String LOCAL_IP = "0.0.0.0";

    //字符串类型，远端IP
    private String REMOTE_IP = "";

    //字符串类型，日志记录时间，精确到毫秒
    private String DATETIME;

    //字符串类型，游戏类型ID
    private String SERVER_TYPE = "0.0.0.0";

    //字符串类型，游戏服务器ID
    private String SERVER_ID = "";

    //字符串类型，日志类型， 访问日志：access_log、错误日志：error_log、账户日志：account_log、自定义日志：custom_log
    private String LOG_TYPE = "access_log";

    //字符串类型，用户ID，没有用户ID时写0，
    private String UID = "0";

    //字符串类型，用户ID，没有用户ID时写0，
    private String MSG ;

}
