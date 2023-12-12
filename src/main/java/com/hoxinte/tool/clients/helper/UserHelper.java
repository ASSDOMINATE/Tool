package com.hoxinte.tool.clients.helper;

import com.hoxinte.tool.clients.redis.RedisClient;
import com.hoxinte.tool.clients.sso.CacheUtil;
import com.hoxinte.tool.clients.sso.ParseUtil;
import com.hoxinte.tool.clients.sso.SsoClient;
import com.hoxinte.tool.clients.sso.entity.*;
import com.hoxinte.tool.clients.sso.enums.CtiEnum;
import com.hoxinte.tool.clients.sso.enums.ManagerEnum;
import com.hoxinte.tool.utils.RandomUtil;
import com.hoxinte.tool.utils.StringUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SSO 用户相关数据工具
 * <p>
 * 除搜索相关，其余均有缓存
 * 部门数据信息会有更新延迟
 *
 * @author dominate
 * @since 2022/07/08
 */
public class UserHelper {

    // 临时缓存Key

    /**
     * 部门下用户ID
     */
    private static final String TEMP_DEPT_USER_ID_LIST_CACHE = "sso:temp:user:id:list:dept:id:";
    /**
     * 部门下用户列表
     */
    private static final String TEMP_DEPT_USER_LIST_CACHE = "sso:temp:user:list:dept:id:";
    /**
     * 部门下全部用户列表
     */
    private static final String TEMP_DEPT_ALL_USER_ID_CACHE = "sso:temp:user:all:id:list:dept:id:";
    /**
     * 用户下级用户ID列表
     */
    private static final String TEMP_LOWER_ID_LIST_CACHE = "sso:temp:lower:id:list:user:id:";


    // Hash 缓存

    /**
     * CTI用户绑定关系
     */
    private static final String HASH_CIT_RELATE_USER_CACHE_HEAD = "sso:hash:cti:relate:";

    private static final int DEFAULT_MAP_SIZE = 8;

    // Redis 临时缓存随机过期时间范围

    private static final int MIN_OUT_TIME = 60;
    private static final int MAX_OUT_TIME = 60 * 5;

    private static boolean START_SYNC = false;

    private static final long RUN_SYNC_MINUTE = 60;

    private static final long REQUEST_TIME = 60 * 60 * 1000;

    /**
     * 需要初始化调用该方法才能使用缓存
     *
     * 或者自行控制 syncDeptCache syncUserCache syncCtiRelateCache
     */
    public static void sync() {
        if (START_SYNC) {
            return;
        }
        START_SYNC = true;
        ScheduledThreadPoolExecutor poolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        poolExecutor.scheduleAtFixedRate(() -> {
            long latestTime = CacheUtil.getLatestRequestTime();
            if (System.currentTimeMillis() - latestTime <= REQUEST_TIME) {
                return;
            }
            syncDeptCache();
            syncUserCache();
            syncCtiRelateCache();
            CacheUtil.printCacheState();
            CacheUtil.saveRequestTime();
        }, 0, RUN_SYNC_MINUTE, TimeUnit.MINUTES);
    }


    /**
     * 判断用户是否在部门下
     *
     * @param userId  用户ID
     * @param deptIds 部门ID列表
     * @return 是否在部门下
     */
    public static boolean checkUserOnDept(int userId, Integer... deptIds) {
        return requestUserCheckDept(userId, deptIds);
    }

    /**
     * 判断权限下是否有该用户
     *
     * @param permId 权限ID
     * @param userId 用户ID
     * @return 是否有该用户
     */
    public static boolean checkPermHasUser(int permId, int userId) {
        return requestPermHasUser(permId, userId);
    }

    /**
     * 角色权限检查列表
     *
     * @param path 权限路径
     * @return 角色检查列表
     */
    public static List<RolePermCheckDTO> getRolePermCheckList(String path) {
        return requestRolesCheck(path);
    }

