package cn.hoxinte.tool.clients.sso.enums;

import cn.hoxinte.tool.utils.StringUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CTI平台 枚举
 *
 * @author dominate
 * @since 2022/04/02
 */
@Getter
public enum CtiPlatformEnum {
    /**
     * 未知CTI平台
     */
    UNKNOWN(0, "未知CTI平台", false, false, "wav"),
    DU_YAN_PLATFORM(1, "度言", true, true, "mp3"),
    JIN_HONG_PLATFORM(2, "金鸿", true, true, "mp3"),
    QUAN_YU_PLATFORM(3, "全宇", true, true, "mp3"),
    LIAN_TENG_PLATFORM(4, "联腾", true, true, "mp3"),
    RONG_LIAN_PLATFORM(5, "容联", false, false, "mp3"),
    HENG_XIN_TONG_PLATFORM(6, "恒信通", false, false, "mp3"),
    ITALK_CLOUD_PLATFORM(7, "智言云", true, true, "wav"),
    XUAN_WU_PLATFORM(8, "玄武", true, true, "wav"),
    YUN_KE_PLATFORM(9, "云客", true, true, "mp3"),
    KE_FU_PLATFORM(10, "客服系统", true, true, "mp3"),
    CUSTOMIZE_PLATFORM(11, "外部系统", true, true, "mp3");

    final int code;
    final String name;
    final boolean update;

    final boolean autoImport;
    final String suffix;

    public static List<CtiEnum> getCtiByPlatform(CtiPlatformEnum platformEnum) {
        List<CtiEnum> ctiEnumList = new ArrayList<>();
        for (CtiEnum ctiEnum : CtiEnum.values()) {
            if (platformEnum == ctiEnum.getPlatformEnum()) {
                ctiEnumList.add(ctiEnum);
            }
        }
        return ctiEnumList;
    }

    public static Map<Integer, String> getCtiMap() {
        Map<Integer, String> ctiMap = new HashMap<>(CtiEnum.values().length);
        for (CtiEnum ctiEnum : CtiEnum.values()) {
            if (CtiEnum.UNKNOWN == ctiEnum) {
                continue;
            }
            ctiMap.put(ctiEnum.getCode(), ctiEnum.getName());
        }
        return ctiMap;
    }

    CtiPlatformEnum(Integer code, String name, boolean update, boolean autoImport, String suffix) {
        this.code = code;
        this.name = name;
        this.update = update;
        this.autoImport = autoImport;
        this.suffix = suffix;
    }

    public static CtiPlatformEnum getValueByCode(int code) {
        for (CtiPlatformEnum stateEnum : CtiPlatformEnum.values()) {
            if (stateEnum.code == code) {
                return stateEnum;
            }
        }
        return UNKNOWN;
    }

    public static Map<Integer, String> getAll() {
        Map<Integer, String> resultMap = new HashMap<>();
        for (CtiPlatformEnum value : CtiPlatformEnum.values()) {
            resultMap.put(value.code, value.name);
        }
        return resultMap;
    }

    public static List<Integer> toList() {
        List<Integer> resultList = new ArrayList<>();
        for (CtiPlatformEnum value : CtiPlatformEnum.values()) {
            resultList.add(value.code);
        }
        return resultList;
    }

    public static String getNameByCode(int code) {
        for (CtiPlatformEnum stateEnum : CtiPlatformEnum.values()) {
            if (stateEnum.code == code) {
                return stateEnum.getName();
            }
        }
        return StringUtil.EMPTY;
    }

    public static String parseSuffix(CtiEnum ctiEnum, String url) {
        return parseSuffix(ctiEnum.getPlatformEnum(), url);
    }

    public static String parseSuffix(CtiPlatformEnum platformEnum, String url) {
        if(StringUtil.isEmpty(url)){
            return platformEnum.getSuffix();
        }
        final int maxSuffixLength = 5;
        final int notFoundIndex = -1;
        final String suffixSign = ".";

        int suffixIndex = url.lastIndexOf(suffixSign);
        if (notFoundIndex == suffixIndex || url.length() - suffixIndex > maxSuffixLength) {
            return platformEnum.getSuffix();
        }
        return url.substring(suffixIndex + 1);
    }

}
