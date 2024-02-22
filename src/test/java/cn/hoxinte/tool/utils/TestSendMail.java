package cn.hoxinte.tool.utils;

import cn.hoxinte.tool.clients.helper.MailHelper;
import org.junit.Test;

/**
 * @author dominate
 * @since 2024/1/5
 **/
public class TestSendMail {

    @Test
    public void testMail(){
        MailHelper.sendAllMail("智言云科技","测试邮件",new String[]{},"547020700@qq.com");
    }
}
