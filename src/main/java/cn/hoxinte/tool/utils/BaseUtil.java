package cn.hoxinte.tool.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 基础工具
 *
 * @author dominate
 * @since 2022/02/25
 */
public final class BaseUtil {

    /**
     * Object 转 Map
     *
     * @param obj    原始对象
     * @param kClass key class
     * @param vClass value class
     * @param <K>    key class
     * @param <V>    key class
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> obj2Map(Object obj, Class<K> kClass, Class<V> vClass) {
        Map<K, V> result = new HashMap<>();
        if (obj instanceof HashMap<?, ?>) {
            Map<?, ?> map = (HashMap<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(kClass.cast(entry.getKey()), vClass.cast(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Object 转 List
     *
     * @param obj    原始对象
     * @param tClass 目标 Class
     * @param <T>    目标 Class
     * @return List<T>
     */
    public static <T> List<T> obj2List(Object obj, Class<T> tClass) {
        List<T> result = new ArrayList<>();
        if (obj instanceof ArrayList<?>) {
            for (Object o : (List<?>) obj) {
                result.add(tClass.cast(o));
            }
        }
        return result;
    }

    /**
     * 判断对象所有属性是否有值
     *
     * @param obj 待检查对象
     * @return 是否有值
     */
    public static boolean hasValue(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(obj);
                if (null == o) {
                    continue;
                }
                if (StringUtil.isNotEmpty(o.toString())) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            } finally {
                field.setAccessible(false);
            }
        }
        return false;
    }

    /**
     * 从目标对象设置原对象相同属性的值
     *
     * @param base   原对象
     * @param target 目标对象
     */
    public static void set(Object base, Object target) {
        for (Field field : base.getClass().getDeclaredFields()) {
            try {
                Object targetValue = findValue(target, field);
                if (null == targetValue) {
                    continue;
                }
                setValue(base, targetValue, field);
            } catch (Exception e) {
                // set one field failed , just skip it
            }
        }
    }

    /**
     * 对比设置更新
     *
     * @param base   对比源
     * @param target 对比目标
     * @param source 对比结果
     * @return 是否有数据更新
     */
    public static boolean compare(Object base, Object target, Object source) {
        boolean setSource = false;
        for (Field field : base.getClass().getDeclaredFields()) {
            try {
                Object targetValue = findValue(target, field);
                if (null == targetValue) {
                    continue;
                }
                // compare base / target value
                field.setAccessible(true);
                Object baseValue = field.get(base);
                if (null != baseValue && baseValue.equals(targetValue)) {
                    continue;
                }
                // find source field
                Field sourceField = ReflectionUtils.findField(source.getClass(), field.getName());
                if (null == sourceField) {
                    continue;
                }
                // set source value
                setValue(source, targetValue, field);
                setSource = true;
            } catch (Exception e) {
                // set one field failed , just skip it
            }
        }
        return setSource;
    }

    /**
     * 对象转 Map<String,Object>
     *
     * @param object 需要转换为Map的对象
     * @return 转换后的 Map<String,Object>
     */
    public static Map<String, Object> beanToMap(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            Map<String, Object> map = new HashMap<>(fields.length);
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(object));
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * Map<String,Object> 转换为指定对象
     *
     * @param map       需要转换的Map
     * @param beanClass 目标对象Class
     * @param <T>       目标对象类型
     * @return 转换结果
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) {
        try {
            T object = beanClass.getDeclaredConstructor().newInstance();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                field.setAccessible(true);
                if (map.containsKey(field.getName())) {
                    field.set(object, map.get(field.getName()));
                }
            }
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Map<String,String> 转换为指定对象
     *
     * @param map       需要转换的Map
     * @param beanClass 目标对象Class
     * @param <T>       目标对象类型
     * @return 转换结果
     */
    public static <T> T stringMapToBean(Map<String, String> map, Class<T> beanClass) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            T object = beanClass.getDeclaredConstructor().newInstance();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                field.setAccessible(true);
                if (map.containsKey(field.getName())) {
                    String v = map.get(field.getName());
                    if (null == v) {
                        continue;
                    }
                    field.set(object, objectMapper.convertValue(v, field.getType()));
                }
            }
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对象转换为 Map<String,String>
     *
     * @param object 待转换对象
     * @return Map<String, String>
     */
    public static Map<String, String> beanToStringMap(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            Map<String, String> map = new HashMap<>(fields.length);
            for (Field field : fields) {
                field.setAccessible(true);
                if (null != field.get(object)) {
                    map.put(field.getName(), field.get(object).toString());
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * 给对象属性设置默认值
     * String 设置 ""
     * Integer 设置 0
     *
     * @param beanClass 对象Class
     * @param bean      进行设置的对象
     */
    public static void setDefault(Class<?> beanClass, Object bean) {
        for (Field field : beanClass.getDeclaredFields()) {

            if (field.getType() == String.class) {
                setField(field, bean, StringUtil.EMPTY);
                continue;
            }
            if (field.getType() == Integer.class) {
                setField(field, bean, 0);
            }
        }
    }


    public static Integer getStringInt(String string) {
        return StringUtil.isNumeric(string) ? Integer.parseInt(string) : 0;
    }

    public static Integer parseUpdateValue(Integer old, Integer update) {
        if (null == update) {
            return null;
        }
        if (null == old) {
            return update;
        }
        if (old.equals(update)) {
            return null;
        }
        return update;
    }

    public static Integer parseUpdateValue(Integer old, String update) {
        if (null == old) {
            if (!StringUtil.isNumeric(update)) {
                return null;
            }
            return Integer.parseInt(update);
        }
        if (!StringUtil.isNumeric(update)) {
            return null;
        }
        if (old.equals(Integer.parseInt(update))) {
            return null;
        }
        return Integer.parseInt(update);
    }

    public static Boolean parseUpdateValue(boolean old, Boolean update) {
        if (null == update || old == update) {
            return null;
        }
        return update;
    }

    public static String parseUpdateValue(String old, String update) {
        if (StringUtil.isEmpty(old)) {
            if (StringUtil.isEmpty(update)) {
                return null;
            }
            return update;
        }
        if (StringUtil.isEmpty(update)) {
            return null;
        }
        if (old.equals(update)) {
            return null;
        }
        return update;
    }

    private static Object findValue(Object object, Field field) throws IllegalAccessException {
        Field targetField = ReflectionUtils.findField(object.getClass(), field.getName());
        if (null == targetField) {
            return null;
        }
        targetField.setAccessible(true);
        return targetField.get(object);
    }

    private static void setValue(Object object, Object targetValue, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(object, targetValue);
    }

    private static void setField(Field field, Object bean, Object value) {
        field.setAccessible(true);
        try {
            if (null == field.get(bean)) {
                field.set(bean, value);
            }
        } catch (Exception e) {
            // just skip set value
        }
    }
}
