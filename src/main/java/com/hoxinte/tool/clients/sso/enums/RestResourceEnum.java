package com.hoxinte.tool.clients.sso.enums;

/**
 * Restful 资源
 *
 * @author dominate
 * @since 2022/9/9
 */
public enum RestResourceEnum {
    /**
     * Restful 每个资源
     */
    USER("user", "用户资源"),
    AUTH("auth", "授权相关资源"),
    DEPT("department", "部门资源"),
    CTI("cti", "CTI资源"),
    PERM("perm", "权限资源"),
    PLATFORM("platform", "平台资源");

    final String path;
    final String desr;


    RestResourceEnum(String source, String desr) {
        this.path = source;
        this.desr = desr;
    }

    private static final String PATH_SPLIT = "/";
    private static final String EMPTY = "";

    public String getSourceUrl(String serverUrl) {
        return serverUrl + (serverUrl.endsWith(PATH_SPLIT) ? EMPTY : PATH_SPLIT) + path + PATH_SPLIT;
    }

}
