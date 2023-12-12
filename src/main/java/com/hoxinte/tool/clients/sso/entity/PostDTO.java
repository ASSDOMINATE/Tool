package com.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 用户-员工职位信息
 * </p>
 *
 * @author dominate
 * @since 2022-03-21
 */
@Data
@Accessors(chain = true)
public class PostDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标准岗位编码
     */
    private String standardPost;

    /**
     * 岗位名称
     */
    private String post;

    /**
     * 职务
     */
    private String postDuty;

    /**
     * 体系
     */
    private String postSystem;

    /**
     * 族群
     */
    private String postGroup;

    /**
     * 职位
     */
    private String position;

    /**
     * 职级
     */
    private String positionClass;

    /**
     * 职等
     */
    private String positionGrade;

}
