package com.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author 计泽汉
 * 领导称呼
 */
@Data
@Accessors(chain = true)
public class ManagerDTO implements Serializable {

    private Integer managerCode;
    /**
     *
     */
    private Integer userId;
    /**
     * EHR Id
     */
    private String uniqueCode;
    /**
     * 名字
     */
    private String userName;
    /**
     * 大区名
     */
    private String name;

}
