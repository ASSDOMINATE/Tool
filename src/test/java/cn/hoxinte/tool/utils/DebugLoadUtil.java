package cn.hoxinte.tool.utils;

import org.junit.Test;

/**
 * @author dominate
 * @since 2023/2/24
 */
public class DebugLoadUtil {


    @Test
    public void forWords(){
        String s = "15110005421\n" +
                "15826070377\n" +
                "18118190001\n" +
                "17624296035\n" +
                "17712499733\n" +
                "18709719956\n" +
                "15685316030";
        int i = 0;
        for (String s1 : s.split("\n")) {
            System.out.print(s1+" ");
            i++;
        }
        System.out.println(i);
    }

    @Test
    public void testLoad() {
        String obj = LoadUtil.getSourceProperty("application", "spring.profiles.active");
        System.out.println(obj);
        obj = LoadUtil.getProperty("redis.database");
        System.out.println(obj);
    }
}
