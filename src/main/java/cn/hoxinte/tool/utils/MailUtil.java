package cn.hoxinte.tool.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送工具
 *
 * @author dominate
 * @since 2022/10/11
 */
@Slf4j
public final class MailUtil {

    private static final String IMG_START = "<br/><img src='";
    private static final String IMG_END = "'/>";
    private static final String ADDRESS_SPLIT = ",";

    private static final String[] PROPERTY_PROTOCOL = {"mail.transport.protocol", "smtp"};
    private static final String[] PROPERTY_AUTH = {"mail.smtp.auth", "true"};
    private static final String[] PROPERTY_START_TLS = {"mail.smtp.starttls.enable", "true"};
    private static final String[] PROPERTY_CONNECT_TIMEOUT = {"mail.smtp.connectiontimeout", "3600"};
    private static final String[] PROPERTY_WRITE_TIMEOUT = {"mail.smtp.writetimeout", "3600"};
    private static final String[] PROPERTY_TIMEOUT = {"mail.smtp.timeout", "3600"};
    private static final String[][] PROPERTY_LIST = {PROPERTY_PROTOCOL, PROPERTY_TIMEOUT, PROPERTY_CONNECT_TIMEOUT,
            PROPERTY_WRITE_TIMEOUT, PROPERTY_AUTH, PROPERTY_START_TLS};

    private static final String PROPERTY_SSL_TRUST = "mail.smtp.ssl.trust";
    private static final String PROPERTY_HOST = "mail.smtp.host";
    private static final String PROPERTY_PORT = "mail.smtp.port";
    private static final int DEFAULT_PORT = 25;

    private static final String UTF_8 = "UTF-8";
    private static final String TEXT_HTML_TYPE = "text/html;charset=UTF-8";
    private static final Map<String, Session> SESSION_MAP = new HashMap<>();

    static{
        // JAVA 11 默认配置
        // SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA,DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL,
        // include jdk.disabled.namedCurves
        Security.setProperty("jdk.tls.disabledAlgorithms","RC4, DES, MD5withRSA,DH keySize < 1024, EC keySize < 224, " +
                "3DES_EDE_CBC, anon, NULL,include jdk.disabled.namedCurves");
    }
    /**
     * 发送邮件
     *
     * @param host               邮件服务器
     * @param sendName           发送名称
     * @param account            邮件账号
     * @param password           邮件密码
     * @param subject            邮件标题
     * @param text               文字内容
     * @param imgUrls            图片地址
     * @param receiveMailAccount 收件人列表
     */
    public static void sendMail(String host, String sendName, String account, String password, String subject, String text, String[] imgUrls, String... receiveMailAccount) {
        sendMail(conf(host, sendName, account, password), subject, text, imgUrls, receiveMailAccount);
    }

    /**
     * 发送邮件
     *
     * @param host               邮件服务器
     * @param sendName           发送名称
     * @param account            邮件账号
     * @param password           邮件密码
     * @param subject            邮件标题
     * @param text               文字内容
     * @param imgUrls            图片地址
     * @param receiveMailAccount 收件人列表
     */
    public static boolean sendMail(String host, int port, String sendName, String account, String password, String subject, String text, String[] imgUrls, String... receiveMailAccount) {
        return sendMail(conf(host, port, sendName, account, password), subject, text, imgUrls, receiveMailAccount);
    }

    private static MailConf conf(String host, int port, String sendName, String account, String password) {
        return new MailConf(host, port, sendName, account, password);
    }

    private static MailConf conf(String host, String sendName, String account, String password) {
        return new MailConf(host, DEFAULT_PORT, sendName, account, password);
    }


