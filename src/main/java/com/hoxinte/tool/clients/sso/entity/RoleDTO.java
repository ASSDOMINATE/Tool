package com.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 角色信息.
 *
 * @Author 计泽汉
 * @Date 2022/10/21
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer roleId;

    private String roleName;

    private Integer platformId;

    private String platformName;
}