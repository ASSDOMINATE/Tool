package com.hoxinte.tool.utils;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author 计泽汉
 */
public class CalculateUtil {

    public final static String PER_CENT = "%";

    /**
     * 小数位数抓换，被除数
     */
    public final static int DIVIDEND = 100;
    /**
     * 除法小数精确位数
     */
    public final static int SCALE_TWO = 2;
    public final static int SCALE_FOUR = 4;

    /**
     * 除法
     * @param dividend 被除数
     * @param divisor 除数
     * @param scale 精确小数位数
     * @return 结果
     */
    public static BigDecimal divide(Integer dividend, Integer divisor, Integer scale) {
        if (null == divisor || divisor == 0 || null == dividend) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(dividend).divide(new BigDecimal(divisor), scale, RoundingMode.HALF_UP);
    }

    /**
     * 除法
     * @param dividend 被除数
     * @param divisor 除数
     * @param scale 精确小数位数
     * @return 结果
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, Integer scale) {
        if (dividend.compareTo(BigDecimal.ZERO) == 0 || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, scale, RoundingMode.HALF_UP);
    }

    /**
     * 乘法
     * @param num1 乘数1
     * @param num2 乘数2
     * @param scale 精确度
     * @return 结果
     */
    public static BigDecimal multiply(BigDecimal num1, BigDecimal num2, Integer scale) {
        return num1.multiply(num2).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 百分号转换
     * @return
     */
    public static String perCentTransfer(BigDecimal data){
        return multiply(data, new BigDecimal(DIVIDEND), SCALE_TWO) + PER_CENT;
    }

    /**
     * 百分号转换
     * @return
     */
    public static String perCentTransfer(BigDecimal dividend, BigDecimal divisor){
        BigDecimal data = divide(dividend, divisor, 4);
        return multiply(data, new BigDecimal(DIVIDEND), SCALE_TWO) + PER_CENT;
    }
}
