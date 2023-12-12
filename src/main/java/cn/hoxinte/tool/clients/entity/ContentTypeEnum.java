package cn.hoxinte.tool.clients.entity;

/**
 * 文件类型对应的 ContentType
 * @author dominate
 * @since 2022/12/16
 */
public enum ContentTypeEnum {
    /**
     * 默认值
     */
    DEFAULT("application/octet-stream",""),
    BMP("image/bmp", ".bmp"),
    GIF("image/gif",".gif"),
    JPG("image/jpg",".jpeg", ".jpg",".png"),
    HTML("text/html",".html"),
    TXT("text/plain",".txt"),
    VSD("application/vnd.visio",".vsd"),
    PPT("application/vnd.ms-powerpoint",".pptx",".ppt"),
    DOCX("application/msword",".docx",".doc"),
    PDF("application/pdf",".pdf"),
    ZIP("application/zip",".zip"),
    XLS("application/vnd.ms-excel",".xls",".xlsx"),
    XML("text/xml",".xml"),
    ;

    /**
     * 对应的 ContentType
     */
    final String contentType;

    /**
     * 文件类型对应的 文件扩展名
     */
    final String[] extensions;

    ContentTypeEnum(String contentType, String... extensions) {
        this.extensions = extensions;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public String[] getExtensions() {
        return extensions;
    }
}
