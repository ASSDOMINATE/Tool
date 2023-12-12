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
public class DeptUserDTO extends UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isLeader;

    private Integer departmentId;

    private PostDTO post;

    private Boolean isAlive;

}
