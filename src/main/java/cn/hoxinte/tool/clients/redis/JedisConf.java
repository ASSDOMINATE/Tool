package cn.hoxinte.tool.clients.redis;

import cn.hoxinte.tool.utils.LoadUtil;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis 配置类
 *
 * @author dominate
 * @since 2022/09/14
 */
public class JedisConf {

    private static final int EVICTION_RUN_NUM = 3;

    private static final int MIN_IDLE = LoadUtil.getIntegerProperty("spring.redis.jedis.pool.min-idle");
    private static final int MAX_IDLE = LoadUtil.getIntegerProperty("spring.redis.jedis.pool.max-idle");
    private static final int MAX_TOTAL = LoadUtil.getIntegerProperty("spring.redis.jedis.pool.max-active");

    private static final int LOAD_TIMEOUT = LoadUtil.getIntegerProperty("spring.redis.timeout");

    private static final String PASSWORD = LoadUtil.getProperty("spring.redis.password");
    private static final int DATABASE = LoadUtil.getIntegerProperty("spring.redis.database");

    // 单点
    protected static final String HOST = LoadUtil.getProperty("spring.redis.host");
    protected static final int PORT = LoadUtil.getIntegerProperty("spring.redis.port");

    // 哨兵
    protected static final String SENTINEL_MASTER_NAME = LoadUtil.getProperty("spring.redis.sentinel.master");
    protected static final String[] SENTINEL_NODES = LoadUtil.getArrayProperty("spring.redis.sentinel.nodes");

    // 集群
    protected static final String[] CLUSTER_NODES = LoadUtil.getArrayProperty("spring.redis.cluster.nodes");

    private static final int TIME_OUT = LOAD_TIMEOUT == 0 ? 100000 : LOAD_TIMEOUT;

    public static DefaultJedisClientConfig client(){
        return DefaultJedisClientConfig.builder()
                .password(PASSWORD)
                .database(DATABASE)
                .timeoutMillis(TIME_OUT)
                .build();
    }

    public static JedisPoolConfig pool() {
        JedisPoolConfig config = new JedisPoolConfig();
        setPoolConfig(config);
        return config;
    }

    public static GenericObjectPoolConfig<Connection> genericPool() {
        GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
        setPoolConfig(config);
        return config;
    }

    private static void setPoolConfig(GenericObjectPoolConfig<?> config) {
        // 是否启用后进先出, 默认true
        config.setLifo(true);
        // 最小空闲连接数, 默认0
        config.setMinIdle(MIN_IDLE);
        // 最大空闲连接数, 默认8个
        config.setMaxIdle(MAX_IDLE);
        // 最大连接数, 默认8个
        config.setMaxTotal(MAX_TOTAL);
        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(EVICTION_RUN_NUM);
        // 在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(true);
        // 在空闲时检查有效性, 默认false
        config.setTestWhileIdle(true);
    }

}