    /**
     * 读取子部门
     *
     * @param deptId 部门ID
     * @param getAll 是否读取所有
     * @return 子部门列表
     */
    public static List<DeptDTO> loadChildDept(int deptId, boolean getAll) {
        return ParseUtil.parseDeptListResponse(SsoClient.loadChildDept(deptId, getAll));
    }

    /**
     * 获取用户领导描述
     *
     * @param userId 用户ID
     * @return 领导描述
     */
    public static String getUserLeaderDesr(int userId) {
        return CacheUtil.getUserLeaderDesr(userId);
    }

    /**
     * 获取用户缓存数据
     *
     * @param userId 用户ID
     * @return 用户缓存数据
     */
    public static UserCache getUserData(int userId) {
        return CacheUtil.getUserData(userId);
    }

    /**
     * 获取CTI下全部用户绑定关系
     *
     * @param ctiCode cti编码
     * @return 绑定关系
     */
    public static List<CtiRelateDTO> getCtiRelate(int ctiCode) {
        return getCacheCtiRelateList(ctiCode);
    }


    /**
     * 检验用户权限
     *
     * @param token 用户token
     * @param path  权限
     * @return 用户ID List
     */
    public static boolean verify(String token, String path) {
        return requestVerify(token, path);
    }

    /**
     * 获取用户数据
     * 静态缓存
     *
     * @param userId 账号ID
     * @return 用户数据
     */
    public static UserInfoDTO getUser(Integer userId) {
        if (null == userId) {
            return null;
        }
        UserInfoDTO user = CacheUtil.getUser(userId);
        if (null != user) {
            return user;
        }
        user = requestUser(userId);
        if (Objects.nonNull(user)) {
            CacheUtil.setUserCache(user);
        }
        return user;
    }

    /**
     * 获取用户名
     * 静态缓存
     *
     * @param userId 账号ID
     * @return 用户数据
     */
    public static String getUserName(Integer userId) {
        if (null == userId) {
            return null;
        }
        UserInfoDTO user = CacheUtil.getUser(userId);
        if (null != user) {
            return user.getName();
        }
        user = requestUser(userId);
        if (null == user) {
            return StringUtil.EMPTY;
        }
        CacheUtil.setUserCache(user);
        return user.getName();
    }

    /**
     * 获取用户ID
     *
     * @param identity 身份证号
     * @return 用户ID
     */
    public static int getUserIdByIdentity(String identity) {
        return CacheUtil.getUserIdByIdentity(identity);
    }

    /**
     * 获取用户ID
     *
     * @param uniqueCode 唯一标识
     * @return 用户ID
     */
    public static int getUserIdByUniqueCode(String uniqueCode) {
        return CacheUtil.getUserIdByUniqueCode(uniqueCode);
    }

    /**
     * 获取用户数据 Map
     * 静态缓存
     *
     * @param userIdList 账号ID列表
     * @return 用户数据 Map
     */
    public static Map<Integer, UserInfoDTO> getUserMap(Collection<Integer> userIdList) {
        Map<Integer, UserInfoDTO> userMap = new HashMap<>(userIdList.size());
        List<Integer> syncUserIdList = new ArrayList<>();
        for (Integer userId : userIdList) {
            UserInfoDTO user = CacheUtil.getUser(userId);
            if (null != user) {
                userMap.put(userId, user);
            } else {
                syncUserIdList.add(userId);
            }
        }
        if (CollectionUtils.isEmpty(syncUserIdList)) {
            return userMap;
        }
        List<UserInfoDTO> userList = requestUserList(syncUserIdList.toArray(new Integer[0]));
        for (UserInfoDTO user : userList) {
            userMap.put(user.getAccountId(), user);
            CacheUtil.setUserCache(user);
        }
        return userMap;
    }

