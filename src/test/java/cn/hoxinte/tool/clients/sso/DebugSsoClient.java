package cn.hoxinte.tool.clients.sso;

import cn.hoxinte.tool.clients.helper.UserHelper;
import cn.hoxinte.tool.clients.sso.entity.UserInfoDTO;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
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
    public void testUserHelper() {
        Map<Integer, String> userNameMap = UserHelper.getUserNameMap(Arrays.asList(1, 2));
        for (Map.Entry<Integer, String> integerStringEntry : userNameMap.entrySet()) {
            System.out.println(integerStringEntry.getKey() + ":" + integerStringEntry.getValue());
        }
    }

    @Test
    public void testSearch() {
        List<UserInfoDTO> userInfoDTOList = UserHelper.searchUserList("1", "token eyJhbGciOiJIUzI1NiJ9.eyJzaWduVGltZSI6MTcwODY2MzM3MTQ2Niwic2V4IjoxLCJhdmF0YXIiOiIiLCJwbGF0Zm9ybUlkIjoxNiwidG9rZW4iOiIxNzA4NjYzMzcxNDY2MTQ0NjgxNjU3MjcwNzA5OTQzMiIsImFjY291bnRJZCI6MSwic2VyaWFsVmVyc2lvblVJRCI6MSwicGhvbmUiOiIxMzM4ODg4ODg4OCIsInBlcm1pc3Npb25zIjpbMjM3LDIzOCwyMzksMjQwLDI0MSwyNDIsMjQzLDI0NCwyNDUsMjQ2LDI0NywyNDhdLCJuYW1lIjoidGVjaC1hZG1pbiIsInRlbmFudElkIjowLCJhbGlhcyI6IiIsImVtYWlsIjoiMTMzODg4ODg4ODhAMTYzLmNvbSJ9.Y9t1g87NHmYKQyJM5L5AqtAzY6CGDrXK6kLjs-C3MV0");
        System.out.println("size " + userInfoDTOList.size());
        for (UserInfoDTO userInfoDTO : userInfoDTOList) {
            System.out.println(userInfoDTO);
        }
    }

}
