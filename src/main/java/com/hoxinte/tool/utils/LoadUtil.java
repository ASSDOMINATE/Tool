package com.hoxinte.tool.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * 文件读取工具
 *
 * @author dominate
 * @date 2021/11/05
 */
public final class LoadUtil {


    private static final String[] BASE_CONF_FILE_NAMES = {"application", "bootstrap"};

    private static final String SPRING = "spring.";
    private static final String PROFILES_ACTIVE = "profiles.active";
    private static final String SPRING_PROFILES_ACTIVE = SPRING + PROFILES_ACTIVE;
    private static final String CONFIG_LOCATION = "config.location";
    private static final String SPRING_CONFIG_LOCATION = SPRING + CONFIG_LOCATION;

    private static final String SUFFIX_YML = ".yml";
    private static final String SPLIT_STR = "-";
    private static final String PARAM_SPLIT_STR = ",";
    private static final String PROPERTY_SPLIT_STR = "\\.";
    private static final String PROD_SIGN = "prod";
    private static final int SET_BYTE_SIZE = 1024;

    private static final Properties SYSTEM_PROPERTIES = System.getProperties();
    private static final Map<String, Map<String, Object>> CONFIG_MAP = new HashMap<>();


    /**
     * 是否为Prod环境
     *
     * @param realTime 实时
     * @return true/false
     */
    public static boolean onProd(boolean realTime) {
        return PROD_SIGN.equals(getActive(realTime));
    }

    /**
     * 是否为Prod环境
     *
     * @return true/false
     */
    public static boolean onProd() {
        return onProd(false);
    }

    /**
     * 读取配置参数值
     * <p>
     * 读取顺序 启动参数 > 基础配置文件 > 环境配置文件
     *
     * @param property 配置参数
     * @return 参数值
     */
    public static String getProperty(String property) {
        return getProperty(property, false);
    }

    /**
     * 读取配置参数值
     * <p>
     * 读取顺序 启动参数 > 基础配置文件 > 环境配置文件
     *
     * @param realTime 实时
     * @param property 配置参数
     * @return 参数值
     */
    public static String getProperty(String property, boolean realTime) {
        // 从基础配置里读取
        String baseValue = getBaseProperty(property, realTime);
        if (StringUtil.isNotEmpty(baseValue)) {
            return baseValue;
        }
        // 根据环境变量选择配置文件读取
        String active = getActive(realTime);
        for (String confFile : BASE_CONF_FILE_NAMES) {
            String value = getSourceProperty(confFile + SPLIT_STR + active, property, realTime);
            if (StringUtil.isNotEmpty(value)) {
                return value;
            }
        }
        return StringUtil.EMPTY;
    }


    /**
     * 读取配置参数值
     * 数字类型
     *
     * @param property 配置参数
     * @return 参数值
     */
    public static Integer getIntegerProperty(String property) {
        return getIntegerProperty(property, false);
    }

    /**
     * 读取配置参数值
     * 数字类型
     *
     * @param realTime 实时
     * @param property 配置参数
     * @return 参数值
     */
    public static Integer getIntegerProperty(String property, boolean realTime) {
        String propertyValue = getProperty(property, realTime);
        if (!StringUtil.isNumeric(propertyValue)) {
            return 0;
        }
        return Integer.parseInt(propertyValue);
    }

    /**
     * 读取配置参数值
     * 字符串数组类型
     *
     * @param property 配置参数
     * @return 参数值
     */
    public static String[] getArrayProperty(String property) {
        return getArrayProperty(property, false);
    }

    /**
     * 读取配置参数值
     * 字符串数组类型
     *
     * @param realTime 实时
     * @param property 配置参数
     * @return 参数值
     */
    public static String[] getArrayProperty(String property, boolean realTime) {
        return getProperty(property, realTime).split(PARAM_SPLIT_STR);
    }

    /**
     * 读取配置参数值
     * 布尔类型
     *
     * @param property 配置参数
     * @return 参数值
     */
    public static Boolean getBooleanProperty(String property) {
        return getBooleanProperty(property, false);
    }

