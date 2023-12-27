package cn.hoxinte.tool.clients.sso;


import cn.hoxinte.tool.clients.redis.RedisClient;
import cn.hoxinte.tool.clients.sso.entity.DeptCache;
import cn.hoxinte.tool.clients.sso.entity.ManagerDTO;
import cn.hoxinte.tool.clients.sso.entity.UserCache;
import cn.hoxinte.tool.clients.sso.entity.UserInfoDTO;
import cn.hoxinte.tool.clients.sso.enums.ManagerEnum;

import java.util.*;

/**
 * SSO缓存工具
 *
 * @author dominate
 * @since 2022/9/9
 */
public class CacheUtil {

    /**
     * 请求时间
     */
    private static final String REQUEST_TIME_KEY = "sso:cache:request:time";

    /**
     * 用户信息缓存 User.id - UserInfo
     */
    private static final String USER_MAP_CACHE_KEY = "sso:cache:user:map";
    /**
     * User.id - UserInfo
     */
    private static final Map<Integer, UserInfoDTO> CACHE_USER_MAP = new HashMap<>();
    /**
     * User.identity - User.id
     */
    private static final Map<String, Integer> CACHE_USER_IDENTITY_MAP = new HashMap<>();
    /**
     * User.uniqueCode - User.id
     */
    private static final Map<String, Integer> CACHE_USER_UNIQUE_CODE_MAP = new HashMap<>();


    /**
     * 用户数据缓存 User.id - UserDataCache
     */
    private static final String USER_DATA_CACHE_KEY = "sso:cache:user:data:map";
    /**
     * User.id - UserDataCache
     */
    private static final Map<Integer, UserCache> CACHE_USER_DATA_MAP = new HashMap<>();


    /**
     * 部门缓存 Dept.id - DeptCache
     */
    private static final String DEPT_CACHE_KEY = "sso:cache:dept:map";
    /**
     * Dept.id - DeptCache
     */
    private static final Map<Integer, DeptCache> CACHE_DEPT_MAP = new HashMap<>();
    /**
     * Dept.id - Manager.Code - ManagerDTO
     */
    private static final Map<Integer, Map<Integer, ManagerDTO>> CACHE_MANAGER_MAP = new HashMap<>();

    private static final String EMPTY_STRING = "";
    private static final String DEPT_LEADER_SPLIT = "/";
    private static final String SPLIT = ":";
    private static final int DEFAULT_MAP_SIZE = 8;

    static {
        loadCache();
    }


    public static long getLatestRequestTime() {
        if (!RedisClient.hasKey(REQUEST_TIME_KEY)) {
            return 0;
        }
        return RedisClient.get(REQUEST_TIME_KEY, Long.class);
    }

    public static void saveRequestTime() {
        RedisClient.setPersist(REQUEST_TIME_KEY, System.currentTimeMillis());
    }

    public static void printCacheState() {
        System.out.println("Cache User " + CACHE_USER_MAP.size());
        System.out.println("Cache User Data " + CACHE_USER_DATA_MAP.size());
        System.out.println("Cache User Identity " + CACHE_USER_IDENTITY_MAP.size());
        System.out.println("Cache User UniqueCode " + CACHE_USER_UNIQUE_CODE_MAP.size());
        System.out.println("Cache Manager " + CACHE_MANAGER_MAP.size());
        System.out.println("Cache Dept " + CACHE_DEPT_MAP.size());
    }


    /**
     * 获取用户部门ID
     *
     * @param userId 用户ID
     * @return 部门ID
     */
    public static int getUserDeptId(int userId) {
        if (!CACHE_USER_DATA_MAP.containsKey(userId)) {
            return 0;
        }
        return CACHE_USER_DATA_MAP.get(userId).getDeptId();
    }

    /**
     * 获取用户领导 Map
     *
     * @param userId 用户ID
     * @return 用户领导 Map
     */
    public static Map<Integer, ManagerDTO> getUserLeaderMap(int userId) {
        if (!CACHE_USER_DATA_MAP.containsKey(userId)) {
            return Collections.emptyMap();
        }
        UserCache userData = CACHE_USER_DATA_MAP.get(userId);
        if (!CACHE_DEPT_MAP.containsKey(userData.getDeptId())) {
            return Collections.emptyMap();
        }
        return getDeptLeaderMap(userData.getDeptId());
    }

