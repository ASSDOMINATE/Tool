package com.hoxinte.tool.clients.redis;

import com.hoxinte.tool.utils.BaseUtil;
import com.hoxinte.tool.utils.LoadUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamGroupInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Stream 客户端
 *
 * @author dominate
 * @since 2022/09/14
 */
public class StreamClient {

    private static final int TIME_OUT = 100000;
    private static final String PASSWORD = LoadUtil.getProperty("spring.redis.password");
    private static final int DATABASE = LoadUtil.getIntegerProperty("spring.redis.database");
    private static final String HOST = LoadUtil.getProperty("spring.redis.host");
    private static final int PORT = LoadUtil.getIntegerProperty("spring.redis.port");

    private static final JedisPool JEDIS_POOL;

    static {
        JEDIS_POOL = initialPool();
    }

    private static JedisPool initialPool() {
        return new JedisPool(JedisConf.pool(), HOST, PORT, TIME_OUT, PASSWORD, DATABASE);
    }

    public static Jedis getJedis() {
        return JEDIS_POOL.getResource();
    }

    public static final String STREAM_MQ = "sm:";
    private static final String LAST_DELIVERED_ID = "0-0";
    public final static int MQ_INFO_CONSUMER = 1;
    public final static int MQ_INFO_GROUP = 2;

    /**
     * 发送消息
     *
     * @param
     * @return
     */
    public static StreamEntryID send(String key, Object obj) {
        return produce(key, BaseUtil.beanToStringMap(obj));
    }

    /**
     * 发布消息到Stream
     *
     * @param key     key
     * @param message 消息内容
     * @return StreamEntryID 消息id
     */
    private static StreamEntryID produce(String key, Map<String, String> message) {
        try (Jedis jedis = getJedis()) {
            //xAdd 发布消息到
            return jedis.xadd(STREAM_MQ + key, StreamEntryID.NEW_ENTRY, message);
        } catch (Exception e) {
            throw new RuntimeException("Stream - product failed !");
        }
    }

    /**
     * 创建消费群组,消费群组不可重复创建
     *
     * @param key             stream key
     * @param groupName       群组名称
     * @param lastDeliveredId 消息id 没有就是从队列头部开始消费
     */
    public static void createCustomGroup(String key, String groupName, String lastDeliveredId) {
        if (lastDeliveredId == null) {
            lastDeliveredId = LAST_DELIVERED_ID;
        }
        try (Jedis jedis = getJedis()) {
            StreamEntryID id = new StreamEntryID(lastDeliveredId);
            // makeStream表示没有时是否自动创建stream，但是如果有，再自动创建会异常
            jedis.xgroupCreate(STREAM_MQ + key, groupName, id, false);
        } catch (Exception e) {
            throw new RuntimeException("Stream - create custom group failed !", e);
        }
    }

    /**
     * 自动接收消息
     *
     * @param key
     * @param customerName
     * @param groupName
     * @return
     */
    public static <T> List<T> autoReceived(String key, String customerName, String groupName, Class<T> clazz) {
        Map.Entry<String, List<StreamEntry>> rnt = consume(customerName, groupName, key);
        List<StreamEntry> streamList = rnt.getValue();
        StreamEntryID[] streamEntryIds = streamList.stream().map(StreamEntry::getID).toArray(StreamEntryID[]::new);
        ackMsg(key, groupName, streamEntryIds);
        del(key, streamEntryIds);
        List<T> result = new ArrayList<>(streamList.size());
        for (StreamEntry streamEntry : rnt.getValue()) {
            result.add(BaseUtil.stringMapToBean(streamEntry.getFields(), clazz));
        }
        return result;
    }

    private static Map.Entry<String, List<StreamEntry>> consume(String customerName, String groupName, String key) {
        List<Map.Entry<String, List<StreamEntry>>> entryList = consumes(customerName, groupName, key);
        assert entryList.size() != 0;
        return entryList.get(0);
    }

    /**
     * 消息消费
     *
     * @param keys         keys
     * @param customerName 消费者名称
     * @param groupName    组名
     * @return 消费结果
     */
    private static List<Map.Entry<String, List<StreamEntry>>> consumes(String customerName, String groupName, String... keys) {
        try (Jedis jedis = getJedis()) {
            // 以阻塞或者非阻塞方式读取，默认超时时间设为1毫秒
            XReadGroupParams xReadGroupParams = new XReadGroupParams().block(0).count(10);
            Map<String, StreamEntryID> streams = new HashMap<>(keys.length);
            for (String key : keys) {
                streams.put(STREAM_MQ + key, StreamEntryID.UNRECEIVED_ENTRY);
            }
            return jedis.xreadGroup(groupName, customerName, xReadGroupParams, streams);
        } catch (Exception e) {
            throw new RuntimeException("Stream - consume failed !", e);
        }
    }

    /**
     * 消息确认
     *
     * @param key       key
     * @param groupName 组名
     * @param ids       消费ID
     */
    private static void ackMsg(String key, String groupName, StreamEntryID... ids) {
        if (ids.length == 0) {
            throw new RuntimeException("msg id is empty !");
        }
        try (Jedis jedis = getJedis()) {
            jedis.xack(key, groupName, ids);
        } catch (Exception e) {
            throw new RuntimeException("Stream - ack msg failed !", e);
        }
    }

    /**
     * 删除
     *
     * @param key key
     * @param id  消费ID
     */
    private static void del(String key, StreamEntryID... id) {
        try (Jedis jedis = getJedis()) {
            jedis.xdel(STREAM_MQ + key, id);
        } catch (Exception e) {
            throw new RuntimeException("Stream - delete failed !", e);
        }
    }

    /**
     * 检查消费者群组是否存在，辅助方法
     *
     * @param key       key
     * @param groupName 组名
     * @return 是否存在
     */
    public static boolean checkGroup(String key, String groupName) {
        try (Jedis jedis = getJedis()) {
            List<StreamGroupInfo> xInfoGroupResult = jedis.xinfoGroups(STREAM_MQ + key);
            for (StreamGroupInfo groupInfo : xInfoGroupResult) {
                if (groupName.equals(groupInfo.getName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Stream - check group  failed !", e);
        }
    }

}
