package cn.hoxinte.tool.utils;

/**
 * 二进制工具
 *
 * @author dominate
 * @since 2022/11/1
 */
public final class HexUtil {


    private static final String FIRST_SIGN_NUM = "1";
    private static final String TRUE_NUM = "1";
    private static final String FALSE_NUM = "0";
    private static final String EMPTY = "";
    private static final int HEX_RADIX = 2;


    /**
     * 创建二进制记录
     *
     * @param total 记录长度
     * @param index 设置为 true 的位置
     * @return 二进制记录
     */
    public static int createHexRecord(int total, int index) {
        StringBuilder recordNums = new StringBuilder(FIRST_SIGN_NUM);
        for (int i = 0; i < total; i++) {
            recordNums.append(index == i ? TRUE_NUM : FALSE_NUM);
        }
        return Integer.parseInt(recordNums.toString(), HEX_RADIX);
    }

    /**
     * 创建数组记录
     * @param total 记录长度
     * @param index 设置为 true 的位置
     * @return 数组记录
     */
    public static boolean[] createArrayRecord(int total, int index) {
        boolean[] recordArray = new boolean[total];
        for (int i = 0; i < total; i++) {
            recordArray[i] = index == i;
        }
        return recordArray;
    }

    /**
     * 转换 boolean数组记录 为 二进制记录
     *
     * @param recordArray boolean数组记录
     * @return 二进制记录
     */
    public static int transform(boolean[] recordArray) {
        StringBuilder recordNums = new StringBuilder(FIRST_SIGN_NUM);
        for (boolean record : recordArray) {
            recordNums.append(record ? TRUE_NUM : FALSE_NUM);
        }
        return Integer.parseInt(recordNums.toString(), HEX_RADIX);
    }

    /**
     * 转换 二进制记录 为 boolean数组记录
     *
     * @param recordHex 二进制记录
     * @return boolean数组记录
     */
    public static boolean[] transform(int recordHex) {
        String recordNums = Integer.toBinaryString(recordHex);
        boolean[] recordArray = new boolean[recordNums.length() - 1];
        String[] recordNumArray = recordNums.split(EMPTY);
        for (int i = 1; i < recordNumArray.length; i++) {
            recordArray[i - 1] = recordNumArray[i].equals(TRUE_NUM);
        }
        return recordArray;
    }
}
