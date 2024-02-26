package cn.hoxinte.tool.clients.sso;


import cn.hoxinte.tool.clients.helper.AuthHelper;
import cn.hoxinte.tool.clients.sso.enums.RestTargetEnum;
import cn.hoxinte.tool.utils.LoadUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SSO请求客户端
 *
 * @author dominate
 * @since 2022/9/9
 */
public class SsoClient {

    /**
     * SSO服务器地址
     */
    private static final String SSO_SERVER = LoadUtil.getProperty("hoxinte.sso.host");
    /**
     * 拉取列表大小限制
     */
    private static final int REQUEST_LIST_LIMIT = LoadUtil.getIntegerProperty("hoxinte.sso.limit");
    /**
     * 平台ID
     */
    private static final int SET_PLATFORM_ID = LoadUtil.getIntegerProperty("hoxinte.sso.platform-id");

    private static final String PARAM_SPLIT = ",";
    private static final String EMPTY_STRING = "";

    private static final String HEADER_APP_TENANT = "App-Tenant";
    private static final String HEADER_TOKEN = "X-User-Token";
    private static final String HEADER_API_TOKEN = "X-App-Token";


    // 封装外部请求

    /**
     * 校验数据权限
     *
     * @param token 用户标识
     * @param path  权限路径
     * @return Json
     */
    public static String requestVerify(String token, String path) {
        // auth/verify + param token + path
        return sendGetWithParam(RestTargetEnum.AUTH_VERIFY, token, path);
    }

    /**
     * 请求用户列表
     *
     * @param userIds 用户ID
     * @return Json
     */
    public static String requestUserList(Integer... userIds) {
        // user/ + param ids=userIds
        return sendGetWithParam(RestTargetEnum.USER_LIST, parseArrayStr(userIds));
    }

    /**
     * 请求用户数据列表
     *
     * @param index 分页位置
     * @return Json
     */
    public static String requestUserList(int index) {
        // user/page + param index + size
        return sendGetWithParam(RestTargetEnum.USER_PAGE, String.valueOf(index), String.valueOf(REQUEST_LIST_LIMIT));
    }

    /**
     * 请求用户详细数据列表
     *
     * @param index 分页位置
     * @return Json
     */
    public static String requestUserDetailList(int index) {
        // user/page + param index + size
        return sendGetWithParam(RestTargetEnum.USER_DETAIL_PAGE, String.valueOf(index), String.valueOf(REQUEST_LIST_LIMIT));
    }

    /**
     * 请求部门下用户列表
     *
     * @param deptId 部门ID
     * @return Json
     */
    public static String requestDeptUserList(int deptId) {
        // dept/&deptId$/users
        return sendGetWithValue(RestTargetEnum.DEPT_USER_LIST, String.valueOf(deptId));
    }


    /**
     * 请求该部门下用户ID列表
     *
     * @param deptId 部门ID
     * @return Json
     */
    public static String requestDeptUserIdList(int deptId) {
        // dept/&deptId$/userIds
        return sendGetWithValue(RestTargetEnum.DEPT_USER_ID_LIST, String.valueOf(deptId));
    }

    /**
     * 查询用户所在部门下的所有用户
     *
     * @param userId 用户ID
     * @return Json
     */
    public static String requestLowerIdList(int userId) {
        // user/&userId$/lower
        return sendGetWithValue(RestTargetEnum.USER_LOWER_ID_LIST, String.valueOf(userId));
    }

    /**
     * 判断用户是否在部门下
     *
     * @param userId  用户ID
     * @param deptIds 部门ID列表
     * @return Json
     */
    public static String requestUserCheckDept(int userId, Integer... deptIds) {
        return sendGet(RestTargetEnum.USER_CHECK_DEPT, params(String.valueOf(userId)), params(parseArrayStr(deptIds)));
    }


    /**
     * 请求根据关键字查询用户ID列表
     *
     * @param keyword 关键字
     * @param token   请求用户Token
     * @return Json
     */
    public static String requestSearchUserList(String keyword, String token) {
        // user/$keyword$
        return sendGetWithValue(RestTargetEnum.USER_KEYWORD, createTokenHeader(token), keyword);
    }

    /**
     * 请求根据关键字查询用户ID列表
     *
     * @param keyword 关键字
     * @param token   请求用户Token
     * @return Json
     */
    public static String requestSearchUserIdList(String keyword, String token) {
        // user/$keyword$
        return sendGetWithValue(RestTargetEnum.USER_ID_KEYWORD, createTokenHeader(token), keyword);
    }

    public static String requestUserDetail(Integer userId) {
        // user/$id$
        return sendGetWithValue(RestTargetEnum.USER_DETAIL, String.valueOf(userId));
    }

    /**
     * 请求部门下全部部门的用户ID列表
     *
     * @param deptId 部门ID
     * @return Json
     */
    public static String requestDeptAllUserIdList(int deptId) {
        // dept/$deptId$/allUserIds
        return sendGetWithValue(RestTargetEnum.DEPT_ALL_USER_ID_LIST, String.valueOf(deptId));
    }

    /**
     * 请求用户部门描述
     *
     * @param userIds 用户ID列表
     * @return Json
     */
    public static String requestUserDeptDesrMap(Integer... userIds) {
        // dept/userDesr + param ids=userIds
        return sendGetWithParam(RestTargetEnum.DEPT_USER_DESR_LIST, parseArrayStr(userIds));
    }

