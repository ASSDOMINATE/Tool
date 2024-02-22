package cn.hoxinte.tool.clients.sso.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门数据 DTO
 *
 * @author dominate
 * @since 2022/02/25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeptTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 父部门ID
     */
    private Integer parentId;

    /**
     * 名称 最大长度45
     */
    private String name;

    /**
     * 描述 最大长度256
     */
    private String desr;

    /**
     * 排序数
     */
    private Integer seq;

    /**
     * 子部门
     */
    private List<DeptTreeDTO> childrenDeptList;


    public DeptTreeDTO(){
        childrenDeptList = new ArrayList<>();
    }

}
