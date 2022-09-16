package com.inspur.service;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.bsp.bo.BspRole;
import com.dr.framework.rpc.ResultMapper;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;
import java.util.Map;

/**
 * BSP应用中心服务接口
 *
 * @author dr
 */
public interface ApplicationService {
    /**
     * 1.2.46　根据应用编码获得第三方应用信息
     *
     * @param appCode 应用编码
     * @return
     */
    Map<String, String> findAppinfoByKey(String appCode);

    /**
     * 1.2.21　获取应用角色树
     *
     * @param appCode 应用编码
     * @return
     */
    @ResultMapper(messageKey = "error", codeKey = "state", successCode = "1", dataKey = "role", targetClass = JSONObject.class)
    List<BspRole> getAppRoleTree(String appCode);
}
