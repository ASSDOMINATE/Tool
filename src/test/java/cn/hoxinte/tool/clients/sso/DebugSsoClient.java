package cn.hoxinte.tool.clients.sso;

import org.junit.Test;

import java.util.Map;

/**
 * @author dominate
 * @since 2023/2/24
 */
public class DebugSsoClient {

    @Test
    public void testLoadDesr() {
        String result = SsoClient.requestUserDeptDesrMap(33640, 34280);
        System.out.println("result" + result);
        Map<Integer, String> desrMap = ParseUtil.parseIntegerMapResponse(result);
        for (Map.Entry<Integer, String> integerStringEntry : desrMap.entrySet()) {
            System.out.println(integerStringEntry.getKey() + " " + integerStringEntry.getValue());
        }
    }

}
