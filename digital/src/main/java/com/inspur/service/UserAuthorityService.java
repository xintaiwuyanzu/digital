package com.inspur.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.bsp.bo.BspPerson;
import com.dr.digital.bsp.bo.BspRole;
import com.dr.framework.rpc.ResultMapper;

/**
 * bsp 用户权限服务
 *
 * @author dr
 */
public interface UserAuthorityService {
    /**
     * 1.3.108　根据组织机构代码、应用编码、用户名称来获取该组织机构下的用户详细信息（模糊查询）
     *
     * @param orgCode  部门编码
     * @param appCode  应用编码
     * @param userName 用户名称
     * @return
     */
    @ResultMapper(targetClass = JSONObject.class, dataKey = "rows", messageKey = "error")
    List<BspPerson> getUserByOrg(String orgCode, String appCode, String userName);

    /**
     * 1.3.109　根据用户ID来获取用户基本信息
     *
     * @param userId
     * @return
     */
    @ResultMapper(value = false, targetClass = Map.class)
    BspPerson getUserInfoById(String userId);

    /**
     * 1.3.98　根据角色id获取角色信息
     *
     * @param roleId 角色id
     * @return
     */
    @ResultMapper(messageKey = "error", dataKey = "role", targetClass = JSONObject.class)
    BspRole getRoleInfoById(String roleId);
}
