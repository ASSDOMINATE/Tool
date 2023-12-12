package com.hoxinte.tool.utils;


import java.util.List;
import java.util.Random;

/**
 * 随机工具
 *
 * @author dominate
 * @date 2018/04/20
 */
public final class RandomUtil {


    private static final String NUM = "num";
    private static final String CHAR = "char";

    private static final String BASE_WORDS_STR_FOR_RAND = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String BASE_NUMS_STR_FOR_RAND = "1234567890";
    private static final char[] BASE_RANDOM_STR = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * 随机取字符串数组中的一条，如果为空返回 ""
     *
     * @param contents 字符串数组
     * @return String 数组中随机一条
     */
    public static String getRandStrForArray(String[] contents) {
        if (contents.length == 0) {
            return "";
        }
        int index = getRandNum(0, contents.length - 1);
        return contents[index];
    }

    public static Integer getRandForArray(Integer[] array) {
        if (array.length == 0) {
            return -1;
        }
        int index = getRandNum(0, array.length - 1);
        return array[index];
    }


    /**
     * 生成指定长度的随机大写字符
     *
     * @param length 总长度
     * @return String 随机大写字符
     */
    public static String createRandomStrWords(int length) {
        return createRandStr(BASE_WORDS_STR_FOR_RAND, length);
    }

    /**
     * 生成指定长度的随机数字
     *
     * @param length 总长度
     * @return String 随机数字字符串
     */
    public static String createRandomStrNums(int length) {
        return createRandStr(BASE_NUMS_STR_FOR_RAND, length);
    }

    /**
     * 用基础字符串，按指定长度生成随机字符串
     *
     * @param baseStr 基础字符串
     * @param length  总长度
     * @return String 用基础字符串组合生成的字符串
     */
    public static String createRandStr(String baseStr, int length) {
        int baseStrSize = baseStr.length();

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(baseStrSize);
            sb.append(baseStr.charAt(number));
        }

        return sb.toString();
    }

    /**
     * 当前毫秒数 + Code + 随机数字字符串 生成长度为64的字符串
     *
     * @param code 基础字符串
     * @return String 毫秒数+字符+随机字符
     */
    public static String create64RandOrder(String code) {
        String timeStr = createUniqueTimestampStr();
        return createRandomNumsCode(timeStr + code, 64);
    }

    private static long LATEST_TIMESTAMP = 0L;

    private static String createUniqueTimestampStr() {
        return String.valueOf(createUniqueTimestamp());
    }

    public static synchronized long createUniqueTimestamp() {
        long thisTimestamp = System.currentTimeMillis();
        if (thisTimestamp > LATEST_TIMESTAMP) {
            LATEST_TIMESTAMP = thisTimestamp;
            return thisTimestamp;
        }
        return ++LATEST_TIMESTAMP;
    }

    /**
     * 当前毫秒数 + Num + 随机数字字符串 生成长度为32的字符串
     *
     * @param num 中间数字
     * @return String 毫秒数+数字+随机字符
     */
    public static String create32RandOrder(int num) {
        return createRandOrder(num, 32);
    }

    /**
     * 当前毫秒数 + Num + 随机数字字符串 生成长度为24的字符串
     *
     * @param num 中间数字
     * @return String 毫秒数+数字+随机字符
     */
    public static String create24RandOrder(int num) {
        return createRandOrder(num, 24);
    }

    /**
     * 当前毫秒数 + Num + 随机数字字符串 生成指定长度的字符串
     *
     * @param num    中间数字
     * @param length 总长度
     * @return String 毫秒数+数字+随机字符
     */
    public static String createRandOrder(int num, int length) {
        String timeStr = createUniqueTimestampStr();
        String numStr = String.valueOf(num);
        //如果组合已经大于了指定长度，对当前毫秒数进行截取，确保num的完整性
        if (timeStr.length() + numStr.length() > length) {
            timeStr = timeStr.substring(0, length - numStr.length());
        }
        // timeStr + numStr 的长度需要小于 size
        return createRandomNumsCode(timeStr + numStr, length);
    }

    /**
     * 生成随机编码
     * before + 毫秒数 + 补位随机字符
     *
     * @param length 编码长度
     * @param before 拼接在开头的字符
     * @return 唯一编码
     */
    public static String createUniqueCode(int length, String... before) {
        StringBuilder uniqueCode = new StringBuilder();
        for (String one : before) {
            uniqueCode.append(one);
        }
        uniqueCode.append(createUniqueTimestamp());
        if (uniqueCode.length() > length) {
            return uniqueCode.toString();
        }
        return createRandomNumsCode(uniqueCode.toString(), length);
    }

    /**
     * 基于当前时间生成唯一标识
     * 长度需大于当前时间毫秒数
     *
     * @param length 字符长度
     * @return String
     */
    public static String createUniqueCode(int length) {
        String timeStr = createUniqueTimestampStr();
        if (length <= timeStr.length()) {
            return timeStr;
        }
        return createRandomNumsCode(timeStr, length);
    }


    /**
     * BasicCode + 随机数字字符串 生成指定长度的字符串
     *
     * @param basicCode 开头字符
     * @param length    总长度
     * @return String 字符+随机字符
     */
    public static String createRandomNumsCode(String basicCode, int length) {
        int randNum = length - basicCode.length();
        assert randNum >= 0;
        return basicCode + createRandomStrNums(randNum);
    }

    /**
     * Mum + 随机字母字符串 生成指定长度的字符串
     *
     * @param num    开头数字
     * @param length 总长度
     * @return String 数字+随机字符
     */
    public static String createRandomWordsCode(int num, int length) {
        String numStr = String.valueOf(num);
        int randNum = length - numStr.length();
        return createRandomStrWords(randNum) + numStr;
    }

    /**
     * 获取一个指定范围的随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return int 随机数
     */
    public static int getRandNum(int min, int max) {
        return (new Random().nextInt(max - min + 1) + min);
    }


    /**
     * 生成指定长度的 随机数字+字母 字符串
     *
     * @param length 长度
     * @return String 随机字符串
     */
    public static String getStringRandom(int length) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? CHAR : NUM;
            //输出字母还是数字
            if (CHAR.equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                buffer.append((char) (random.nextInt(26) + temp));
            } else {
                buffer.append(random.nextInt(10));
            }
        }
        return buffer.toString();
    }

    /**
     * 生成随机密码
     *
     * @param pwdLen 生成的密码的总长度
     * @return 密码的字符串
     */
    public static String genRandomNum(int pwdLen) {
        // 35是因为数组是从0开始的，26个字母+10个数字
        final int maxNum = 36;
        // 生成的随机数
        int i;
        // 生成的密码的长度
        int count = 0;

        StringBuilder pwd = new StringBuilder();
        Random r = new Random();
        while (count < pwdLen) {
            // 生成随机数，取绝对值，防止生成负数，
            // 生成的数最大为36-1
            i = Math.abs(r.nextInt(maxNum));
            pwd.append(BASE_RANDOM_STR[i]);
            count++;
        }
        return pwd.toString();
    }


    /**
     * 用权重列表随机给出位置
     *
     * @param numList 权重列表
     * @return 位置
     */
    public static int randByNum(List<Integer> numList) {
        int total = 0;
        for (Integer num : numList) {
            total += num;
        }
        int thisRandNum = RandomUtil.getRandNum(0, total);
        int latestNum = 0;
        for (int i = 0; i < numList.size(); i++) {
            int endNum = latestNum + numList.get(i);
            if (thisRandNum <= endNum) {
                return i;
            }
            latestNum = endNum;
        }
        return 0;
    }
}
