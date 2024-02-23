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
     * auth/verify/
     * param String - token
     * param String - path
     */
    AUTH_VERIFY(RestResourceEnum.AUTH, params("verify"), params("token", "path")),

    /**
     * user/
     * param Integer Array - ids
     */
    USER_LIST(RestResourceEnum.USER, params(), params("ids")),
    /**
     * user/page/
     * param Integer - index
     * param Integer - size
     */
    USER_PAGE(RestResourceEnum.USER, params("page"), params("index", "size")),
    /**
     * user/detailPage/
     * param Integer - index
     * param Integer - size
     */
    USER_DETAIL_PAGE(RestResourceEnum.USER, params("detailPage"), params("index", "size")),
    /**
     * user/{id}/lower/
     */
    USER_LOWER_ID_LIST(RestResourceEnum.USER, params("", "lower"), params()),
    /**
     * user/{id}/checkDept/
     * param Integer Array - deptIds
     */
    USER_CHECK_DEPT(RestResourceEnum.USER, params("", "checkDept"), params("deptIds")),
    /**
     * user/keyword/{content}/
     */
    USER_KEYWORD(RestResourceEnum.USER, params("keyword", ""), params()),
    /**
     * user/id/keyword/{content}/
     */
    USER_ID_KEYWORD(RestResourceEnum.USER, params("id", "keyword", ""), params()),
    /**
     * user/{id}/
     */
    USER_DETAIL(RestResourceEnum.USER, params(""), params()),

    /**
     * department/
     */
    DEPT_LIST(RestResourceEnum.DEPT, params(), params()),
    /**
     * department/list/{accountId}/
     */
    DEPT_LIST_BY_USER(RestResourceEnum.DEPT, params("list", ""), params()),
    /**
     * department/{id}/
     * param Boolean - getAll
     */
    DEPT_CHILD_LIST(RestResourceEnum.DEPT, params(""), params("getAll")),
    /**
     * department/{id}/users/
     */
    DEPT_USER_LIST(RestResourceEnum.DEPT, params("", "users"), params()),
    /**
     * department/{id}/userIds/
     */
    DEPT_USER_ID_LIST(RestResourceEnum.DEPT, params("", "userIds"), params()),
    /**
     * department/userDesr/
     * param Integer Array - accountIds
     */
    DEPT_USER_DESR_LIST(RestResourceEnum.DEPT, params("userDesr"), params("accountIds")),
    /**
     * department/{id}/allUserIds/
     */
    DEPT_ALL_USER_ID_LIST(RestResourceEnum.DEPT, params("", "allUserIds"), params()),
    /**
     * department/desr/
     * param Integer Array - ids
     */
    DEPT_DESR_LIST(RestResourceEnum.DEPT, params("desr"), params("ids")),

    /**
     * cti/{code}/relate
     */
    CTI_RELATE_LIST(RestResourceEnum.CTI, params("", "relate"), params()),

    /**
     * perm/{id}/hasUser/
     * param Integer - accountId
     */
    PERM_HAS_USER(RestResourceEnum.PERM, params("", "hasUser"), params("accountId")),

    /**
     * platform/{id}/rolesCheck/
     * param String - path
     */
    PLATFORM_ROLES_CHECK_LIST(RestResourceEnum.PLATFORM, params("", "rolesCheck"), params("path")),
    /**
     * platform/{id}/hasPerm/{path}/
     * param Integer Array - ids(accountIds)
     */
    PLATFORM_PERM_CHECK_LIST(RestResourceEnum.PLATFORM, params("", "hasPerm", ""), params("ids")),
    ;


    final RestResourceEnum resource;
    final String[] paths;
    final String[] params;


    RestTargetEnum(RestResourceEnum resource, String[] paths, String[] params) {
        this.resource = resource;
        this.paths = paths;
        this.params = params;
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

    // http固定常量

    private static final String PATH_SPLIT = "/";
    private static final String PARAM_APPEND = "?";


    private String[] getPaths(String[] urlValues) {
        if (urlValues.length == 0) {
            return this.paths;
        }
        String[] thisPaths = this.paths;
        String[] targetPaths = new String[thisPaths.length];
        int valueIndex = 0;
        for (int i = 0; i < thisPaths.length; i++) {
            // "" 是否为占位参数
            if (!thisPaths[i].isEmpty()) {
                targetPaths[i] = thisPaths[i];
                continue;
            }
            targetPaths[i] = urlValues[valueIndex];
            valueIndex++;
        }
        return targetPaths;
    }

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
