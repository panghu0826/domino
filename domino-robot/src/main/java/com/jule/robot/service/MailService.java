package com.jule.robot.service;

import com.jule.robot.config.Config;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailService {
    private final static Logger logger = LoggerFactory.getLogger(RobotMoneyPoolHolder.class);

    public static void sendMail(String subject, String content){
        String[] arrToMail = Config.TO_MAIL_LIST.split(",");
        sendMail(arrToMail, subject, content);
    }

    public static void sendMail(String[] arrToMail, String subject, String content) {
        Transport transport = null;
        try {
            // 属性
            Properties properties = new Properties();
            // 设置认证属性
            properties.setProperty("mail.smtp.auth", "true");
            // 设置通信协议
            properties.setProperty("mail.transport.protocol", "smtp");
            // 邮件环境信息
            Session session = Session.getInstance(properties);
            // 调试,打印信息
            session.setDebug(false);


            // 邮件
            Message message = new MimeMessage(session);
            // 主题
            message.setSubject(subject);
            // 发送人
            message.setFrom(new InternetAddress("jologame@163.com"));

            //收件人列表
            List<Address> addressList = new ArrayList<Address>();
            for (String toMail : arrToMail) {
                addressList.add(new InternetAddress(toMail));
            }

            // 内容
            message.setText(content);
            message.addRecipient(Message.RecipientType.CC, new InternetAddress("jologame@163.com"));
            message.addRecipients(Message.RecipientType.TO, addressList.toArray(new Address[addressList.size()]));

            // 邮件传输对象
            transport = session.getTransport();
            // 传输连接：host，port，user，pass/主机，端口，用户名，密码
            transport.connect("smtp.163.com", 25, "jologame@163.com", "guoxu1983");
            // 发送邮件
            transport.sendMessage(message, addressList.toArray(new Address[addressList.size()]));
            logger.warn("发送邮件，subject->" + subject);
        } catch (Exception ex) {
            logger.error("SendMail error, msg->" + ex.getMessage(), ex);
        } finally {
            if (null != transport && transport.isConnected()) {
                // 关闭连接
                try {
                    transport.close();
                } catch (Exception ex) {
                }
            }
        }
    }
}
