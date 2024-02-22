package cn.hoxinte.tool.clients.helper;

import cn.hoxinte.tool.clients.sso.entity.UserAuthDTO;
import cn.hoxinte.tool.utils.BaseUtil;
import cn.hoxinte.tool.utils.LoadUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * 授权工具
 *
 * @author dominate
 * @since 2022/02/22
 */
public class AuthHelper {

    private static final String JWT_SECRET = LoadUtil.getProperty("jwt.secret");

    private static final String VALID_TIME = "time";

    private static final Key JWT_SECRET_KEY;

    static {
        JWT_SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    /**
     * 解析 JWT-Token 获得用户信息
     *
     * @param token JWT-Token
     * @return 用户信息
     */
    public static UserAuthDTO parse(String token) {
        Map<String, Object> claims = parseJwtToken(token);
        return BaseUtil.mapToBean(claims, UserAuthDTO.class);
    }

    /**
     * 创建API Token
     *
     * @return String
     */
    public static String createApiToken() {
        Map<String, Object> claims = new HashMap<>();
        // TODO 和SSO的AuthHelper一致即可，看情况再加点参数吧
        claims.put(VALID_TIME, System.currentTimeMillis());
        return createJwtToken(claims);
    }

    /**
     * 解析 JWT-Token
     *
     * @param token JWT-Token
     * @return 解析结果
     */
    private static Map<String, Object> parseJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY).build().parseClaimsJws(token).getBody();
    }

    /**
     * 创建 JWT-Token
     *
     * @param claims 加密数据
     * @return JWT-Token
     */
    private static String createJwtToken(Map<String, Object> claims) {
        return Jwts.builder().setClaims(claims).signWith(JWT_SECRET_KEY).compact();
    }


}
