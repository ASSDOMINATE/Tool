package cn.hoxinte.tool.clients.redis;

import cn.hoxinte.tool.utils.BaseUtil;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import redis.clients.jedis.*;

import java.util.*;

/**
 * Redis 基础操作客户端
 * 支持单点、哨兵、集群，根据配置文件优先级为 集群、哨兵、单点
 *
 * @author dominate
 */
public class RedisClient {


    private static final int DEFAULT_OVERTIME_SECONDS = 60 * 5;
    private static final int MAX_ATTEMPTS = 3;

    // 初始化

    /**
     * JedisPool JedisSentinelPool 需要执行完成后把取出来对 Jedis 释放连接
     * JedisCluster 不需要手动释放连接，将在执行完成自动释放
     */

    private static final JedisPool JEDIS_POOL;
    private static final JedisSentinelPool JEDIS_SENTINEL_POOL;
    private static final JedisCluster JEDIS_CLUSTER;

    private static RedissonClient REDISSON;


    static {
        JEDIS_POOL = initialPool();
        JEDIS_SENTINEL_POOL = initialSentinelPool();
        JEDIS_CLUSTER = initialCluster();
    }

    private static RedissonClient getRedisson() {
        if (REDISSON == null) {
            REDISSON = Redisson.create(RedisConfig.redissonConfig());
        }
        return REDISSON;
    }

    private static Jedis getJedis() {
        if (RedisConfig.USE_SENTINEL) {
            return JEDIS_SENTINEL_POOL.getResource();
        }
        return JEDIS_POOL.getResource();
    }

    private static final String HOST_PORT_SPLIT_STR = ":";

