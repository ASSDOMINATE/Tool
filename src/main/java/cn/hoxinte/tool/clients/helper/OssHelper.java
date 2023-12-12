package cn.hoxinte.tool.clients.helper;

import cn.hoxinte.tool.clients.entity.ContentTypeEnum;
import cn.hoxinte.tool.clients.entity.OssProperties;
import cn.hoxinte.tool.utils.LoadUtil;
import cn.hoxinte.tool.utils.StringUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 对接阿里云OSS工具
 *
 * @author dominate
 * @since 2022/12/16
 */
@Slf4j
public class OssHelper {

    private static final String ENDPOINT = LoadUtil.getProperty("aliyun.oss.endpoint");
    private static final String BUCKET_NAME = LoadUtil.getProperty("aliyun.oss.bucket-name");
    private static final String ACCESS_KEY = LoadUtil.getProperty("aliyun.oss.access-key");
    private static final String SECRET_KEY = LoadUtil.getProperty("aliyun.oss.secret-key");

    private static final char C_DOT = '.';
    private static final String SLASH = "/";


    private static OSS build() {
        return new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY, SECRET_KEY);
    }

    private static void checkBucket(OSS client) {
        if (!client.doesBucketExist(BUCKET_NAME)) {
            client.createBucket(BUCKET_NAME);
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(BUCKET_NAME);
            createBucketRequest.setCannedACL(CannedAccessControlList.Private);
            client.createBucket(createBucketRequest);
        }
    }

    public static OssProperties getProperties() {
        OssProperties ossProperties = new OssProperties();
        ossProperties.setAccessKeyId(ACCESS_KEY);
        ossProperties.setAccessKeySecret(SECRET_KEY);
        ossProperties.setEndPoint(ENDPOINT);
        ossProperties.setBucketName(BUCKET_NAME);
        return ossProperties;
    }

    public static String uploadFile(File file, String targetUrl) {
        try {
            return upload(new FileInputStream(file), targetUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.EMPTY;
    }

    /**
     * 上传文件
     *
     * @param is        输入流
     * @param targetUrl 文件路径
     * @return 文件路径
     */
    public static String upload(InputStream is, String targetUrl) {
        OSS client = build();
        try {
            checkBucket(client);
            PutObjectResult result = client.putObject(BUCKET_NAME, targetUrl, is);
            if (result != null) {
                return targetUrl;
            }
        } catch (OSSException oe) {
            oe.printStackTrace();
        } finally {
            client.shutdown();
        }
        return StringUtil.EMPTY;
    }

    /**
     * 读取OSS文件byte数组
     *
     * @param url 文件路径
     */
    public static byte[] downloadByte(String url) {
        OSS client = build();
        //读取数据
        OSSObject ossObject = client.getObject(BUCKET_NAME, url);
        //使用bytes数组存贮输入流
        byte[] bytes = new byte[0];
        try (InputStream in = ossObject.getObjectContent()) {
            bytes = in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
        return bytes;
    }


    /**
     * 生成OSS下载文件地址
     *
     * @param targetUrl 文件地址
     * @return 下载地址
     */
    public static String createDownloadPath(String targetUrl) {
        return BUCKET_NAME + C_DOT + ENDPOINT + SLASH + targetUrl;
    }


    /**
     * 删除OSS单个文件
     *
     * @param objectName 阿里云上文件路径
     */
    public static void del(String objectName) {
        // 创建client实例。
        OSS client = build();
        // 判断当前文件url 是否存在
        if (!client.doesObjectExist(BUCKET_NAME, objectName)) {
            client.shutdown();
            return;
        }
        // 删除文件。
        client.deleteObject(BUCKET_NAME, objectName);
        // 关闭client。
        client.shutdown();
    }

    /**
     * 删除OSS文件
     *
     * @param url 阿里云链接
     */
    public static void delByUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return;
        }
        //获取<myObjectName>
        String objectName = url.substring(url.indexOf(ENDPOINT) + ENDPOINT.length() + 1);
        del(objectName);
    }

    /**
     * 批量删除OSS文件
     *
     * @param urlList 阿里云上文件
     */
    public static void delByUrls(List<String> urlList) {
        // 创建client实例。
        OSS client = build();
        List<String> keyList = new ArrayList<>(urlList.size());
        for (String url : urlList) {
            String key = url.substring(url.indexOf(ENDPOINT) + ENDPOINT.length() + 1);
            keyList.add(key);
        }
        // 删除请求
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(BUCKET_NAME);
        // 赋值需要删除的文件
        deleteRequest.setKeys(keyList);
        // 调用删除
        client.deleteObjects(deleteRequest);
        // 关闭client。
        client.shutdown();
    }


    /**
     * 处理文件类型,用于文件存储时设置对应存储类型和访问权限
     *
     * @param filenameExtension 文件扩展名
     * @return 文件对应的 contentType
     */
    public static String getContentType(String filenameExtension) {
        if (StringUtil.isEmpty(filenameExtension)) {
            return ContentTypeEnum.DEFAULT.getContentType();
        }
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.values()) {
            for (String extension : contentTypeEnum.getExtensions()) {
                if (extension.equalsIgnoreCase(filenameExtension)) {
                    return contentTypeEnum.getContentType();
                }
            }
        }
        return ContentTypeEnum.DEFAULT.getContentType();
    }


    /**
     * byte[]转MultipartFile
     *
     * @param bytes byte数组
     * @return MultipartFile文件
     */
    private static MockMultipartFile byte2MultipartFile(byte[] bytes, String name) {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        MockMultipartFile file = null;
        try {
            file = new MockMultipartFile(name, name, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        } catch (IOException e) {
            log.error("",e);
        }
        return file;
    }

    public static void uploadFile(String url, InputStream inputStream) {
        OSS client = build();
        client.putObject(BUCKET_NAME, url, inputStream);
    }
}
