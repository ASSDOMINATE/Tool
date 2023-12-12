package com.hoxinte.tool.utils;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库工具
 *
 * @author dominate
 * @since 2022/07/26
 */
@Slf4j
public class DataSourceUtil {

    private static final String USER = "user";
    private static final String PASSWORD = "password";

    public static <T> List<T> selectList(String dbUrl, String user, String password, String sql, Class<T> resultClass) {
        try (Connection connection = DataSourceUtil.connect(dbUrl, user, password)) {
            log.info("run sql:{}", sql);
            List<T> ts = DataSourceUtil.executeQuery(connection, sql, resultClass);
            log.info("select result size:{}", ts.size());
            return ts;
        } catch (SQLException e) {
            log.error("execute sql error ", e);
            return Collections.emptyList();
        }
    }

    @SneakyThrows
    public static <T> List<T> selectList(Connection connect, String sql, Class<T> resultClass) {
        List<T> ts = DataSourceUtil.executeQuery(connect, sql, resultClass);
        log.info("select result size:{}", ts.size());
        return ts;
    }

    public static <T> T selectOne(String dbUrl, String user, String password, String sql, Class<T> resultClass) {
        try (Connection connect = DataSourceUtil.connect(dbUrl, user, password)) {
            log.info("select type:{}", resultClass);
            return DataSourceUtil.executeSingleQuery(connect, sql, resultClass);
        } catch (SQLException e) {
            log.error("execute sql error ", e);
            return null;
        }
    }

    @SneakyThrows
    public static <T> T selectOne(Connection connect, String sql, Class<T> resultClass) {
        return DataSourceUtil.executeSingleQuery(connect, sql, resultClass);
    }

    /**
     * 创建连接
     * 需要关闭
     *
     * @param dbUrl    数据库 URL
     * @param user     账户
     * @param password 密码
     * @return 数据库连接
     */
    public static Connection connect(String dbUrl, String user, String password) throws SQLException {
        Properties perm = new Properties();
        perm.setProperty(USER, user);
        perm.setProperty(PASSWORD, password);
        Driver driver = new com.mysql.cj.jdbc.Driver();
        return driver.connect(dbUrl, perm);
    }

    /**
     * 创建连接
     * 需要关闭
     *
     * @param dbUrl    数据库 URL
     * @param user     账户
     * @param password 密码
     * @return 数据库连接
     */
    public static Connection connect(String dbUrl, String driverPath, String user, String password) throws SQLException {
        try {
            Class.forName(driverPath);
            return DriverManager.getConnection(dbUrl, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行查询SQL
     *
     * @param connection 数据库连接
     * @param sql        待执行的SQL
     * @return 查询结果
     */
    public static List<Map<String, Object>> executeQuery(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                return parseResult(resultSet);
            }
        }
    }

    /**
     * 执行查询Sql
     *
     * @param connection 数据库连接
     * @param sql        待执行的SQL
     * @param clazz      返回 class
     * @param <T>        返回的对象
     * @return 查询结果
     */
    public static <T> List<T> executeQuery(Connection connection, String sql, Class<T> clazz, String... params) throws SQLException {
        String parsedSql = parseSql(sql, params);
        log.info("[SQL] {}", parsedSql);
        try (PreparedStatement statement = connection.prepareStatement(parsedSql)) {
            ResultSet resultSet = statement.executeQuery();
            return parseResult(resultSet, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T> T executeSingleQuery(Connection connection, String sql, Class<T> clazz) throws SQLException {
        log.info("[SQL] {}", sql);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                return parseSingleResult(resultSet, clazz);
            }
        }
    }

    private static <T> T parseSingleResult(ResultSet rs, Class<T> clazz) throws SQLException {
        T obj = null;
        while (rs.next()) {
            obj = rs.getObject(1, clazz);
        }
        return obj;
    }



    private static List<Map<String, Object>> parseResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        List<Map<String, Object>> result = new ArrayList<>();
        int colCount = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> rowMap = new HashMap<>(colCount);
            for (int i = 1; i <= colCount; i++) {
                rowMap.put(metaData.getColumnName(i), rs.getObject(i));
            }
            result.add(rowMap);
        }
        return result;
    }


    public static <T> List<T> parseResult(ResultSet rs, Class<T> clazz) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount();
        Field[] fields = clazz.getDeclaredFields();
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            try {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= colCount; i++) {
                    for (Field f : fields) {
                        if (!f.getName().equalsIgnoreCase(metaData.getColumnName(i))) {
                            continue;
                        }
                        f.setAccessible(true);
                        f.set(obj, rs.getObject(i));
                    }
                }
                result.add(obj);
            } catch (Exception e) {
                // set field error , just skip
                log.error(e.getMessage());
            }
        }
        return result;
    }

    private static final String SQL_PARAM_SIGN = "&";

    public static String parseSql(String sql, String... params) {
        String result = sql;
        for (int i = 0; i < params.length; i++) {
            result = result.replace(SQL_PARAM_SIGN + (i + 1), params[i]);
        }
        return result;
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("_(\\w)");

    private static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = LINE_PATTERN.matcher(str);
        StringBuilder s = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(s, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(s);
        return s.toString();

    }

}