    /**
     * 请求部门描述
     *
     * @param ids 部门ID列表
     * @return key 部门ID value 部门描述
     */
    public static String requestDeptDesrMap(Integer... ids) {
        // dept/desr + param ids
        return sendGetWithParam(RestTargetEnum.DEPT_DESR_LIST, parseArrayStr(ids));
    }

    /**
     * 请求CTI关联
     *
     * @param ctiCode CTI编码
     * @return 关联
     */
    public static String requestCtiRelate(int ctiCode) {
        // cti/$ctiCode$/relate
        return sendGetWithValue(RestTargetEnum.CTI_RELATE_LIST, String.valueOf(ctiCode));
    }

    /**
     * 请求平台下角色权限判断列表
     *
     * @param path 权限路径
     * @return Json
     */
    public static String requestRolesCheck(String path) {
        return sendGet(RestTargetEnum.PLATFORM_ROLES_CHECK_LIST, params(String.valueOf(SET_PLATFORM_ID)), params(path));
    }

    /**
     * 请求平台下 有权限的用户
     *
     * @param path 权限路径
     * @param ids  请求用户ID列表
     * @return Json 有该权限的用户列表
     */
    public static String requestHasPerm(String path, Integer... ids) {
        return sendGet(RestTargetEnum.PLATFORM_PERM_CHECK_LIST, params(String.valueOf(SET_PLATFORM_ID), path), params(parseArrayStr(ids)));
    }

    /**
     * 权限下是否有用户
     *
     * @param permId 权限ID
     * @param userId 用户ID
     * @return 权限下是否有用户
     */
    public static String requestPermHasUser(int permId, int userId) {
        return sendGet(RestTargetEnum.PERM_HAS_USER, params(String.valueOf(permId)), params(String.valueOf(userId)));
    }

    /**
     * 读取所有部门
     *
     * @return 所有部门列表
     */
    public static String loadAllDept() {
        return sendGetWithValue(RestTargetEnum.DEPT_LIST);
    }

    public static String loadDept(String token) {
        return sendGetWithValue(RestTargetEnum.DEPT_LIST, createTokenHeader(token));
    }

    public static String loadDept(int accountId) {
        return sendGetWithValue(RestTargetEnum.DEPT_LIST_BY_USER, params(String.valueOf(accountId)));
    }

    /**
     * 读取子部门
     *
     * @param deptId 部门ID
     * @param getAll 是否获取全部
     * @return 子部门列表
     */
    public static String loadChildDept(int deptId, boolean getAll) {
        return sendGet(RestTargetEnum.DEPT_CHILD_LIST, params(String.valueOf(deptId)), params(String.valueOf(getAll)));
    }

    // 封装工请求具

    private static String sendGetWithParam(RestTargetEnum restfulEnum, String... paramValues) {
        return sendGetWithParam(restfulEnum, Collections.emptyMap(), paramValues);
    }

    private static String sendGetWithParam(RestTargetEnum restfulEnum, Map<String, String> headerMap, String... paramValues) {
        return sendGet(restfulEnum, headerMap, new String[]{}, paramValues);
    }

    private static String sendGetWithValue(RestTargetEnum restfulEnum, String... urlValues) {
        return sendGetWithValue(restfulEnum, Collections.emptyMap(), urlValues);
    }

    private static String sendGetWithValue(RestTargetEnum restfulEnum, Map<String, String> headerMap, String... urlValues) {
        return sendGet(restfulEnum, headerMap, urlValues, new String[]{});
    }

    private static String sendGet(RestTargetEnum restfulEnum, String[] urlValues, String[] paramValues) {
        return sendGet(restfulEnum, Collections.emptyMap(), urlValues, paramValues);
    }

    private static String sendGet(RestTargetEnum restfulEnum, Map<String, String> headerMap, String[] urlValues, String[] paramValues) {
        String url = restfulEnum.createUrl(SSO_SERVER, urlValues, paramValues);
        return sendGet(url, headerMap);
    }


    private static String parseArrayStr(Integer... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if (i < values.length - 1) {
                builder.append(PARAM_SPLIT);
            }
        }
        return builder.toString();
    }

    private static String[] params(String... values) {
        return values;
    }

    private static Map<String, String> createTokenHeader(String token) {
        return createHeader(new String[]{HEADER_TOKEN}, new String[]{token});
    }

    private static Map<String, String> createHeader(String[] keys, String[] values) {
        Map<String, String> headerMap = new HashMap<String, String>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            headerMap.put(keys[i], values[i]);
        }
        return headerMap;
    }

    private static String sendGet(String url, Map<String, String> headerMap) {
        var client = HttpClient.newHttpClient();
        var requestBuilder = HttpRequest.newBuilder(URI.create(url));
        for (Map.Entry<String, String> header : headerMap.entrySet()) {
            requestBuilder.setHeader(header.getKey(), header.getValue());
        }
        // TODO API请求头的封装 暂时用的Jwt校验
        requestBuilder.setHeader(HEADER_API_TOKEN, AuthHelper.createApiToken());
        var request = requestBuilder.GET().build();
        try {
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            return response.get().body();
        } catch (Exception e) {
            return EMPTY_STRING;
        }
    }

    private static boolean isEmpty(Object str) {
        return str == null || EMPTY_STRING.equals(str);
    }

}
