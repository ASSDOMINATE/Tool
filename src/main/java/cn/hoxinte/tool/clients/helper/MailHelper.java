package cn.hoxinte.tool.clients.helper;

import cn.hoxinte.tool.clients.entity.MailSend;
import cn.hoxinte.tool.clients.redis.RedisClient;
import cn.hoxinte.tool.utils.LoadUtil;
import cn.hoxinte.tool.utils.MailUtil;
import cn.hoxinte.tool.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * 邮件发送工具
 * <p>
 * 可通过 sendMail 直接发送邮件
 * 推荐使用 Redis队列发送模式，通过 addToQueue 添加队列，通过定时执行 sendByQueue 发送队列中的所有邮件
 *
 * @author dominate
 * @since 2022/10/11
 */
@Slf4j
public final class MailHelper {


    /**
     * 阿里云服务器限制25端口
     * <p>
     * 网易企业邮箱
     * smtp.qiye.163.com 25
     * itmessage@huaweijinan.com - HWJAit135=
     * <p>
     * <p>
     * HotMail 免费邮箱
     * smtp.office365.com 587
     * haiwaicuishou@hotmail.com - Hw123456
     * haiwai0001@hotmail.com/haiwai0002@hotmail.com/haiwai0003@hotmail.com/haiwai0004@hotmail.com - Haiwaicuishou
     */
    private static final String[] EMAIL_ACCOUNTS = LoadUtil.getArrayProperty("mail.accounts");
    private static final String[] EMAIL_PASSWORDS = LoadUtil.getArrayProperty("mail.passwords");
    private static final int TRY_COUNT = EMAIL_ACCOUNTS.length;

    private static final String EMAIL_NAME = LoadUtil.getProperty("mail.send.name");
    private static final String SMTP_HOST = LoadUtil.getProperty("mail.smtp-host");
    private static final int PORT = LoadUtil.getIntegerProperty("mail.port");

    private static final String MAIL_SEND_QUEUE_KEY = "queue:mail:send";


    /**
     * 发送当前队列里的所有邮件
     */
    public static void sendByQueue() {
        long queueLength = RedisClient.listLength(MAIL_SEND_QUEUE_KEY);
        if (0 >= queueLength) {
            return;
        }
        log.info("send mail by queue, send target count {}", queueLength);
        int successCount = 0;
        for (long i = 0; i < queueLength; i++) {
            MailSend send = (MailSend) RedisClient.leftPop(MAIL_SEND_QUEUE_KEY);
            if (sendMail(send.getSubject(), send.getText(), send.getImgUrls(), send.getAccounts())) {
                successCount++;
                continue;
            }
            // 发送失败
            log.info("send failed check email {}, now queue length {}", send.getAccounts()[0], queueLength);
        }
        log.info("send mail success {}, failed {}, total {}", successCount, queueLength - successCount, queueLength);
    }


    /**
     * for Test Mail Account
     *
     * @param subject     题目
     * @param text        内容
     * @param imgUrls     图片地址
     * @param receiveMail 接收邮件地址
     */
    public static void sendAllMail(String subject, String text, String[] imgUrls, String... receiveMail) {
        for (int i = 0; i < TRY_COUNT; i++) {
            MailUtil.sendMail(SMTP_HOST, PORT, EMAIL_NAME, EMAIL_ACCOUNTS[i], EMAIL_PASSWORDS[i], subject, text, imgUrls, receiveMail);
        }
    }


    private static boolean addToQueue(String subject, String text, String[] imgUrls, String... account) {
        long queueLength = RedisClient.rightPush(MAIL_SEND_QUEUE_KEY, new MailSend(subject, text, imgUrls, account));
        log.info("add Mail to Redis queue, now queue length {}", queueLength);
        return queueLength > 0;
    }

    public static boolean sendMail(String subject, String text, String... receiveMailAccount) {
        return addToQueue(subject, text, new String[0], receiveMailAccount);
    }

    private static boolean sendMail(String subject, String text, String[] imgUrls, String... receiveMailAccount) {
        if (TRY_COUNT == 1) {
            return MailUtil.sendMail(SMTP_HOST, PORT, EMAIL_NAME, EMAIL_ACCOUNTS[0], EMAIL_PASSWORDS[0], subject, text,
                    imgUrls, receiveMailAccount);
        }
        Set<Integer> usedIndex = new HashSet<>(TRY_COUNT);
        for (int i = 0; i < TRY_COUNT; i++) {
            int randMailIndex = getMailRandIndex(usedIndex);
            if (MailUtil.sendMail(SMTP_HOST, PORT, EMAIL_NAME, EMAIL_ACCOUNTS[randMailIndex],
                    EMAIL_PASSWORDS[randMailIndex], subject, text, imgUrls, receiveMailAccount)) {
                return true;
            }
        }
        return false;
    }

    private static int getMailRandIndex(Set<Integer> usedIndex) {
        int randIndex = RandomUtil.getRandNum(0, TRY_COUNT);
        if (usedIndex.contains(randIndex)) {
            return getMailRandIndex(usedIndex);
        }
        usedIndex.add(randIndex);
        return randIndex;
    }


}
