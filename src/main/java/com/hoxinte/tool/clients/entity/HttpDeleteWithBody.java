package com.hoxinte.tool.clients.entity;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * @author dominate
 * @since 2023/1/17
 */
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteWithBody() {
    }

    public HttpDeleteWithBody(URI uri) {
        this.setURI(uri);
    }

    public HttpDeleteWithBody(String uri) {
        this.setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return "DELETE";
    }
}
