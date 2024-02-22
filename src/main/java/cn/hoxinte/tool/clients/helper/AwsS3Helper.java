package cn.hoxinte.tool.clients.helper;

import cn.hoxinte.tool.utils.LoadUtil;
import cn.hoxinte.tool.utils.StringUtil;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dominate
 * @since 2022/8/9
 */
public class AwsS3Helper {

    protected static final String OUTSIDE = LoadUtil.getProperty("aws.s3.outside");
    protected static final String ENDPOINT = LoadUtil.getProperty("aws.s3.endpoint");
    protected static final String BUCKET = LoadUtil.getProperty("aws.s3.bucket");
    private static final String ACCESS_KEY = LoadUtil.getProperty("aws.s3.access-key");
    private static final String SECRET_KEY = LoadUtil.getProperty("aws.s3.secret-key");
    private static final String PATH_SPLIT = "/";
    private static final String BASE_URL = OUTSIDE + PATH_SPLIT + BUCKET + PATH_SPLIT;

    private static final AWSStaticCredentialsProvider CREDENTIALS;

    static {
        CREDENTIALS = new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
    }

    public static String parseInsideUrl(String url) {
        if (url.contains(OUTSIDE)) {
            return url.replace(OUTSIDE, ENDPOINT);
        }
        return url;
    }

    public static String parseKey(String url) {
        return url.replace(BASE_URL, StringUtil.EMPTY);
    }

    public static String parseUrl(String key) {
        return BASE_URL + key;
    }

    public static boolean existed(String key) {
        try {
            AmazonS3 s3Client = getClient();
            return s3Client.doesObjectExist(BUCKET, key);
        } catch (Exception e) {
            // check error
            return false;
        }
    }

    public static String upload(String key, File file) {
        try {
            AmazonS3 s3Client = getClient();
            if (s3Client.doesObjectExist(BUCKET, key)) {
                return parseUrl(key);
            }
            s3Client.putObject(BUCKET, key, file);
            return parseUrl(key);
        } catch (Exception e) {
            // upload error
            return StringUtil.EMPTY;
        }
    }

    public static String upload(String key, InputStream inputStream, ObjectMetadata objectMetadata) {
        try {
            AmazonS3 s3Client = getClient();
            if (s3Client.doesObjectExist(BUCKET, key)) {
                return parseUrl(key);
            }
            s3Client.putObject(BUCKET, key, inputStream, objectMetadata);
            return parseUrl(key);
        } catch (Exception e) {
            // upload error
            return StringUtil.EMPTY;
        }
    }

    public static void deleteByUrl(String... urls) {
        try {
            AmazonS3 s3Client = getClient();
            String[] keys = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                keys[i] = parseKey(urls[i]);
            }
            s3Client.deleteObjects(new DeleteObjectsRequest(BUCKET).withKeys(keys));
        } catch (Exception e) {
            // delete error
        }
    }

    public static void delete(String... keys) {
        try {
            AmazonS3 s3Client = getClient();
            s3Client.deleteObjects(new DeleteObjectsRequest(BUCKET).withKeys(keys));
        } catch (Exception e) {
            // delete error
        }
    }

    public static List<S3ObjectSummary> listObject() {
        AmazonS3 s3Client = getClient();
        ObjectListing objList = s3Client.listObjects(BUCKET);
        List<S3ObjectSummary> list = new ArrayList<>();
        do {
            list.addAll(objList.getObjectSummaries());
            objList = s3Client.listNextBatchOfObjects(objList);
        } while (objList.isTruncated());

        return list;
    }

    protected static AmazonS3 getClient() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(CREDENTIALS)
                .withEndpointConfiguration(
                        new AwsClientBuilder
                                .EndpointConfiguration(ENDPOINT, Regions.CN_NORTH_1.getName())
                ).build();
    }
}
