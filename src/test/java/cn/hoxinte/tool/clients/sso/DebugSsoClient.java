package cn.hoxinte.tool.clients.sso;

import cn.hoxinte.tool.clients.helper.UserHelper;
import org.junit.Test;

import java.util.Arrays;
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

    @Test
    public void testUserHelper(){
        Map<Integer, String> userNameMap = UserHelper.getUserNameMap(Arrays.asList(1,2));
        for (Map.Entry<Integer, String> integerStringEntry : userNameMap.entrySet()) {
            System.out.println(integerStringEntry.getKey() + ":" + integerStringEntry.getValue());
        }
    }

}
