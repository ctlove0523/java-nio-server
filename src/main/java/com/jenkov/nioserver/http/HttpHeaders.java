package com.jenkov.nioserver.http;

/**
 * Created by jjenkov on 19-10-2015.
 */
public class HttpHeaders {

    static final int HTTP_METHOD_GET = 1;
    static final int HTTP_METHOD_POST = 2;
    static final int HTTP_METHOD_PUT = 3;
    static final int HTTP_METHOD_HEAD = 4;
    static final int HTTP_METHOD_DELETE = 5;

    int httpMethod = 0;

    public int hostStartIndex = 0;
    public int hostEndIndex = 0;

    int contentLength = 0;

    int bodyStartIndex = 0;
    int bodyEndIndex = 0;


}