    /**
     * 获取部门领导 Map
     *
     * @param deptId 部门ID
     * @return 部门领导 Map
     */
    public static Map<Integer, ManagerDTO> getDeptLeaderMap(int deptId) {
        if (!CACHE_MANAGER_MAP.containsKey(deptId)) {
            return Collections.emptyMap();
        }
        return CACHE_MANAGER_MAP.get(deptId);
    }

    /**
     * 从缓存获取用户领导描述
     *
     * @param userId 用户ID
     * @return 领导描述
     */
    public static String getUserLeaderDesr(int userId) {
        if (!CACHE_USER_DATA_MAP.containsKey(userId)) {
            return EMPTY_STRING;
        }
        UserCache userData = CACHE_USER_DATA_MAP.get(userId);
        if (!CACHE_DEPT_MAP.containsKey(userData.getDeptId())) {
            return EMPTY_STRING;
        }
        return CACHE_DEPT_MAP.get(userData.getDeptId()).getLeaderDesr();
    }

    /**
     * 从缓存获取用户数据
     *
     * @param userId 用户ID
     * @return 用户数据
     */
    public static UserCache getUserData(int userId) {
        return CACHE_USER_DATA_MAP.getOrDefault(userId, UserCache.defaultData());
    }

    /**
     * 从缓存获取部门数据
     *
     * @param deptId 部门ID
     * @return 部门数据
     */
    public static DeptCache getDept(int deptId) {
        DeptCache deptCache = CACHE_DEPT_MAP.get(deptId);
        if (Objects.nonNull(deptCache)) {
            return deptCache;
        }
        Object object = RedisClient.hGet(DEPT_CACHE_KEY, String.valueOf(deptId));
        if (Objects.nonNull(object)) {
            return (DeptCache) object;
        }
        return null;
    }

    public static UserInfoDTO getUser(int accountId) {
        return CACHE_USER_MAP.getOrDefault(accountId, null);
    }

    /**
     * 从缓存获取用户部门描述
     *
     * @param accountId 用户ID
     * @return 部门描述
     */
    public static String getUserDeptDesr(int accountId) {
        UserCache userCache = CACHE_USER_DATA_MAP.get(accountId);
        if (Objects.isNull(userCache)) {
            return EMPTY_STRING;
        }
        return userCache.getDeptDesr();
    }

    /**
     * 获取用户ID
     *
     * @param identity 身份证号
     * @return 用户ID
     */
    public static int getUserIdByIdentity(String identity) {
        return CACHE_USER_IDENTITY_MAP.getOrDefault(identity, 0);
    }

    /**
     * 获取用户ID
     *
     * @param uniqueCode 唯一标识
     * @return 用户ID
     */
    public static int getUserIdByUniqueCode(String uniqueCode) {
        return CACHE_USER_UNIQUE_CODE_MAP.getOrDefault(uniqueCode, 0);
    }

    /**
     * 设置用户缓存
     *
     * @param user 用户数据
     */
    public static void setUserCache(UserInfoDTO user) {
        RedisClient.hSetPersist(USER_MAP_CACHE_KEY, user.getAccountId().toString(), user);
        CACHE_USER_MAP.put(user.getAccountId(), user);
        CACHE_USER_IDENTITY_MAP.put(user.getIdentity(), user.getAccountId());
        CACHE_USER_UNIQUE_CODE_MAP.put(user.getUniqueCode(), user.getAccountId());
    }

    /**
     * 设置用户数据缓存
     *
     * @param userCache 缓存
     */
    public static void setUserDataCache(UserCache userCache) {
        RedisClient.hSetPersist(USER_DATA_CACHE_KEY, userCache.getAccountId().toString(), userCache);
        CACHE_USER_DATA_MAP.put(userCache.getAccountId(), userCache);
    }

    /**
     * 设置部门数据缓存
     *
     * @param deptCache 部门数据
     */
    public static void setDeptCache(DeptCache deptCache) {
        RedisClient.hSetPersist(DEPT_CACHE_KEY, deptCache.getDeptId().toString(), deptCache);
        CACHE_DEPT_MAP.put(deptCache.getDeptId(), deptCache);
    }

    /**
     * 分析部门领导
     */
    public static void parseDeptLeader() {
        for (DeptCache dept : CACHE_DEPT_MAP.values()) {
            parseThisDeptLeader(dept);
        }
    }

    /**
     * 读取Redis缓存到内存中使用
     */
    public static void loadCache() {
        loadUserCache();
        loadUserData();
        loadDeptCache();
    }