    /**
     * 读取配置参数值
     * 布尔类型
     *
     * @param realTime 实时
     * @param property 配置参数
     * @return 参数值
     */
    public static Boolean getBooleanProperty(String property, boolean realTime) {
        String propertyValue = getProperty(property, realTime);
        if (StringUtil.isEmpty(propertyValue)) {
            return false;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    /**
     * 从配置文件中获取配置参数值
     *
     * @param resource 配置文件
     * @param property 配置参数
     * @return 参数值
     */
    public static String getSourceProperty(String resource, String property) {
        return getSourceProperty(resource, property, false);
    }

    /**
     * 从配置文件中获取配置参数值
     *
     * @param realTime 实时
     * @param resource 配置文件
     * @param property 配置参数
     * @return 参数值
     */
    public static String getSourceProperty(String resource, String property, boolean realTime) {
        String[] keys = property.split(PROPERTY_SPLIT_STR);
        int steps = keys.length;
        if (steps == 0) {
            return StringUtil.EMPTY;
        }
        Map<String, Object> conf = loadYamlConfig(resource, realTime);
        int index = 1;
        for (String key : keys) {
            if (!conf.containsKey(key)) {
                return StringUtil.EMPTY;
            }
            if (index == steps) {
                return conf.get(key).toString();
            }
            conf = (Map<String, Object>) conf.get(key);
            index++;
        }
        return StringUtil.EMPTY;
    }

    /**
     * 按行读取文件
     *
     * @param fileName 文件名
     * @return 按行读取的字符串列表
     */
    public static List<String> loadLine(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        return loadLine(file);
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream 输入流
     * @return 读取字节
     */
    public static byte[] readInputStream(InputStream inputStream) {
        byte[] buffer = new byte[SET_BYTE_SIZE];
        int len;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }


    /**
     * 按行读取文件
     *
     * @param file 文件
     * @return 文件内容行 列表
     */
    private static List<String> loadLine(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 获取基础参数值，在系统变量及基础配置文件里
     *
     * @param property 配置参数
     * @return 参数值
     */
    private static String getBaseProperty(String property, boolean realTime) {
        // 先从系统参数里读取
        String systemValue = getSystemProperty(property, realTime);
        if (StringUtil.isNotEmpty(systemValue)) {
            return systemValue;
        }
        // 从基础配置文件里读取
        return getBaseSourceProperty(property, realTime);
    }

    /**
     * 从系统变量获取配置参数值
     *
     * @param property 配置参数
     * @return 参数值
     */
    private static String getSystemProperty(String property, boolean realTime) {
        return realTime ? System.getProperty(property) : SYSTEM_PROPERTIES.getProperty(property);
    }

    /**
     * 获取启用环境
     *
     * @return 启用环境
     */
    private static String getActive(boolean realTime) {
        // 先读取Spring的启用环境
        String active = getBaseProperty(SPRING_PROFILES_ACTIVE, realTime);
        // 没有再获取系统的
        if (StringUtil.isEmpty(active)) {
            return getBaseProperty(PROFILES_ACTIVE, realTime);
        }
        return active;
    }

    /**
     * 基础配置文件里获取配置产数值
     *
     * @param realTime 实时
     * @param property 配置参数
     * @return 参数值
     */
    private static String getBaseSourceProperty(String property, boolean realTime) {
        for (String confFile : BASE_CONF_FILE_NAMES) {
            String value = getSourceProperty(confFile, property, realTime);
            if (StringUtil.isNotEmpty(value)) {
                return value;
            }
        }
        return StringUtil.EMPTY;
    }

    /**
     * 读取配置文件
     * 优先从spring.config.location的路径里读取，没有再从config.location里读
     * 都没有则从项目内的配置文件读取
     *
     * @param resource 配置文件名称
     * @return 配置文件输入流
     */
    private static InputStream load(String resource) throws FileNotFoundException {
        String springConfigLocation = System.getProperty(SPRING_CONFIG_LOCATION);
        if (StringUtil.isNotEmpty(springConfigLocation)) {
            return new FileInputStream(springConfigLocation + resource + SUFFIX_YML);
        }
        String configLocation = System.getProperty(CONFIG_LOCATION);
        if (StringUtil.isNotEmpty(configLocation)) {
            return new FileInputStream(configLocation + resource + SUFFIX_YML);
        }
        return LoadUtil.class.getClassLoader().getResourceAsStream(resource + SUFFIX_YML);
    }

    /**
     * 读取配置文件
     *
     * @param resource 配置文件名称
     * @param realTime 实时
     * @return 配置文件对象
     */
    private static Map<String, Object> loadYamlConfig(String resource, boolean realTime) {
        if (!realTime && CONFIG_MAP.containsKey(resource)) {
            return CONFIG_MAP.get(resource);
        }
        try (InputStream is = load(resource)) {
            Map<String, Object> config = new Yaml().load(is);
            if (!realTime) {
                CONFIG_MAP.put(resource, config);
            }
            return config;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }


}
