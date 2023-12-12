package com.hoxinte.tool.clients.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 邮件发送对象
 *
 * @author dominate
 * @since 2022/10/24
 */
@Data
@Accessors(chain = true)
public class MailSend implements Serializable {
    private static final long serialVersionUID = 1L;
    private String subject;
    private String text;
    private String[] imgUrls;
    private String[] accounts;
    public MailSend() { }
    public MailSend(String subject, String text, String[] imgUrls, String... accounts) {
        this.subject = subject;
        this.text = text;
        this.imgUrls = imgUrls;
        this.accounts = accounts;
    }
}
