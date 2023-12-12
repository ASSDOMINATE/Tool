package com.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author dominate
 * @since 2022-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号ID
     */
    private Integer accountId;

    /**
     * 名称
     */
    private String name;

    /**
     * 部门描述
     */
    private String deptDesr;

    /**
     * 职位
     */
    private String position;

    /**
     * 标准岗位编码
     */
    private String standardPost;

    /**
     * 管理编码
     */
    private Integer managerCode;

    /**
     * 部门ID
     */
    private Integer deptId;


    public static UserCache defaultData() {
        final int emptyInt = 0;
        final String emptyStr = "";
        UserCache data = new UserCache();
        data.setAccountId(emptyInt);
        data.setName(emptyStr);
        data.setDeptDesr(emptyStr);
        data.setPosition(emptyStr);
        data.setStandardPost(emptyStr);
        data.setManagerCode(emptyInt);
        data.setDeptId(emptyInt);
        return data;
    }


}
