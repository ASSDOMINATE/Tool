package cn.hoxinte.tool.clients.sso.enums;

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
public enum CtiPlatformEnum {
    /**
     * 未知CTI平台
     */
    UNKNOWN(0, "未知CTI平台", ""),
    DU_YAN_PLATFORM(1, "度言", "wav"),
    JIN_HONG_PLATFORM(2, "金鸿", "mp3"),
    QUAN_YU_PLATFORM(3, "全宇", "mp3"),
    LIAN_TENG_PLATFORM(4, "联腾", "mp3", true, false),
    RONG_LIAN_PLATFORM(5, "容联", "mp3", true, true),
    HENG_XIN_TONG_PLATFORM(6, "恒信通", "mp3", false, false),
    FORECAST_CALL_OUT_PLATFORM(7, "自研外呼", "wav", false, true),
    XUAN_WU_PLATFORM(8, "玄武", "mp3"),
    YUN_KE_PLATFORM(9, "云客", "mp3" ,true, true);

    final int code;
    final String name;
    final String suffix;
    final boolean disabled;
    final boolean autoImport;

    CtiPlatformEnum(int code, String name, String suffix) {
        this.code = code;
        this.name = name;
        this.suffix = suffix;
        this.disabled = false;
        this.autoImport = true;
    }

    CtiPlatformEnum(int code, String name, String suffix, boolean disabled, boolean autoImport) {
        this.code = code;
        this.name = name;
        this.suffix = suffix;
        this.disabled = disabled;
        this.autoImport = autoImport;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isAutoImport() {
        return autoImport;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }

    public static String parseSuffix(CtiEnum ctiEnum, String url) {
        return parseSuffix(ctiEnum.getPlatformEnum(), url);
    }

    public static String parseSuffix(CtiPlatformEnum platformEnum, String url) {
        final int maxSuffixLength = 5;
        final int notFoundIndex = -1;
        final String suffixSign = ".";

        int suffixIndex = url.lastIndexOf(suffixSign);
        if (notFoundIndex == suffixIndex || url.length() - suffixIndex > maxSuffixLength) {
            return platformEnum.getSuffix();
        }
        return url.substring(suffixIndex + 1);
    }

    public static CtiPlatformEnum getValueByCode(int code) {
        for (CtiPlatformEnum stateEnum : CtiPlatformEnum.values()) {
            if (code == stateEnum.code) {
                return stateEnum;
            }
        }
        return UNKNOWN;
    }

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

}
