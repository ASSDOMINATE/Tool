package com.hoxinte.tool.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author dominate
 * @since 2022/12/16
 */
public class JsonUtil {
    private static final String EMPTY_STRING = "";


    public static <T> List<T> parseListResponse(String response, Class<T> tClass, String... keys) {
        if (isEmpty(response)) {
            return Collections.emptyList();
        }
        JSONArray data = parseResponseValueForJsonArray(response, keys);
        return parseResult(data,tClass);
    }

    public static <T> List<T> parseResult(JSONArray data, Class<T> tClass) {
        if (data == null) {
            return Collections.emptyList();
        }
        List<T> resultList = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            resultList.add(JSONObject.toJavaObject(data.getJSONObject(i), tClass));
        }
        return resultList;
    }


    public static Object parseResponseValueForJson(String jsonResponse, String... keys) {
        JSONObject resultJson = JSONObject.parseObject(jsonResponse);
        for (int i = 0; i < keys.length; i++) {
            if (keys.length - 1 == i) {
                return resultJson.get(keys[i]);
            }
            resultJson = resultJson.getJSONObject(keys[i]);
        }
        return null;
    }

    public static JSONArray parseResponseValueForJsonArray(String jsonResponse, String... keys) {
        JSONObject resultJson = JSONObject.parseObject(jsonResponse);
        for (int i = 0; i < keys.length; i++) {
            if (keys.length - 1 == i) {
                return resultJson.getJSONArray(keys[i]);
            }
            resultJson = resultJson.getJSONObject(keys[i]);
        }
        return null;
    }

    public static JSONObject parseResponseValueForJsonObject(String jsonResponse, String... keys) {
        JSONObject resultJson = JSONObject.parseObject(jsonResponse);
        for (int i = 0; i < keys.length; i++) {
            if (keys.length - 1 == i) {
                return resultJson.getJSONObject(keys[i]);
            }
            resultJson = resultJson.getJSONObject(keys[i]);
        }
        return null;
    }

    public static String parseResponseValueForString(String jsonResponse, String... keys) {
        JSONObject resultJson = JSONObject.parseObject(jsonResponse);
        for (int i = 0; i < keys.length; i++) {
            if (keys.length - 1 == i) {
                return resultJson.getString(keys[i]);
            }
            resultJson = resultJson.getJSONObject(keys[i]);
        }
        return null;
    }

    public static Boolean parseResponseValueForBoolean(String jsonResponse, String... keys) {
        JSONObject resultJson = JSONObject.parseObject(jsonResponse);
        for (int i = 0; i < keys.length; i++) {
            if (keys.length - 1 == i) {
                return resultJson.getBoolean(keys[i]);
            }
            resultJson = resultJson.getJSONObject(keys[i]);
        }
        return null;
    }


    public static <T> Map<String, T> jsonToMap(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    private static boolean isEmpty(Object str) {
        return str == null || EMPTY_STRING.equals(str);
    }
}
