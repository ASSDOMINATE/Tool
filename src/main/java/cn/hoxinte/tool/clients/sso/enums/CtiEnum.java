package cn.hoxinte.tool.clients.sso.enums;

import cn.hoxinte.tool.utils.StringUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * CTI 枚举
 *
 * @author dominate
 * @since 2022/04/02
 */
public enum CtiEnum {
    /**
     * 未知CTI平台
     */
    UNKNOWN(0, CtiPlatformEnum.UNKNOWN, "未知CTI平台", "", "", true),

    DY_1(1, CtiPlatformEnum.DU_YAN_PLATFORM, "北京恒昌德盛信用管理有限公司", "恒昌德盛", "qSjAkEkQmsGFVagbt9fRjIHa8VPleunB"),
    DY_2(2, CtiPlatformEnum.DU_YAN_PLATFORM, "湖南华威金安企业管理有限公司", "华威金安主", "dDyVrI4IwJ3juBdQUcYZCjCl7sG5BX7e"),
    DY_3(3, CtiPlatformEnum.DU_YAN_PLATFORM, "北京恒昌德盛信用管理有限公司_车贷", "车贷", "HNQmsJ8xIBCZc7MVfNEGS31lnLb1LtVt"),
    DY_4(4, CtiPlatformEnum.DU_YAN_PLATFORM, "湖南华威金安企业管理有限公司_贵阳职场", "贵阳职场", "rLzOFjDfEh2hqIC2kNFoFR3DxUJE7ha4"),
    DY_5(5, CtiPlatformEnum.DU_YAN_PLATFORM, "北京恒昌成都分公司", "成都", "6UtLf6Y9UtSfhbytqFAjUaIezDcMKyC1"),
    DY_7(6, CtiPlatformEnum.DU_YAN_PLATFORM, "北京恒昌德盛信用管理有限公司昆明分公司", "昆明职场", "a8OFU1UXNBrAakMxHX4Q4WPm8R6yXS4X", true),
    DY_6(7, CtiPlatformEnum.DU_YAN_PLATFORM, "恒昌业务_武汉一中心", "武汉一", "mcynEFnNzBf2kzZll9cOVBBF0tuyaVVr"),
    DY_8(8, CtiPlatformEnum.DU_YAN_PLATFORM, "恒昌业务_武汉二中心", "武汉二", "hjGK4MdMj6a61tI2FHwW5sQMsn90GrCi"),
    DY_9(9, CtiPlatformEnum.DU_YAN_PLATFORM, "恒昌业务_武汉三中心", "武汉三", "IH4wDSU7Ef488RIzbG3yrVRtlAAhIsLt"),
    DY_10(10, CtiPlatformEnum.DU_YAN_PLATFORM, "非恒业务_呼市/泉州/房山/福州职场", "非恒-呼泉房福", "sbfJmBnwoUaSeFMOApA9dZXkvSB4GmGM"),
    DY_11(18, CtiPlatformEnum.DU_YAN_PLATFORM, "北京恒昌德盛信用管理有限公司潍坊分公司", "恒昌潍坊", "a8OFU1UXNBrAakMxHX4Q4WPm8R6yXS4X"),

    JH_1(11, CtiPlatformEnum.JIN_HONG_PLATFORM, "", "", "C125", "FDA3E34DA08B8F27EE94EFADBB4E4A9A02A33BB1"),

    QY_1(12, CtiPlatformEnum.QUAN_YU_PLATFORM, "", "", "1000"),

    LT_1(13, CtiPlatformEnum.LIAN_TENG_PLATFORM, "华威金安兰州分公司", "", "xitongguanliyuan", "hwjalz8888"),

    RL_1(14, CtiPlatformEnum.RONG_LIAN_PLATFORM, "", "", "N00000015352", "0e348cc0-96c8-11e7-b661-81f285e6ab96"),

    HXT(15, CtiPlatformEnum.HENG_XIN_TONG_PLATFORM),

    FOC(16, CtiPlatformEnum.FORECAST_CALL_OUT_PLATFORM),

    XW_1(17, CtiPlatformEnum.XUAN_WU_PLATFORM, "", "5c34e9f4a961ac3d5ad2e9f99c582340", "8052445c53693a93b90a80df6e185f04", "ff817a847603cebfc3abfd26ba25dab9"),
    //company,签名key,超级管理员的partnerId
    YK_1(19, CtiPlatformEnum.YUN_KE_PLATFORM, "https://phone.yunkecn.com", "4gr2kt", "5D171662610C4DADBA0235", "pC6B08ED56BC542A9AFFEC6C0703D2E4B"),
    YK_2(20, CtiPlatformEnum.YUN_KE_PLATFORM, "http://yunke.credithc.com", "tyyzdf", "C952E44A38B32A5CBC1884E0F", "p79D34826DEA44B8E984E290860C7FB16");


    final int code;
    final CtiPlatformEnum platformEnum;
    final String name;
    final String alias;
    final String sign;
    final String auth;
    final boolean disabled;