    private static boolean sendMail(MailConf conf, String subject, String text, String[] imgUrls, String... receiveMailAccount) {
        // 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = getSession(conf.getHost(), conf.getPort());
        try {
            // 创建一封邮件
            MailSender sender = new MailSender(subject, text, imgUrls, conf.getAccount(), conf.getSendName(), receiveMailAccount);
            MimeMessage message = createMimeMessage(sender, session);
            // 使用 邮箱账号 和 密码 连接邮件服务器
            Transport transport = session.getTransport();
            // 这里认证的邮箱必须与 message 中的发件人邮箱一致，否则报错
            transport.connect(conf.getAccount(), conf.getPassword());
            // 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        } catch (Exception e) {
            log.error("MailHelper send mail error ", e);
            return false;
        }
    }

    private static Session getSession(String host, int port) {
        if (SESSION_MAP.containsKey(host)) {
            return SESSION_MAP.get(host);
        }
        // 参数配置
        Properties props = new Properties();
        for (String[] property : PROPERTY_LIST) {
            props.setProperty(property[0], property[1]);
        }
        // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty(PROPERTY_SSL_TRUST, host);
        props.setProperty(PROPERTY_HOST, host);
        props.setProperty(PROPERTY_PORT, String.valueOf(port));
        // 需要请求认证
        Session session = Session.getInstance(props);
        SESSION_MAP.put(host, session);
        return session;
    }

    @Data
    private static class MailSender {
        private String sendMail;
        private String sendName;
        private String[] receiveMail;
        private String subject;
        private String content;
        private String[] imgUrls;

        public MailSender(String subject, String content, String[] imgUrls, String sendMail, String sendName, String... receiveMail) {
            this.subject = subject;
            this.content = content;
            this.imgUrls = imgUrls;
            this.sendMail = sendMail;
            this.sendName = sendName;
            this.receiveMail = receiveMail;
        }
    }

    @Data
    private static class MailConf {
        private String host;
        private String sendName;
        private String account;
        private String password;
        private Integer port;

        public MailConf(String host, int port, String sendName, String account, String password) {
            this.host = host;
            this.port = port;
            this.sendName = sendName;
            this.account = account;
            this.password = password;
        }
    }

    private static MimeMessage createMimeMessage(MailSender sender, Session session) throws Exception {
        // 1. 创建邮件对象
        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人
        message.setFrom(new InternetAddress(sender.getSendMail(), sender.getSendName(), UTF_8));
        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.addRecipients(MimeMessage.RecipientType.TO, parseAddressIp(sender.getReceiveMail()));
        // 4. Subject: 邮件主题
        message.setSubject(sender.getSubject(), UTF_8);
        // 5. 创建文本“节点”
        MimeBodyPart text = new MimeBodyPart();
        // 这里添加图片的方式是将整个图片包含到邮件内容中, http 链接的形式添加网络图片
        text.setContent(parseContentText(sender.getContent(), sender.getImgUrls()), TEXT_HTML_TYPE);
        // 6. （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
        MimeMultipart mmText = new MimeMultipart();
        mmText.addBodyPart(text);
        // 7. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
        message.setContent(mmText);
        // 8. 设置发件时间
        message.setSentDate(new Date());
        // 9. 保存上面的所有设置
        message.saveChanges();
        return message;
    }


    private static String parseContentText(String content, String... imgUrls) {
        if (imgUrls.length == 0) {
            return content;
        }

        StringBuilder builder = new StringBuilder(content);
        for (String imgUrl : imgUrls) {
            builder.append(IMG_START);
            builder.append(imgUrl);
            builder.append(IMG_END);
        }
        return builder.toString();
    }


    private static InternetAddress[] parseAddressIp(String... mail) throws AddressException {
        if (mail.length == 1) {
            return InternetAddress.parse(mail[0]);
        }
        StringBuilder mailList = new StringBuilder();
        for (int i = 0; i < mail.length; i++) {
            mailList.append(mail[i]);
            if (i < mail.length - 1) {
                mailList.append(ADDRESS_SPLIT);
            }
        }
        return InternetAddress.parse(mailList.toString());
    }
}
