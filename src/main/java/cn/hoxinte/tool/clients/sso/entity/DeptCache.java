package cn.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 部门信息
 * </p>
 *
 * @author dominate
 * @since 2022-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeptCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 名称
     */
    private String name;

    /**
     * 部门描述
     */
    private String desr;

    /**
     * 父部门ID
     */
    private Integer parentId;

    /**
     * 部门领导Map key managerCode
     */
    private Map<Integer, Leader> leaderMap;

    /**
     * 领导描述
     */
    private String leaderDesr;

    @Data
    public static class Leader implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        private Integer userId;
        /**
         * 用户名称
         */
        private String userName;
        /**
         * 唯一标识
         */
        private String uniqueCode;
    }

}
