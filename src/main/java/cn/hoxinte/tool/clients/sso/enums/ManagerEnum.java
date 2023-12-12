package cn.hoxinte.tool.clients.sso.enums;


/**
 * 管理层信息
 *
 * @author dominate
 * @since 2022/09/14
 */
public enum ManagerEnum {

    /**
     * 组织架构领导
     */
    TOP_MANAGER(0, "总裁", false),
    REGION_MANAGER(1, "总监", false),
    BRANCH_MANAGER(2, "分公司负责人", false),
    DEPART_MANAGER(3, "经理", true),
    GROUP_MANAGER(4, "主管", true),
    ;

    final int code;
    final String name;

    final boolean showManager;

    ManagerEnum(int code, String name, boolean showManager) {
        this.code = code;
        this.name = name;
        this.showManager = showManager;
    }

    public boolean isShowManager() {
        return showManager;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static int parseCode(String postStr) {
        for (ManagerEnum managerEnum : ManagerEnum.values()) {
            if (managerEnum.name.contains(postStr) || postStr.contains(managerEnum.name)) {
                return managerEnum.code;
            }
        }
        return -1;
    }

    public static ManagerEnum getValueOf(int code) {
        for (ManagerEnum managerEnum : ManagerEnum.values()) {
            if (code == managerEnum.getCode()) {
                return managerEnum;
            }
        }
        return null;
    }

    public static boolean canShow(int code) {
        for (ManagerEnum managerEnum : ManagerEnum.values()) {
            if (code == managerEnum.getCode()) {
                return managerEnum.showManager;
            }
        }
        return false;
    }
}