    /**
     * 获取用户名称 Mao
     * 静态缓存
     *
     * @param userIdList userIdList 账号ID列表
     * @return 用户名称 Map
     */
    public static Map<Integer, String> getUserNameMap(Collection<Integer> userIdList) {
        Map<Integer, String> userMap = new HashMap<>(userIdList.size());
        List<Integer> syncUserIdList = new ArrayList<>();
        for (Integer userId : userIdList) {
            UserInfoDTO user = CacheUtil.getUser(userId);
            if (null != user) {
                userMap.put(userId, user.getName());
            } else {
                syncUserIdList.add(userId);
            }
        }
        if (CollectionUtils.isEmpty(syncUserIdList)) {
            return userMap;
        }
        List<UserInfoDTO> userList = requestUserList(syncUserIdList.toArray(new Integer[0]));
        for (UserInfoDTO user : userList) {
            userMap.put(user.getAccountId(), user.getName());
            CacheUtil.setUserCache(user);
        }
        return userMap;
    }

    /**
     * 获取用户数据 Map
     * 静态缓存
     *
     * @param userIdList 账号ID列表
     * @return 用户数据 Map
     */
    public static List<UserInfoDTO> getUserList(List<Integer> userIdList) {
        List<UserInfoDTO> userList = new ArrayList<>(userIdList.size());
        List<Integer> syncUserIdList = new ArrayList<>();
        for (Integer userId : userIdList) {
            UserInfoDTO user = CacheUtil.getUser(userId);
            if (null != user) {
                userList.add(user);
            } else {
                syncUserIdList.add(userId);
            }
        }
        if (CollectionUtils.isEmpty(syncUserIdList)) {
            return userList;
        }
        List<UserInfoDTO> requestUserList = requestUserList(syncUserIdList.toArray(new Integer[0]));
        for (UserInfoDTO user : requestUserList) {
            userList.add(user);
            CacheUtil.setUserCache(user);
        }
        return userList;
    }

    /**
     * 用户搜索
     *
     * @param keyword 搜索关键字
     * @return 用户列表
     */
    public static List<UserInfoDTO> searchUserList(String keyword) {
        return requestSearchUserList(keyword);
    }

    /**
     * 查询用户角色
     *
     * @param userId 用户id
     * @return 用户角色 List
     */
    public static List<RoleDTO> getUserRoleList(Integer userId) {
        return requestUserRoleList(userId);
    }

