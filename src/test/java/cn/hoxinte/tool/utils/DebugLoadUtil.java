package cn.hoxinte.tool.utils;

import org.junit.Test;

/**
 * @author dominate
 * @since 2023/2/24
 */
public class DebugLoadUtil {

    @Test
    public void testLoad() {
        String obj = LoadUtil.getSourceProperty("application", "spring.profiles.active");
        System.out.println(obj);
        obj = LoadUtil.getProperty("redis.database");
        System.out.println(obj);
    }
}
