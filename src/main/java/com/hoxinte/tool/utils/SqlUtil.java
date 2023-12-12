package com.hoxinte.tool.utils;


/**
 * Mysql 工具
 *
 * @author dominate
 */
public class SqlUtil {

    private static final String SQL_LIMIT = "limit ";

    private static final String SPLIT_STR = ",";

    private static final String SQL_LIMIT_ONE = SQL_LIMIT + "1";

    private static final int MAX_SIZE = 1000;

    public static String limitOne() {
        return SQL_LIMIT_ONE;
    }




    public static String indexLimit(int size, int index) {
        if (MAX_SIZE < size) {
            size = MAX_SIZE;
        }
        if (index < 0) {
            index = 0;
        }
        if (size < 1) {
            size = 1;
        }
        return SQL_LIMIT + index + SPLIT_STR + size;
    }

    public static String indexLimit(long size, long index) {
        if (MAX_SIZE < size) {
            size = MAX_SIZE;
        }
        if (index < 0L) {
            index = 0L;
        }
        if (size < 1L) {
            size = 1L;
        }
        return SQL_LIMIT + index + SPLIT_STR + size;
    }


    public static String pageLimit(int size, int page) {
        if (MAX_SIZE < size) {
            size = MAX_SIZE;
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 1;
        }
        return SQL_LIMIT + (page - 1) * size + SPLIT_STR + size;
    }

    public static String pageLimit(long size, long page) {
        if (MAX_SIZE < size) {
            size = MAX_SIZE;
        }
        if (page < 1L) {
            page = 1L;
        }
        if (size < 1L) {
            size = 1L;
        }
        return SQL_LIMIT + (page - 1) * size + SPLIT_STR + size;
    }
}
