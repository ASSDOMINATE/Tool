package com.hoxinte.tool.utils;

import com.hoxinte.tool.clients.entity.OssProperties;
import com.hoxinte.tool.clients.sso.entity.RoleDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBaseUtil {

    @Test
    public void testBeanSet() {
        OssProperties base = new OssProperties();
        OssProperties target = new OssProperties();
        target.setAccessKeyId("1");
        target.setBucketName("2");
        BaseUtil.set(base, target);
        assert target.getAccessKeyId().equals(base.getAccessKeyId());
        assert target.getBucketName().equals(base.getBucketName());

        OssProperties source = new OssProperties();
        assert !BaseUtil.hasValue(source);
        assert !BaseUtil.compare(base, target, source);

        target.setEndPoint("3");
        assert BaseUtil.compare(base, target, source);
        assert BaseUtil.hasValue(source);
        assert StringUtils.isEmpty(source.getAccessKeyId());
        assert StringUtils.isEmpty(source.getBucketName());
        assert source.getEndPoint().equals(target.getEndPoint());
    }

    @Test
    public void testBeanTransferMap() {
        RoleDTO base = new RoleDTO();
        base.setPlatformId(1);
        base.setPlatformName("1");
        Map<String, String> stringStringMap = BaseUtil.beanToStringMap(base);
        assert stringStringMap.get("platformId").equals("1");
        assert stringStringMap.get("platformName").equals("1");
        RoleDTO target = BaseUtil.stringMapToBean(stringStringMap, RoleDTO.class);
        assert target != null;
        assert base.getPlatformId().equals(target.getPlatformId());
        assert base.getPlatformName().equals(target.getPlatformName());
        Map<String, Object> stringObjectMap = BaseUtil.beanToMap(base);
        assert stringObjectMap.get("platformId").equals(1);
        assert stringObjectMap.get("platformName").equals("1");
        target = BaseUtil.mapToBean(stringObjectMap, RoleDTO.class);
        assert target != null;
        assert base.getPlatformId().equals(target.getPlatformId());
        assert base.getPlatformName().equals(target.getPlatformName());
    }

    @Test
    public void testObjectToCollect() {
        List<RoleDTO> list = new ArrayList<>();
        Map<Integer, RoleDTO> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            RoleDTO role = new RoleDTO();
            role.setRoleId(i);
            list.add(role);
            map.put(i, role);
        }
        List<RoleDTO> objectList = BaseUtil.obj2List(list, RoleDTO.class);
        for (int i = 0; i < objectList.size(); i++) {
            assert objectList.get(i).getRoleId() == i;
        }
        Map<Integer, RoleDTO> objectMap = BaseUtil.obj2Map(map, Integer.class, RoleDTO.class);
        for (Map.Entry<Integer, RoleDTO> integerRoleEntry : objectMap.entrySet()) {
            assert integerRoleEntry.getKey().equals(integerRoleEntry.getValue().getRoleId());
        }
    }
}
