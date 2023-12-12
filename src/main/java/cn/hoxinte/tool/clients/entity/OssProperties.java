package cn.hoxinte.tool.clients.entity;

import lombok.Data;

/**
 * @author dominate
 * @since 2023/1/5
 */
@Data
public class OssProperties {

    private String accessKeyId;
    private String accessKeySecret;
    private String endPoint;
    private String bucketName;
}
