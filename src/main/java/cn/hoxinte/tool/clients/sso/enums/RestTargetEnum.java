package cn.hoxinte.tool.clients.sso.enums;

import cn.hoxinte.tool.utils.HttpUtil;

/**
 * Restful 请求
 *
 * @author dominate
 * @since 2022/9/9
 */
public enum RestTargetEnum {
    /**
     * 每个请求目标及其参数
     */
    AUTH_VERIFY(RestResourceEnum.AUTH, params("verify"), params("token", "path")),

    USER_LIST(RestResourceEnum.USER, params(), params("ids")),
    USER_PAGE(RestResourceEnum.USER, params("page"), params("index", "size")),
    USER_DETAIL_PAGE(RestResourceEnum.USER, params("detailPage"), params("index", "size")),
    USER_LOWER_ID_LIST(RestResourceEnum.USER, params("", "lower"), params()),
    USER_CHECK_DEPT(RestResourceEnum.USER, params("", "checkDept"), params("deptIds")),
    USER_KEYWORD(RestResourceEnum.USER, params("keyword"), params()),
    USER_DETAIL(RestResourceEnum.USER, params(""), params()),

    DEPT_LIST(RestResourceEnum.DEPT, params(), params()),
    DEPT_CHILD_LIST(RestResourceEnum.DEPT, params(""), params("getAll")),
    DEPT_USER_LIST(RestResourceEnum.DEPT, params("", "users"), params()),
    DEPT_USER_ID_LIST(RestResourceEnum.DEPT, params("", "userIds"), params()),
    DEPT_USER_DESR_LIST(RestResourceEnum.DEPT, params("userDesr"), params("accountIds")),
    DEPT_ALL_USER_ID_LIST(RestResourceEnum.DEPT, params("", "allUserIds"), params()),
    DEPT_DESR_LIST(RestResourceEnum.DEPT, params("desr"), params("ids")),

    CTI_RELATE_LIST(RestResourceEnum.CTI, params("", "relate"), params()),

    PERM_HAS_USER(RestResourceEnum.PERM, params(""), params("accountId")),

    PLATFORM_ROLES_CHECK_LIST(RestResourceEnum.PLATFORM, params("", "rolesCheck"), params("path")),
    PLATFORM_PERM_CHECK_LIST(RestResourceEnum.PLATFORM, params("", "hasPerm",""), params("ids")),

    ;


    final RestResourceEnum resource;
    final String[] paths;
    final String[] params;


    RestTargetEnum(RestResourceEnum resource, String[] paths, String[] params) {
        this.resource = resource;
        this.paths = paths;
        this.params = params;
    }

    public String[] getPaths(String[] urlValues) {
        if (urlValues.length == 0) {
            return this.paths;
        }
        String[] thisPaths = this.paths;
        String[] targetPaths = new String[thisPaths.length];
        int valueIndex = 0;
        for (int i = 0; i < thisPaths.length; i++) {
            if (thisPaths[i].length() > 0) {
                targetPaths[i] = thisPaths[i];
                continue;
            }
            targetPaths[i] = urlValues[valueIndex];
            valueIndex++;
        }
        return targetPaths;
    }

    public String createUrl(String serverUrl, String[] urlValues, String[] paramValues) {
        String url = this.resource.getSourceUrl(serverUrl);
        String requestUrl = createRestUrl(url, this.getPaths(urlValues));
        if (paramValues.length == 0) {
            return requestUrl;
        }
        String params = HttpUtil.createParamUri(this.params, paramValues);
        return requestUrl + PARAM_APPEND + params;
    }

    // 工具

    private static final String PATH_SPLIT = "/";
    private static final String PARAM_APPEND = "?";


    private static String createRestUrl(String url, String... params) {
        StringBuilder uri = new StringBuilder();
        for (String param : params) {
            uri.append(param);
            uri.append(PATH_SPLIT);
        }
        if (url.endsWith(PATH_SPLIT)) {
            return url + uri;
        }
        return url + PATH_SPLIT + uri;
    }


    private static String[] params(String... values) {
        return values;
    }

}
