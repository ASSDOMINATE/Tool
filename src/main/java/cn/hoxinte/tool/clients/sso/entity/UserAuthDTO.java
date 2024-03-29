package cn.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

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
public class UserAuthDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    private String token;

    /**
     * 账号ID
     */
    private Integer accountId;

    /**
     * 别名
     */
    private String alias;

    /**
     * 名字 最大长度45
     */
    private String name;

    /**
     * 电话 最大长度45
     */
    private String phone;

    /**
     * 邮箱 最大长度45
     */
    private String email;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 头像地址 最大长度1024
     */
    private String avatar;

    /**
     * 用户所拥有的权限ID数组
     */
    private List<Integer> permissions;

    private Integer platformId;

    private Integer tenantId;


    public UserAuthDTO() {
    }


}
