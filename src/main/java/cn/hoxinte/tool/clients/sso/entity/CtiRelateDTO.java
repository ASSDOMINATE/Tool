package cn.hoxinte.tool.clients.sso.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * CTI code 关联 ehr id
 * </p>
 *
 * @author dominate
 * @since 2022-04-02
 */
@Getter
@Setter
@Accessors(chain = true)
public class CtiRelateDTO implements Serializable {

    private String code;

    private String ehrId;

    private Integer accountId;

    public CtiRelateDTO() {
    }

    public CtiRelateDTO(String code, String ehrId, Integer accountId) {
        this.code = code;
        this.ehrId = ehrId;
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "CtiRelateDTO{" +
                "code='" + code + '\'' +
                ", ehrId='" + ehrId + '\'' +
                '}';
    }
}