    private static void loadUserCache() {
        Map<String, UserInfoDTO> userCacheMap = RedisClient.hGetAll(USER_MAP_CACHE_KEY, UserInfoDTO.class);
        CACHE_USER_MAP.clear();
        CACHE_USER_IDENTITY_MAP.clear();
        CACHE_USER_UNIQUE_CODE_MAP.clear();
        for (UserInfoDTO userInfo : userCacheMap.values()) {
            CACHE_USER_MAP.put(userInfo.getAccountId(), userInfo);
            CACHE_USER_IDENTITY_MAP.put(userInfo.getIdentity(), userInfo.getAccountId());
            CACHE_USER_UNIQUE_CODE_MAP.put(userInfo.getUniqueCode(), userInfo.getAccountId());
        }
    }

    private static void loadUserData() {
        Map<String, UserCache> userCacheMap = RedisClient.hGetAll(USER_DATA_CACHE_KEY, UserCache.class);
        CACHE_USER_DATA_MAP.clear();
        for (Map.Entry<String, UserCache> entry : userCacheMap.entrySet()) {
            CACHE_USER_DATA_MAP.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }

    }

    private static void loadDeptCache() {
        Map<String, DeptCache> deptCacheMap = RedisClient.hGetAll(DEPT_CACHE_KEY, DeptCache.class);
        CACHE_DEPT_MAP.clear();
        for (DeptCache deptCache : deptCacheMap.values()) {
            CACHE_DEPT_MAP.put(deptCache.getDeptId(), deptCache);
        }
        parseDeptLeader();
    }


    private static void loadParentDept(int deptId, List<DeptCache> deptList) {
        if (!CACHE_DEPT_MAP.containsKey(deptId)) {
            return;
        }
        // 避免出现死循环
        for (DeptCache deptCache : deptList) {
            if (deptId == deptCache.getDeptId()) {
                return;
            }
        }
        DeptCache dept = CACHE_DEPT_MAP.get(deptId);
        deptList.add(dept);
        loadParentDept(dept.getParentId(), deptList);
    }

    private static void parseThisDeptLeader(DeptCache thisDept) {
        List<DeptCache> deptList = new ArrayList<>();
        loadParentDept(thisDept.getDeptId(), deptList);
        // managerCode+，领导人
        Map<Integer, DeptCache.Leader> leaderMap = new HashMap<>(ManagerEnum.values().length);
        //领导人ehrId，部门名称
        Map<String, String> leaderDeptMap = new HashMap<>(ManagerEnum.values().length);
        for (DeptCache dept : deptList) {
            for (Map.Entry<Integer, DeptCache.Leader> leaderEntry : dept.getLeaderMap().entrySet()) {
                if (leaderMap.containsKey(leaderEntry.getKey())) {
                    continue;
                }
                leaderMap.put(leaderEntry.getKey(), leaderEntry.getValue());
                leaderDeptMap.put(leaderEntry.getValue().getUniqueCode(), dept.getName());
            }
        }
        if (!CACHE_MANAGER_MAP.containsKey(thisDept.getDeptId())) {
            CACHE_MANAGER_MAP.put(thisDept.getDeptId(), new HashMap<>(DEFAULT_MAP_SIZE));
        }
        StringBuilder builder = new StringBuilder();
        for (ManagerEnum managerEnum : ManagerEnum.values()) {
            if (!leaderMap.containsKey(managerEnum.getCode())) {
                continue;
            }
            DeptCache.Leader thisLeader = leaderMap.get(managerEnum.getCode());
            if (null == thisLeader) {
                continue;
            }
            ManagerDTO manager = new ManagerDTO()
                    .setManagerCode(managerEnum.getCode())
                    .setName(leaderDeptMap.get(thisLeader.getUniqueCode()))
                    .setUniqueCode(thisLeader.getUniqueCode())
                    .setUserId(thisLeader.getUserId())
                    .setUserName(thisLeader.getUserName());
            CACHE_MANAGER_MAP.get(thisDept.getDeptId()).put(managerEnum.getCode(), manager);
            if (!managerEnum.isShowManager()) {
                continue;
            }
            if (builder.length() != 0) {
                builder.append(DEPT_LEADER_SPLIT);
            }
            builder.append(managerEnum.getName());
            builder.append(SPLIT);
            builder.append(thisLeader.getUserName());
        }
        thisDept.setLeaderDesr(builder.toString());
    }

    private static boolean isEmpty(Object str) {
        return str == null || EMPTY_STRING.equals(str);
    }

}
