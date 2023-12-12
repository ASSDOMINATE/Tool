package com.hoxinte.tool.clients.sso;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.hoxinte.tool.clients.sso.entity.*;
import com.hoxinte.tool.utils.JsonUtil;

import java.lang.reflect.Type;
import java.util.*;

/**
 * SSO 解析工具
 *
 * @author dominate
 * @since 2022/9/13
 */
public class ParseUtil {

    private static final String EMPTY_STRING = "";

    private static final String[] PARSE_RESPONSE_DATA = {"data"};
    private static final String[] PARSE_RESPONSE_ROLES = {"data", "roles"};

    public static <T> List<T> objectToList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            for (Object o : list) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    public static List<UserInfoDTO> parseUserInfoListResponse(String response) {
        return parseListResponse(response, UserInfoDTO.class, PARSE_RESPONSE_DATA);
    }

    public static List<RoleDTO> parseUserRoleListResponse(String response) {
        return parseListResponse(response, RoleDTO.class, PARSE_RESPONSE_ROLES);
    }

    public static List<CtiRelateDTO> parseCtiRelateListResponse(String response) {
        return parseListResponse(response, CtiRelateDTO.class, PARSE_RESPONSE_DATA);
    }

    public static List<DeptDTO> parseDeptListResponse(String response) {
        return parseListResponse(response, DeptDTO.class, PARSE_RESPONSE_DATA);
    }

    public static List<DeptUserDTO> parseUserListResponse(String response) {
        return parseListResponse(response, DeptUserDTO.class, PARSE_RESPONSE_DATA);
    }

    public static List<RolePermCheckDTO> parseRoleCheckListResponse(String response) {
        return parseListResponse(response, RolePermCheckDTO.class, PARSE_RESPONSE_DATA);
    }

    public static List<Integer> parseIdListResponse(String response) {
        if (isEmpty(response)) {
            return Collections.emptyList();
        }
        JSONArray data = JsonUtil.parseResponseValueForJsonArray(response, PARSE_RESPONSE_DATA);
        if (data == null) {
            return Collections.emptyList();
        }
        List<Integer> idList = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            idList.add(data.getInteger(i));
        }
        return idList;
    }

    public static <T> List<T> parseListResponse(String response, Class<T> tClass, String... keys) {
        if (isEmpty(response)) {
            return Collections.emptyList();
        }
        JSONArray data = JsonUtil.parseResponseValueForJsonArray(response, keys);
        return JsonUtil.parseResult(data, tClass);
    }

    public static Map<Integer, String> parseIntegerMapResponse(String response) {
        Map<String, String> map = parseMapResponse(response);
        Map<Integer, String> integerMap = new HashMap<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            integerMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return integerMap;
    }

    private final static Type MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
    }.getType();

    public static Map<String, String> parseMapResponse(String response) {
        if (isEmpty(response)) {
            return Collections.emptyMap();
        }
        String data = JsonUtil.parseResponseValueForString(response, PARSE_RESPONSE_DATA);
        if (isEmpty(data)) {
            return Collections.emptyMap();
        }
        return JsonUtil.jsonToMap(data, MAP_STRING_STRING);
    }

    public static boolean parseBoolean(String response) {
        return Boolean.TRUE.equals(JsonUtil.parseResponseValueForBoolean(response, PARSE_RESPONSE_DATA));
    }

    private static boolean isEmpty(Object str) {
        return str == null || EMPTY_STRING.equals(str);
    }

}