    private static Set<HostAndPort> parseHostAndPort(String[] nodes) {
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        for (String node : nodes) {
            String[] hostAndPort = node.split(HOST_PORT_SPLIT_STR);
            hostAndPortSet.add(new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
        }
        return hostAndPortSet;
    }

    private static JedisCluster initialCluster() {
        if (!RedisConfig.USE_CLUSTER) {
            return null;
        }
        Set<HostAndPort> clusterNode = parseHostAndPort(RedisConfig.CLUSTER_NODES);
        return new JedisCluster(clusterNode, RedisConfig.jedisClient(), MAX_ATTEMPTS, RedisConfig.genericPool());
    }

    private static JedisSentinelPool initialSentinelPool() {
        if (!RedisConfig.USE_SENTINEL) {
            return null;
        }
        Set<HostAndPort> sentinelNode = parseHostAndPort(RedisConfig.SENTINEL_NODES);
        return new JedisSentinelPool(RedisConfig.SENTINEL_MASTER_NAME, sentinelNode, RedisConfig.jedisPool(), RedisConfig.jedisClient(), RedisConfig.jedisClient());
    }

    private static JedisPool initialPool() {
        if (RedisConfig.USE_CLUSTER || RedisConfig.USE_SENTINEL) {
            return null;
        }
        return new JedisPool(RedisConfig.jedisPool(), new HostAndPort(RedisConfig.HOST, RedisConfig.PORT), RedisConfig.jedisClient());
    }


    // 封装好的缓存基础函数

    /**
     * 设置缓存 默认5分钟过期
     *
     * @param key   缓存Key
     * @param value 缓存值
     * @return String 操作结果
     */
    public static String set(String key, Object value) {
        return setKeyValue(serialize(key), serialize(value), DEFAULT_OVERTIME_SECONDS);
    }

    /**
     * 设置缓存
     *
     * @param key    缓存Key
     * @param value  缓存值
     * @param second 缓存到期时间 秒
     * @return String 操作结果
     */
    public static String set(String key, Object value, int second) {
        return setKeyValue(serialize(key), serialize(value), second);
    }

    /**
     * 设置永久缓存
     *
     * @param key   缓存Key
     * @param value 缓存值值
     * @return Long 操作结果
     */
    public static Long setPersist(String key, Object value) {
        return setPersistKeyValue(serialize(key), serialize(value));
    }

    /**
     * 设置散列缓存
     *
     * @param key   散列缓存key
     * @param field 字段
     * @param value 缓存值
     * @return Long 操作结果
     */
    public static Long hSetPersist(String key, String field, Object value) {
        return hSetKeyValue(serialize(key), serialize(field), serialize(value));
    }

    /**
     * 查询缓存到期时间
     *
     * @param key 缓存Key
     * @return long 缓存过期时间 秒
     */
    public static long ttl(String key) {
        return ttl(serialize(key));
    }

    /**
     * 为 Key 设置过期时间
     *
     * @param key     缓存key
     * @param seconds 缓存过期时间 秒
     * @return Long 操作结果
     */
    public static long expire(String key, int seconds) {
        return expire(serialize(key), seconds);
    }

    /**
     * 获取分布式锁
     *
     * @param key 锁的值
     * @return 是否成功上锁
     */
    public static RLock lock(String key) {
        return getRedisson().getLock(key);
    }

    // 读取缓存

    /**
     * 读取缓存值
     *
     * @param key 缓存key
     * @return Object 缓存对象
     */
    public static Object get(String key) {
        return unSerialize(getValue(serialize(key)));
    }

    /**
     * 读取缓存值
     *
     * @param key    缓存key
     * @param tClass 元素类型
     * @param <T>    取出数据类
     * @return Object 缓存对象
     */
    public static <T> T get(String key, Class<T> tClass) {
        return tClass.cast(unSerialize(getValue(serialize(key))));
    }

    /**
     * 读取缓存值 List
     *
     * @param key    缓存key
     * @param tClass 列表元素类型
     * @param <T>    元素类型
     * @return 缓存数据
     */
    public static <T> List<T> getList(String key, Class<T> tClass) {
        Object object = unSerialize(getValue(serialize(key)));
        return BaseUtil.obj2List(object, tClass);
    }

    /**
     * 读取缓存值 Map
     *
     * @param key    缓存key
     * @param kClass Map元素Key类型
     * @param vClass Map元素Value类型
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @return 缓存数据
     */
    public static <K, V> Map<K, V> getMap(String key, Class<K> kClass, Class<V> vClass) {
        Object object = unSerialize(getValue(serialize(key)));
        return BaseUtil.obj2Map(object, kClass, vClass);
    }

    /**
     * 读取散列缓存一个字段值
     *
     * @param key   散列缓存key
     * @param field 字段
     * @return Object 缓存对象
     */
    public static Object hGet(String key, String field) {
        return unSerialize(hGetValue(serialize(key), serialize(field)));
    }

    /**
     * 读取散列缓存一个字段值
     *
     * @param key    散列缓存key
     * @param field  字段
     * @param tClass 元素类型
     * @param <T>    元素类型
     * @return Object 缓存对象
     */
    public static <T> T hGet(String key, String field, Class<T> tClass) {
        return tClass.cast(unSerialize(hGetValue(serialize(key), serialize(field))));
    }

    /**
     * 读取散列缓存中所有 值
     *
     * @param key 散列缓存key
     * @return Map key 字段 ，value 缓存对象
     */
    public static Map<String, Object> hGetAll(String key) {
        Map<byte[], byte[]> unSerializeMap = hGetAllValues(serialize(key));
        Map<String, Object> serializeMap = new HashMap<>(unSerializeMap.size());
        for (byte[] field : unSerializeMap.keySet()) {
            serializeMap.put((String) unSerialize(field), unSerialize(unSerializeMap.get(field)));
        }
        return serializeMap;
    }

    /**
     * 读取散列缓存中所有 值
     *
     * @param key    散列缓存key
     * @param tClass 元素类型
     * @param <T>    元素类型
     * @return Map key 字段 ，value 缓存对象
     */
    public static <T> Map<String, T> hGetAll(String key, Class<T> tClass) {
        Map<byte[], byte[]> unSerializeMap = hGetAllValues(serialize(key));
        Map<String, T> serializeMap = new HashMap<>(unSerializeMap.size());
        for (byte[] field : unSerializeMap.keySet()) {
            serializeMap.put((String) unSerialize(field), tClass.cast(unSerialize(unSerializeMap.get(field))));
        }
        return serializeMap;
    }


    /**
     * 查询缓存是否存在
     *
     * @param key 缓存Key
     * @return boolean 是否存在
     */
    public static boolean hasKey(String key) {
        return hasKey(serialize(key));
    }

    /**
     * 查询散列缓存字段是否存在
     *
     * @param key   散列缓存key
     * @param field 字段
     * @return boolean 是否存在
     */
    public static boolean hHasKey(String key, String field) {
        return hHasKey(serialize(key), serialize(field));
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return Long 操作结果
     */
    public static Long removeKey(String key) {
        return removeKey(serialize(key));
    }

    /**
     * 删除list缓存字段
     *
     * @param key   key
     * @param value 字段
     * @return Long 操作结果
     */
    public static Long lRemove(String key, String value) {
        return lRem(serialize(key), serialize(value));
    }

    /**
     * 删除散列缓存字段
     *
     * @param key   散列缓存key
     * @param field 字段
     * @return Long 操作结果
     */
    public static Long hRemoveField(String key, String field) {
        return hRemoveFields(serialize(key), serialize(field));
    }

    /**
     * 批量删除散列缓存字段
     *
     * @param key    散列缓存key
     * @param fields 字段数组
     * @return Long 操作结果
     */
    public static Long hRemoveFields(String key, String[] fields) {
        byte[][] bytes = new byte[fields.length][];
        for (int i = 0; i < fields.length; i++) {
            bytes[i] = serialize(fields[i]);
        }
        return hRemoveFields(serialize(key), bytes);
    }

    /**
     * 列表指定位置设置值
     *
     * @param key   缓存Key
     * @param index 指定位置
     * @param value 保存数据
     * @return String 操作结果
     */
    public static String listSet(String key, long index, Object value) {
        return lSet(serialize(key), index, serialize(value));
    }

    /**
     * 向队列最前面插入值
     *
     * @param key   缓存Key
     * @param value 保存数据
     * @return Long 操作结果
     */
    public static Long leftPush(String key, Object value) {
        return lPush(serialize(key), serialize(value));
    }

    /**
     * 向队列最后面插入值
     *
     * @param key   缓存Key
     * @param value 保存数据
     * @return Long 操作结果
     */
    public static Long rightPush(String key, Object value) {
        return rPush(serialize(key), serialize(value));
    }

    /**
     * 获取列表长度
     *
     * @param key 缓存Key
     * @return 列表长度
     */
    public static long listLength(String key) {
        return lLen(serialize(key));
    }

    /**
     * 获取列表指定范围
     *
     * @param key   缓存Key
     * @param start 开始位置
     * @param end   结束位置
     * @return 数据列表
     */
    public static List<Object> listRange(String key, long start, long end) {
        List<byte[]> list = lRange(serialize(key), start, end);
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(list.size());
        for (byte[] cache : list) {
            result.add(unSerialize(cache));
        }
        return result;
    }

    /**
     * 获取列表指定范围
     *
     * @param key    缓存Key
     * @param start  开始位置
     * @param end    结束位置
     * @param tClass 元素类型
     * @param <T>    元素类型
     * @return 数据列表
     */
    public static <T> List<T> listRange(String key, long start, long end, Class<T> tClass) {
        List<byte[]> list = lRange(serialize(key), start, end);
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(list.size());
        for (byte[] cache : list) {
            result.add(tClass.cast(unSerialize(cache)));
        }
        return result;
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @param key 缓存key
     * @return 列表的第一个元素
     */
    public static Object leftPop(String key) {
        return unSerialize(lPop(serialize(key)));
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @param key    缓存key
     * @param tClass 元素类型
     * @param <T>    元素类型
     * @return 列表的第一个元素
     */
    public static <T> T leftPop(String key, Class<T> tClass) {
        return tClass.cast(unSerialize(lPop(serialize(key))));
    }

    /**
     * 移出并获取列表的最后一个元素
     *
     * @param key 缓存key
     * @return 列表的最后一个元素
     */
    public static Object rightPop(String key) {
        return unSerialize(rPop(serialize(key)));
    }

    /**
     * 移出并获取列表的最后一个元素
     *
     * @param key    缓存key
     * @param tClass 元素类型
     * @param <T>    元素类型
     * @return 列表的最后一个元素
     */
    public static <T> T rightPop(String key, Class<T> tClass) {
        return tClass.cast(unSerialize(rPop(serialize(key))));
    }

    /**
     * 设置set集合缓存
     *
     * @param key    缓存Key
     * @param values 缓存值值
     * @return 操作结果
     */
    public static Long addSetValue(String key, Object values) {
        return setAdd(serialize(key), serialize(values));
    }

    /**
     * 检查value是否是key对应set的成员
     *
     * @param key   缓存key
     * @param value 缓存值
     * @return 查询结果
     */
    public static Boolean sisMember(String key, Object value) {
        return sisMember(serialize(key), serialize(value));
    }

    /**
     * 查询key对应的set集合容量
     *
     * @param key 缓存key
     * @return set中元素数量
     */
    public static Long sCard(String key) {
        return sCard(serialize(key));
    }

    /**
     * set集合使用pipelined插入
     *
     * @param key   缓存key
     * @param value set元素数组
     */
    public static void pipeLinedSet(String key, String... value) {
        pipeLinedSet(serialize(key), convert(value));
    }

    // Jedis 操作函数

    private static final byte[] EMPTY_BYTE = new byte[0];
    private static final String EMPTY_STRING = "";

    //TODO pipelined 可能有点问题
    //TODO 需要重新调整下异常处理

    private static Long setAdd(byte[] key, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            JEDIS_CLUSTER.sadd(key, value);
            return JEDIS_CLUSTER.sadd(key, value);
        }
        try (Jedis jedis = getJedis()) {
            Pipeline pipelined = jedis.pipelined();
            pipelined.sadd(key, value);
            pipelined.sync();
            return jedis.sadd(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Boolean sisMember(byte[] key, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.sismember(key, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.sismember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Long sCard(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.scard(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.scard(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void pipeLinedSet(byte[] key, byte[]... value) {
        if (RedisConfig.USE_CLUSTER) {
            JEDIS_CLUSTER.sadd(key, value);
            return;
        }
        try (Jedis jedis = getJedis()) {
            Pipeline pipelined = jedis.pipelined();
            pipelined.sadd(key, value);
            pipelined.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<byte[]> lRange(byte[] key, long start, long end) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.lrange(key, start, end);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static long lLen(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.llen(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.llen(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static byte[] lPop(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.lpop(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.lpop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY_BYTE;
        }
    }

    private static byte[] rPop(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.rpop(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.rpop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY_BYTE;
        }
    }

    private static String lSet(byte[] key, long index, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.lset(key, index, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY_STRING;
        }
    }


    private static Long lPush(byte[] key, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.lpush(key, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.lpush(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static Long rPush(byte[] key, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.rpush(key, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.rpush(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static boolean hasKey(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.exists(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Long setPersistKeyValue(byte[] key, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.persist(key);
        }
        try (Jedis jedis = getJedis()) {
            jedis.set(key, value);
            return jedis.persist(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static Long removeKey(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.del(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static String setKeyValue(byte[] key, byte[] value, int seconds) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.setex(key, seconds, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.setex(key, seconds, value);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY_STRING;
        }
    }

    private static Long hRemoveFields(byte[] key, byte[]... fields) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.hdel(key, fields);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.hdel(key, fields);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static Long lRem(byte[] key, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.lrem(key, 0, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.lrem(key, 0, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static boolean hHasKey(byte[] key, byte[] field) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.hexists(key, field);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private static long ttl(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.ttl(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.ttl(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static Map<byte[], byte[]> hGetAllValues(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.hgetAll(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static byte[] hGetValue(byte[] key, byte[] field) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.hget(key, field);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.hget(key, field);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY_BYTE;
        }
    }

    private static Long hSetKeyValue(byte[] key, byte[] field, byte[] value) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.hset(key, field, value);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.hset(key, field, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static byte[] getValue(byte[] key) {
        if (RedisConfig.USE_CLUSTER) {
            return JEDIS_CLUSTER.get(key);
        }
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY_BYTE;
        }
    }


    private static long expire(byte[] bytes, int seconds) {
        try (Jedis jedis = getJedis()) {
            return jedis.expire(bytes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static Object unSerialize(byte[] bytes) throws RuntimeException {
        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer();
        return serializer.deserialize(bytes);
    }

    private static byte[] serialize(Object object) throws RuntimeException {
        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer();
        return serializer.serialize(object);
    }

    private static byte[][] convert(String[] strings) {
        byte[][] bytes = new byte[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            bytes[i] = serialize(strings[i]);
        }
        return bytes;
    }
}
