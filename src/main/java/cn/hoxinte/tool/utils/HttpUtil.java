package cn.hoxinte.tool.utils;

import cn.hoxinte.tool.clients.entity.HttpDeleteWithBody;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP 工具
 *
 * @author dominate
 * @since 2022/12/16
 */
@Slf4j
public final class HttpUtil {

    /**
     * 超时时间毫秒数
     */
    private static final int DEFAULT_TIMEOUT = 15000;
    private static final String[] CONTENT_TYPE = {"Content-Type", "application/x-www-form-urlencoded;charset=utf-8"};
    private static final String[] CONTENT_TYPE_JSON = {"Content-Type", "application/json"};
    private static final String UTF_8 = "UTF8";
    private static final String URL_PARAM_SPLIT = "&";
    private static final String URL_PARAM_CONNECT = "=";
    private static final String PARAM_APPEND = "?";
    private static final String EMPTY_RESULT = "";

    private static final String BLANK = " ";
    private static final String URL_BLANK = "%20";

    // GET

    /**
     * 发起 GET 请求
     *
     * @param url 发送请求的URL
     * @return 请求结果字符串
     */
    public static String sendGet(String url) {
        return sendGet(url, Collections.emptyMap());
    }


    /**
     * 发起 GET 请求
     *
     * @param url   发送请求的URL
     * @param param 自定参数字符串
     * @return String 请求结果字符串
     */
    public static String sendGet(String url, String param) {
        return sendGetSetHeader(url + PARAM_APPEND + param, Collections.emptyMap());
    }


    /**
     * 发起 GET 请求,支持自定义 Header
     *
     * @param url       发送请求的URL
     * @param param     参数
     * @param headerMap 消息头
     * @return String 请求结果字符串
     */
    public static String sendGet(String url, String param, Map<String, String> headerMap) {
        return sendGetSetHeader(url + PARAM_APPEND + param, headerMap);
    }


    /**
     * 发起 GET 请求
     *
     * @param url    请求路径
     * @param params 参数名
     * @param values 参数值
     * @return 请求结果
     */
    public static String sendGet(String url, String[] params, String[] values) {
        return sendGet(url, params, values, null);
    }

    /**
     * 发起 GET 请求
     *
     * @param url       请求路径
     * @param params    参数名
     * @param values    参数值
     * @param headerMap 消息头
     * @return 请求结果
     */
    public static String sendGet(String url, String[] params, String[] values, Map<String, String> headerMap) {
        String paramUrl = createParamUri(params, values);
        return sendGet(url, paramUrl, headerMap);
    }


    /**
     * 发起 GET 请求
     *
     * @param url      请求路径
     * @param paramMap 参数 Map
     * @return 请求结果
     */
    public static String sendGet(String url, Map<String, Object> paramMap) {
        return sendGet(url, paramMap, Collections.emptyMap());
    }

    /**
     * 发起 GET 请求
     *
     * @param url             请求路径
     * @param paramMap        参数 Map
     * @param customHeaderMap 消息头
     * @return 请求结果
     */
    public static String sendGet(String url, Map<String, Object> paramMap, Map<String, String> customHeaderMap) {
        return sendGet(url, paramMap, customHeaderMap, DEFAULT_TIMEOUT);
    }

    /**
     * 发起 GET 请求
     *
     * @param url             请求路径
     * @param paramMap        参数 Map
     * @param customHeaderMap 消息头
     * @param timeout         请求超时时间
     * @return 请求结果
     */
    public static String sendGet(String url, Map<String, Object> paramMap, Map<String, String> customHeaderMap, int timeout) {
        String getUrl = createUrl(url, paramMap, false);
        HttpGet httpGet = createGet(getUrl, customHeaderMap, timeout);
        return execute(httpGet);
    }

    /**
     * 发起 GET 请求,支持自定义 Header
     *
     * @param url       发送请求的URL
     * @param headerMap 消息头
     * @return String 请求结果字符串
     */
    public static String sendGetSetHeader(String url, Map<String, String> headerMap) {
        HttpGet httpGet = createGet(url, headerMap, DEFAULT_TIMEOUT);
        return execute(httpGet);
    }


    // POST