    /**
     * 获取部门下用户ID列表
     *
     * @param keyword 关键字
     * @return 用户ID List
     */
    public static List<Integer> searchUserIdList(String keyword) {
        if (StringUtil.isEmpty(keyword)) {
            return Collections.emptyList();
        }
        List<UserInfoDTO> userInfoList = requestSearchUserList(keyword);
        if (!CollectionUtils.isEmpty(userInfoList)) {
            return userInfoList.stream().map(UserInfoDTO::getAccountId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 获取部门下用户ID列表
     * Redis缓存
     *
     * @param deptId 部门ID
     * @return 用户ID List
     */
    public static List<Integer> getDeptUserIdList(int deptId) {
        String cacheKey = TEMP_DEPT_USER_ID_LIST_CACHE + deptId;
        if (RedisClient.hasKey(cacheKey)) {
            return getCacheIntegerList(cacheKey);
        }
        List<Integer> userIdList = requestDeptUserIdList(deptId);
        RedisClient.set(cacheKey, userIdList, randOutTime());
        return userIdList;
    }


    /**
     * 查询用户所在部门下的所有用户
     * Redis缓存
     *
     * @param userId 用户id
     * @return 用户ID List
     */
    public static List<Integer> lowerIdList(Integer userId) {
        String cacheKey = TEMP_LOWER_ID_LIST_CACHE + userId;
        if (RedisClient.hasKey(cacheKey)) {
            return getCacheIntegerList(cacheKey);
        }
        List<Integer> userIdList = requestLowerIdList(userId);
        RedisClient.set(cacheKey, userIdList, randOutTime());
        return userIdList;
    }

    /**
     * 获取部门下用户
     *
     * @param deptId 部门ID
     * @return 用户列表
     */
    public static List<DeptUserDTO> getDeptUserList(int deptId) {
        String cacheKey = TEMP_DEPT_USER_LIST_CACHE + deptId;
        if (RedisClient.hasKey(cacheKey)) {
            Object cache = RedisClient.get(cacheKey);
            return null == cache ? Collections.emptyList() : ParseUtil.objectToList(cache, DeptUserDTO.class);
        }
        List<DeptUserDTO> userDTOList = requestDeptUserList(deptId);
        RedisClient.set(cacheKey, userDTOList, randOutTime());
        return userDTOList;
    }

    /**
     * 获取部门下所有部门的用户ID列表
     * Redis缓存
     *
     * @param deptId 部门ID
     * @return 用户ID List
     */
    public static List<Integer> getDeptAllUserIdList(int deptId) {
        String cacheKey = TEMP_DEPT_ALL_USER_ID_CACHE + deptId;
        if (RedisClient.hasKey(cacheKey)) {
            List<Integer> cacheIntegerList = getCacheIntegerList(cacheKey);
            if (!CollectionUtils.isEmpty(cacheIntegerList)) {
                return cacheIntegerList;
            }
        }
        List<Integer> userIdList = requestDeptAllUserIdList(deptId);
        RedisClient.set(cacheKey, userIdList, randOutTime());
        return userIdList;
    }

    /**
     * 获取用户部门描述
     * 静态缓存
     *
     * @param userIdList 用户ID列表
     * @return key 用户ID value 部门描述
     */
    public static Map<Integer, String> getUserDeptDesrMap(Collection<Integer> userIdList) {
        Map<Integer, String> userDesrMap = new HashMap<>(userIdList.size());
        List<Integer> syncUserIdList = new ArrayList<>(userIdList.size());
        for (Integer userId : userIdList) {
            if (userDesrMap.containsKey(userId)) {
                continue;
            }
            String cacheDeptDesr = CacheUtil.getUserDeptDesr(userId);
            if (StringUtil.isEmpty(cacheDeptDesr)) {
                syncUserIdList.add(userId);
                continue;
            }
            userDesrMap.put(userId, cacheDeptDesr);
        }
        if (!CollectionUtils.isEmpty(syncUserIdList)) {
            Map<Integer, String> requestMap = requestUserDeptDesrMap(syncUserIdList.toArray(new Integer[0]));
            userDesrMap.putAll(requestMap);
        }
        return userDesrMap;
    }

    /**
     * 获取用户部门描述
     *
     * @param userId 用户ID
     * @return 部门描述
     */
    public static String getUserDeptDesr(int userId) {
        return CacheUtil.getUserDeptDesr(userId);
    }

    /**
     * 获取部门描述
     * 静态缓存
     *
     * @param idList 部门ID列表
     * @return key 部门ID value 部门描述
     */
    public static Map<Integer, String> getDeptDesrMap(Collection<Integer> idList) {
        Map<Integer, String> deptDesrMap = new HashMap<>(idList.size());
        List<Integer> getDesrIdList = new ArrayList<>(idList.size());
        for (Integer deptId : idList) {
            DeptCache dept = CacheUtil.getDept(deptId);
            if (deptDesrMap.containsKey(deptId)) {
                continue;
            }
            if (null == dept) {
                getDesrIdList.add(deptId);
                continue;
            }
            deptDesrMap.put(deptId, dept.getDesr());
        }
        if (!CollectionUtils.isEmpty(getDesrIdList)) {
            Map<Integer, String> requestMap = requestDeptDesrMap(getDesrIdList.toArray(new Integer[0]));
            deptDesrMap.putAll(requestMap);
        }
        return deptDesrMap;
    }

    /**
     * 获取CTI绑定的用户ID
     *
     * @param ctiCode     CTI编码
     * @param ctiUserCode CIT用户唯一标识
     * @return 用户ID
     */
    public static int getCtiUserId(int ctiCode, String ctiUserCode) {
        CtiRelateDTO relate = getCacheCtiRelate(ctiCode, ctiUserCode);
        if (null == relate) {
            return 0;
        }
        return relate.getAccountId();
    }

    /**
     * 获取CTI绑定的用户ID
     *
     * @param ctiCode     CTI编码
     * @param ctiUserCode CIT用户唯一标识
     * @return 用户ID
     */
    public static CtiRelateDTO getCtiUser(int ctiCode, String ctiUserCode) {
        return getCacheCtiRelate(ctiCode, ctiUserCode);
    }


    /**
     * 同步用户缓存
     * 需要定时同步
     */
    public static void syncUserCache() {
        int index = 0;
        while (true) {
            List<UserInfoDTO> userList = requestUserList(index);
            if (CollectionUtils.isEmpty(userList)) {
                return;
            }
            index += userList.size();
            for (UserInfoDTO user : userList) {
                CacheUtil.setUserCache(user);
            }
        }
    }

    /**
     * 同步部门缓存
     * 需要定时同步
     */
    public static void syncDeptCache() {
        List<DeptDTO> deptList = loadAllDept();
        Map<Integer, List<DeptUserDTO>> deptUserMap = loadAllUserDetail();
        for (DeptDTO dept : deptList) {
            // 保存部门领导信息
            List<DeptUserDTO> userList = deptUserMap.getOrDefault(dept.getId(), Collections.emptyList());
            Map<Integer, DeptCache.Leader> leaderMap = new HashMap<>(userList.size());
            for (DeptUserDTO user : userList) {
                if (null == user.getPost() || StringUtil.isEmpty(user.getPost().getPost())) {
                    continue;
                }
                int userManagerCode = ManagerEnum.parseCode(user.getPost().getPost());
                CacheUtil.setUserDataCache(parseCache(user, dept, userManagerCode));
                if (0 > userManagerCode) {
                    continue;
                }
                DeptCache.Leader leader = new DeptCache.Leader();
                leader.setUserId(user.getAccountId());
                leader.setUserName(user.getName());
                leader.setUniqueCode(user.getUniqueCode());
                leaderMap.put(userManagerCode, leader);
            }
            CacheUtil.setDeptCache(parseCache(dept, leaderMap));
        }

        CacheUtil.parseDeptLeader();
    }

    /**
     * 同步CTI绑定关系
     * 在拉取CTI数据前同步
     */
    public static void syncCtiRelateCache() {
        for (CtiEnum ctiEnum : CtiEnum.values()) {
            if (ctiEnum.isDisabled()) {
                continue;
            }
            List<CtiRelateDTO> relateList = requestCtiRelate(ctiEnum.getCode());
            for (CtiRelateDTO relate : relateList) {
                saveCtiRelateCache(ctiEnum.getCode(), relate.getCode(), relate);
            }
        }
    }

    /**
     * 获取用户领导 Map
     *
     * @param userId 用户ID
     * @return 用户领导 Map
     */
    public static Map<Integer, ManagerDTO> getUserLeaderMap(int userId) {
        return CacheUtil.getUserLeaderMap(userId);
    }

    /**
     * 获取用户部门ID
     *
     * @param userId 用户ID
     * @return 部门ID
     */
    public static int getUserDeptId(int userId) {
        return CacheUtil.getUserDeptId(userId);
    }

    /**
     * 获取部门领导 Map
     *
     * @param deptId 部门ID
     * @return 部门领导 Map
     */
    public static Map<Integer, ManagerDTO> getDeptLeaderMap(int deptId) {
        return CacheUtil.getDeptLeaderMap(deptId);
    }


    // 封装 SSOClient 的请求


    /**
     * 校验数据权限
     *
     * @param token 用户标识
     * @param path  权限路径
     * @return 是否通过验证
     */
    private static boolean requestVerify(String token, String path) {
        return ParseUtil.parseBoolean(SsoClient.requestVerify(token, path));
    }

    /**
     * 判断用户是否在部门下
     *
     * @param userId  用户ID
     * @param deptIds 部门ID列表
     * @return 是否在部门下
     */
    private static boolean requestUserCheckDept(int userId, Integer... deptIds) {
        return ParseUtil.parseBoolean(SsoClient.requestUserCheckDept(userId, deptIds));
    }

    /**
     * 判断权限下是否有该用户
     *
     * @param permId 权限ID
     * @param userId 用户ID
     * @return 权限下是否有该用户
     */
    private static boolean requestPermHasUser(int permId, int userId) {
        return ParseUtil.parseBoolean(SsoClient.requestPermHasUser(permId, userId));
    }

    /**
     * 请求角色权限判断列表
     *
     * @param path 权限路径
     * @return 角色检查列表
     */
    private static List<RolePermCheckDTO> requestRolesCheck(String path) {
        return ParseUtil.parseRoleCheckListResponse(SsoClient.requestRolesCheck(path));
    }

    /**
     * 请求用户数据
     *
     * @param userId 账号ID
     * @return UserInfoDTO
     */
    private static UserInfoDTO requestUser(int userId) {
        List<UserInfoDTO> userList = requestUserList(userId);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        return userList.get(0);
    }

    /**
     * 请求用户数据列表
     *
     * @param userIds 账号ID列表
     * @return UserInfoDTO List
     */
    private static List<UserInfoDTO> requestUserList(Integer... userIds) {
        return ParseUtil.parseUserInfoListResponse(SsoClient.requestUserList(userIds));
    }

    /**
     * 请求用户数据列表
     *
     * @param index 分页位置
     * @return UserInfoDTO List
     */
    private static List<UserInfoDTO> requestUserList(int index) {
        return ParseUtil.parseUserInfoListResponse(SsoClient.requestUserList(index));
    }

    /**
     * 请求用户详情数据列表
     *
     * @param index 分页位置
     * @return DeptUserDTO List
     */
    private static List<DeptUserDTO> requestUserDetailList(int index) {
        return ParseUtil.parseUserListResponse(SsoClient.requestUserDetailList(index));
    }

    /**
     * 请求部门下用户列表
     *
     * @param deptId 部门ID
     * @return UserInfoDTO List
     */
    private static List<DeptUserDTO> requestDeptUserList(int deptId) {
        return ParseUtil.parseUserListResponse(SsoClient.requestDeptUserList(deptId));
    }

    /**
     * 请求该部门下用户ID列表
     *
     * @param deptId 部门ID
     * @return userId List
     */
    private static List<Integer> requestDeptUserIdList(int deptId) {
        return ParseUtil.parseIdListResponse(SsoClient.requestDeptUserIdList(deptId));
    }

    /**
     * 查询用户所在部门下的所有用户
     *
     * @param userId 用户ID
     * @return userId List
     */
    private static List<Integer> requestLowerIdList(int userId) {
        return ParseUtil.parseIdListResponse(SsoClient.requestLowerIdList(userId));
    }

    /**
     * 请求根据关键字查询用户ID列表
     *
     * @param keyword 关键字
     * @return userId List
     */
    private static List<UserInfoDTO> requestSearchUserList(String keyword) {
        return ParseUtil.parseUserInfoListResponse(SsoClient.requestSearchUserList(keyword));
    }

    /**
     * 请求用户角色列表
     *
     * @param userId 用户id
     * @return 用户角色 List
     */
    private static List<RoleDTO> requestUserRoleList(Integer userId) {
        return ParseUtil.parseUserRoleListResponse(SsoClient.requestUserDetail(userId));
    }

    /**
     * 请求部门下全部部门的用户ID列表
     *
     * @param deptId 部门ID
     * @return userId List
     */
    private static List<Integer> requestDeptAllUserIdList(int deptId) {
        return ParseUtil.parseIdListResponse(SsoClient.requestDeptAllUserIdList(deptId));
    }

    /**
     * 请求用户部门描述
     *
     * @param userIds 用户ID列表
     * @return key 用户ID value 部门描述
     */
    private static Map<Integer, String> requestUserDeptDesrMap(Integer... userIds) {
        return ParseUtil.parseIntegerMapResponse(SsoClient.requestUserDeptDesrMap(userIds));
    }

    /**
     * 请求部门描述
     *
     * @param ids 部门ID列表
     * @return key 部门ID value 部门描述
     */
    private static Map<Integer, String> requestDeptDesrMap(Integer... ids) {
        return ParseUtil.parseIntegerMapResponse(SsoClient.requestDeptDesrMap(ids));
    }

    /**
     * 请求CTI关联
     *
     * @param ctiCode CTI编码
     * @return 关联
     */
    private static List<CtiRelateDTO> requestCtiRelate(int ctiCode) {
        return ParseUtil.parseCtiRelateListResponse(SsoClient.requestCtiRelate(ctiCode));
    }

    /**
     * 读取所有部门
     *
     * @return 所有部门列表
     */
    private static List<DeptDTO> loadAllDept() {
        return ParseUtil.parseDeptListResponse(SsoClient.loadAllDept());
    }

    public static Map<Integer, List<DeptUserDTO>> loadAllUserDetail() {
        int index = 0;
        Map<Integer, List<DeptUserDTO>> deptUserMap = new HashMap<>(DEFAULT_MAP_SIZE);
        while (true) {
            List<DeptUserDTO> userList = requestUserDetailList(index);
            if (CollectionUtils.isEmpty(userList)) {
                return deptUserMap;
            }
            for (DeptUserDTO deptUser : userList) {
                if (deptUser.getDepartmentId() == 0 || !deptUser.getIsAlive()) {
                    continue;
                }
                if (!deptUserMap.containsKey(deptUser.getDepartmentId())) {
                    deptUserMap.put(deptUser.getDepartmentId(), new ArrayList<>());
                }
                deptUserMap.get(deptUser.getDepartmentId()).add(deptUser);
            }
            index += userList.size();
        }
    }

    // 缓存处理

    private static void saveCtiRelateCache(int ctiCode, String ctiUserCode, CtiRelateDTO relate) {
        RedisClient.hSetPersist(HASH_CIT_RELATE_USER_CACHE_HEAD + ctiCode, ctiUserCode, relate);
    }

    private static List<CtiRelateDTO> getCacheCtiRelateList(int ctiCode) {
        Map<String, Object> ctiCodeMap = RedisClient.hGetAll(HASH_CIT_RELATE_USER_CACHE_HEAD + ctiCode);
        List<CtiRelateDTO> relateList = new ArrayList<>(ctiCodeMap.size());
        for (Object cache : ctiCodeMap.values()) {
            if (cache instanceof CtiRelateDTO) {
                relateList.add((CtiRelateDTO) cache);
            }
        }
        return relateList;
    }

    private static CtiRelateDTO getCacheCtiRelate(int ctiCode, String ctiUserCode) {
        Object cache = RedisClient.hGet(HASH_CIT_RELATE_USER_CACHE_HEAD + ctiCode, ctiUserCode);
        if (null == cache) {
            return null;
        }
        if (cache instanceof CtiRelateDTO) {
            return (CtiRelateDTO) cache;
        }
        return null;
    }

    private static List<Integer> getCacheIntegerList(String cacheKey) {
        Object cache = RedisClient.get(cacheKey);
        return null == cache ? Collections.emptyList() : ParseUtil.objectToList(cache, Integer.class);
    }

    private static int randOutTime() {
        return RandomUtil.getRandNum(MIN_OUT_TIME, MAX_OUT_TIME);
    }

    private static UserCache parseCache(DeptUserDTO user, DeptDTO dept, int managerCode) {
        UserCache userData = new UserCache();
        userData.setDeptDesr(dept.getDesr());
        userData.setDeptId(dept.getId());
        userData.setAccountId(user.getAccountId());
        userData.setName(user.getName());
        userData.setPosition(user.getPost().getPosition());
        userData.setStandardPost(user.getPost().getStandardPost());
        userData.setManagerCode(managerCode);
        return userData;
    }

    private static DeptCache parseCache(DeptDTO dept, Map<Integer, DeptCache.Leader> leaderMap) {
        DeptCache deptCache = new DeptCache();
        deptCache.setDeptId(dept.getId());
        deptCache.setName(dept.getName());
        deptCache.setDesr(dept.getDesr());
        deptCache.setParentId(dept.getParentId());
        deptCache.setLeaderMap(leaderMap);
        return deptCache;
    }

}