    CtiEnum(int code, CtiPlatformEnum platformEnum) {
        this.code = code;
        this.platformEnum = platformEnum;
        this.name = "";
        this.alias = "";
        this.sign = "";
        this.auth = "";
        this.disabled = false;
    }

    CtiEnum(int code, CtiPlatformEnum platformEnum, String name, String alias, String sign) {
        this.code = code;
        this.platformEnum = platformEnum;
        this.name = name;
        this.alias = alias;
        this.sign = sign;
        this.auth = "";
        this.disabled = false;
    }

    CtiEnum(int code, CtiPlatformEnum platformEnum, String name, String alias, String sign, boolean disabled) {
        this.code = code;
        this.platformEnum = platformEnum;
        this.name = name;
        this.alias = alias;
        this.sign = sign;
        this.auth = "";
        this.disabled = disabled;
    }

    CtiEnum(int code, CtiPlatformEnum platformEnum, String name, String alias, String sign, String auth) {
        this.code = code;
        this.platformEnum = platformEnum;
        this.name = name;
        this.alias = alias;
        this.sign = sign;
        this.auth = auth;
        this.disabled = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        if (null == name || name.isEmpty()) {
            return platformEnum.getName();
        }
        return name;
    }

    /**
     * 平台名/名称/别名
     * 无名称，返回平台名
     * 有名称、别名，返回平台名+别名
     * 只有名称，返回平台名+名称
     *
     * @return 显示名称
     */
    public String getShowName() {
        if (StringUtil.isEmpty(name)) {
            return platformEnum.getName();
        }
        if (StringUtil.isNotEmpty(alias)) {
            return platformEnum.getName() + "-" + alias;
        }
        return platformEnum.getName() + "-" + name;
    }

    public CtiPlatformEnum getPlatformEnum() {
        return platformEnum;
    }

    public String getSign() {
        return sign;
    }

    public String getAuth() {
        return auth;
    }

    public String getAlias() {
        return alias;
    }

    public static CtiEnum getValueByCode(int code) {
        for (CtiEnum stateEnum : CtiEnum.values()) {
            if (code == stateEnum.code) {
                return stateEnum;
            }
        }
        return UNKNOWN;
    }

    public static List<CtiEnum> getCtiByPlatformCode(int platformCode) {
        List<CtiEnum> ctiEnumList = new ArrayList<>();
        for (CtiEnum ctiEnum : values()) {

            if (ctiEnum.platformEnum.getCode() == platformCode) {
                ctiEnumList.add(ctiEnum);
            }
        }
        return ctiEnumList;
    }

    public static List<CtiEnum> getCtiByPlatform(CtiPlatformEnum platformEnum) {
        List<CtiEnum> ctiEnumList = new ArrayList<>();
        for (CtiEnum ctiEnum : CtiEnum.values()) {
            if (ctiEnum.disabled) {
                continue;
            }
            if (platformEnum == ctiEnum.getPlatformEnum()) {
                ctiEnumList.add(ctiEnum);
            }
        }
        return ctiEnumList;
    }

    public static Integer getMaxCtiCode() {
        int maxCtiCode = 0;
        for (CtiEnum ctiEnum : CtiEnum.values()) {
            if (ctiEnum.getCode() > maxCtiCode) {
                maxCtiCode = ctiEnum.getCode();
            }
        }
        return maxCtiCode;
    }

    public static String getNameByCode(int code) {
        for (CtiEnum stateEnum : CtiEnum.values()) {
            if (code == stateEnum.code) {
                return stateEnum.getShowName();
            }
        }
        return UNKNOWN.name;
    }

    public static String getPlatformName(int code) {
        for (CtiEnum stateEnum : CtiEnum.values()) {
            if (code == stateEnum.code) {
                return stateEnum.getPlatformEnum().name;
            }
        }
        return UNKNOWN.name;
    }

    public static Map<Integer, Set<Integer>> getPlatformCodeCtiCodeMap(Set<Integer> delOrUnvalidConfList) {
        Map<Integer, Set<Integer>> codeMap = new HashMap<>();
        for (CtiEnum ctiEnum : values()) {
            if (CtiEnum.UNKNOWN == ctiEnum || delOrUnvalidConfList.contains(ctiEnum.getCode())) {
                continue;
            }
            Integer platformCode = ctiEnum.platformEnum.getCode();
            Set<Integer> existedCtiSet = codeMap.get(platformCode);
            if (CollectionUtils.isEmpty(existedCtiSet)) {
                existedCtiSet = new HashSet<>();
                existedCtiSet.add(ctiEnum.getCode());
                codeMap.put(platformCode, existedCtiSet);
            } else {
                existedCtiSet.add(ctiEnum.getCode());
            }
        }

        return codeMap;
    }

    public static List<Integer> allCode() {
        List<Integer> resultList = new ArrayList<>();
        for (CtiEnum ctiEnum : CtiEnum.values()) {
            if (CtiEnum.UNKNOWN == ctiEnum) {
                continue;
            }
            resultList.add(ctiEnum.getCode());
        }
        return resultList;
    }
}