    /**
     * 发起Post请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url   目标地址
     * @param param 自定参数字符串
     * @return String 请求结果字符串
     */
    public static String sendPost(String url, String param) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CONTENT_TYPE[0], CONTENT_TYPE[1]);
        httpPost.setEntity(new StringEntity(param, UTF_8));
        return executeEntity(httpPost);
    }


    /**
     * 发起Post请求
     *
     * @param url      请求地址
     * @param paramMap 请求参数
     * @return String 请求结果字符串
     */
    public static String sendPost(String url, Map<String, Object> paramMap) {
        return sendPost(url, null, paramMap, false);
    }

    /**
     * 发起Post请求，支持自定义Header
     * 需要服务端支持这种在Entity中取值的方式
     *
     * @param url      请求地址
     * @param paramMap 请求参数
     * @param forJson  是否以JSON发送参数
     * @return String 请求结果字符串
     */
    public static String sendPost(String url, Map<String, Object> paramMap, boolean forJson) {
        return sendPost(url, null, paramMap, forJson);
    }

    /**
     * 发起Post请求，支持自定义Header
     * 需要服务端支持这种在Entity中取值的方式
     *
     * @param url             请求地址
     * @param customHeaderMap 自定义请求头
     * @param paramMap        请求参数
     * @param forJson         是否以JSON发送参数
     * @return String 请求结果字符串
     */
    public static String sendPost(String url, Map<String, String> customHeaderMap, Map<String, Object> paramMap, boolean forJson) {
        return sendPost(url, customHeaderMap, paramMap, forJson, DEFAULT_TIMEOUT);
    }

    /**
     * 发起Post请求，支持自定义Header
     * 需要服务端支持这种在Entity中取值的方式
     *
     * @param url             请求地址
     * @param customHeaderMap 自定义请求头
     * @param paramMap        请求参数
     * @param forJson         是否以JSON发送参数
     * @param timeout         请求超时时间
     * @return String 请求结果字符串
     */
    public static String sendPost(String url, Map<String, String> customHeaderMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        String postUrl = createUrl(url, paramMap, forJson);
        HttpPost httpPost = createPostBody(postUrl, customHeaderMap, paramMap, forJson, timeout);
        return executeEntity(httpPost);
    }

    /**
     * 发起Post请求，附带SSLContext认证
     * 已处理异常，出现异常返回空字符串
     *
     * @param url        目标地址
     * @param param      自定参数字符串
     * @param sslContext SSL认证
     * @return String 请求结果字符串
     */
    public static String sendPostWithSsl(String url, String param, SSLContext sslContext) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CONTENT_TYPE[0], CONTENT_TYPE[1]);
        httpPost.setEntity(new StringEntity(param, UTF_8));
        return executeEntity(httpPost, sslContext);
    }

    // PUT

    /**
     * 发起 Put 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url      目标地址
     * @param paramMap 参数map
     * @return String 请求结果字符串
     */
    public static String sendPut(String url, Map<String, Object> paramMap) {
        return sendPut(url, Collections.emptyMap(), paramMap, false);
    }

    /**
     * 发起 Put 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url      目标地址
     * @param paramMap 参数map
     * @param forJson  是否以JSON发送参数
     * @return String 请求结果字符串
     */
    public static String sendPut(String url, Map<String, Object> paramMap, boolean forJson) {
        return sendPut(url, Collections.emptyMap(), paramMap, forJson);
    }


    /**
     * 发起 Put 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url             目标地址
     * @param paramMap        参数map
     * @param customHeaderMap 自定义头
     * @param forJson         是否以JSON发送参数
     * @return String 请求结果字符串
     */
    public static String sendPut(String url, Map<String, String> customHeaderMap, Map<String, Object> paramMap, boolean forJson) {
        return sendPut(url, customHeaderMap, paramMap, forJson, DEFAULT_TIMEOUT);
    }

    /**
     * 发起 Put 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url             目标地址
     * @param paramMap        参数map
     * @param customHeaderMap 自定义头
     * @param forJson         是否以JSON发送参数
     * @param timeout         请求超时时间
     * @return String 请求结果字符串
     */
    public static String sendPut(String url, Map<String, String> customHeaderMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        String putUrl = createUrl(url, paramMap, forJson);
        HttpPut httpPut = createPutBody(putUrl, customHeaderMap, paramMap, forJson, timeout);
        return executeEntity(httpPut);
    }


    // DELETE

    /**
     * 发起 Delete 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url      请求地址
     * @param paramMap 请求参数
     * @param forJson  是否使用Json参数
     * @return String 请求结果字符串
     */
    public static String sendDelete(String url, Map<String, Object> paramMap, boolean forJson) {
        return sendDelete(url, Collections.emptyMap(), paramMap, forJson);
    }

    /**
     * 发起 Delete 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url             请求地址
     * @param customHeaderMap 自定义请求头
     * @param paramMap        请求参数
     * @param forJson         是否使用Json参数
     * @return String 请求结果字符串
     */
    public static String sendDelete(String url, Map<String, String> customHeaderMap, Map<String, Object> paramMap, boolean forJson) {
        return sendDelete(url, customHeaderMap, paramMap, forJson, DEFAULT_TIMEOUT);
    }

    /**
     * 发起 Delete 请求
     * 已处理异常，出现异常返回空字符串
     *
     * @param url             请求地址
     * @param customHeaderMap 自定义请求头
     * @param paramMap        请求参数
     * @param forJson         是否使用Json参数
     * @param timeout         请求超时时间
     * @return String 请求结果字符串
     */
    public static String sendDelete(String url, Map<String, String> customHeaderMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        String deleteUrl = createUrl(url, paramMap, forJson);
        if (forJson) {
            HttpDeleteWithBody httpDeleteWithBody = createDeleteBody(deleteUrl, customHeaderMap, paramMap, forJson, timeout);
            return executeEntity(httpDeleteWithBody);
        }
        HttpDelete httpDelete = createDelete(deleteUrl, customHeaderMap, timeout);
        return execute(httpDelete);
    }

    /**
     * 生成参数地址 URI
     *
     * @param params 参数名称数组
     * @param values 参数值数组
     * @return 参数地址 URI
     */
    public static String createParamUri(String[] params, String[] values) {
        assert params.length == values.length;
        StringBuilder paramStr = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            paramStr.append(params[i]);
            paramStr.append(URL_PARAM_CONNECT);
            paramStr.append(values[i]);
            if (i != params.length - 1) {
                paramStr.append(URL_PARAM_SPLIT);
            }
        }
        return paramStr.toString();
    }

    /**
     * 生成参数地址 URI
     *
     * @param paramMap 参数 Map
     * @return 参数地址 URI
     */
    public static String parseParamToUri(Map<String, Object> paramMap) {
        StringBuilder uri = new StringBuilder();
        for (String paramName : paramMap.keySet()) {
            if (uri.length() > 0) {
                uri.append(URL_PARAM_SPLIT);
            }
            uri.append(paramName);
            uri.append(URL_PARAM_CONNECT);
            uri.append(paramMap.get(paramName));
        }
        return uri.toString();
    }

    private static String createUrl(String baseUrl, Map<String, Object> paramMap, boolean forJson) {
        if (forJson) {
            return baseUrl;
        }
        return baseUrl + PARAM_APPEND + parseParamToUri(paramMap);
    }

    // 创建请求体


    private static HttpDeleteWithBody createDeleteBody(String url, Map<String, String> headerMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
        setEntity(httpDelete, headerMap, paramMap, forJson, timeout);
        return httpDelete;
    }

    private static HttpPost createPostBody(String url, Map<String, String> headerMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        setEntity(httpPost, headerMap, paramMap, forJson, timeout);
        return httpPost;
    }

    private static HttpPut createPutBody(String url, Map<String, String> headerMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        HttpPut httpPut = new HttpPut(url);
        setEntity(httpPut, headerMap, paramMap, forJson, timeout);
        return httpPut;
    }

    private static HttpGet createGet(String url, Map<String, String> headerMap, int timeout) {
        HttpGet httpGet = new HttpGet(url.replaceAll(BLANK, URL_BLANK));
        setEntity(httpGet, headerMap, timeout);
        return httpGet;
    }

    private static HttpDelete createDelete(String url, Map<String, String> headerMap, int timeout) {
        HttpDelete httpDelete = new HttpDelete(url.replaceAll(BLANK, URL_BLANK));
        setEntity(httpDelete, headerMap, timeout);
        return httpDelete;
    }

    // 设置请求参数

    private static void setEntity(HttpEntityEnclosingRequestBase http, Map<String, String> headerMap, Map<String, Object> paramMap, boolean forJson, int timeout) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
        http.setConfig(config);
        if (forJson && paramMap != null) {
            String jsonParam = JSON.toJSONString(paramMap);
            StringEntity requestEntity = new StringEntity(jsonParam, ContentType.APPLICATION_JSON);
            http.setEntity(requestEntity);
            http.addHeader(CONTENT_TYPE_JSON[0], CONTENT_TYPE_JSON[1]);
        }
        if (!forJson) {
            http.addHeader(CONTENT_TYPE[0], CONTENT_TYPE[1]);
        }
        if (headerMap == null) {
            return;
        }
        //添加自定义header
        for (String headerKey : headerMap.keySet()) {
            http.addHeader(headerKey, headerMap.get(headerKey));
        }
    }

    private static void setEntity(HttpRequestBase http, Map<String, String> headerMap, int timeout) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
        http.setConfig(config);
        if (null != headerMap) {
            //添加自定义header
            for (String headerKey : headerMap.keySet()) {
                http.addHeader(headerKey, headerMap.get(headerKey));
            }
        }
    }

    // 执行请求

    private static String execute(HttpRequestBase http) {
        try (CloseableHttpClient client = HttpClients.custom().build(); CloseableHttpResponse resp = client.execute(http)) {
            HttpEntity entity = resp.getEntity();
            return EntityUtils.toString(entity, UTF_8);
        } catch (Exception e) {
            log.error("send Get error {} ", http.getURI(), e);
        } finally {
            http.releaseConnection();
        }
        return EMPTY_RESULT;
    }

    private static String executeEntity(HttpEntityEnclosingRequestBase httpEntity) {
        return executeEntity(httpEntity, null);
    }

    private static String executeEntity(HttpEntityEnclosingRequestBase httpEntity, SSLContext sslContext) {
        if (Objects.isNull(sslContext)) {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpResponse response = httpclient.execute(httpEntity);
                return EntityUtils.toString(response.getEntity(), UTF_8);
            } catch (Exception e) {
                log.error("request error", e);
            } finally {
                httpEntity.releaseConnection();
            }
            return EMPTY_RESULT;
        }
        try (CloseableHttpClient httpclient = HttpClients.custom().setSslcontext(sslContext).build()) {
            HttpResponse response = httpclient.execute(httpEntity);
            return EntityUtils.toString(response.getEntity(), UTF_8);
        } catch (Exception e) {
            log.error("request error", e);
        } finally {
            httpEntity.releaseConnection();
        }
        return EMPTY_RESULT;
    }

}
