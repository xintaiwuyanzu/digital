package com.inspur.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dr.digital.bsp.bo.BspDict;
import com.dr.digital.bsp.bo.BspOrganType;
import com.dr.digital.bsp.bo.BspOrganise;
import com.dr.digital.bsp.bo.BspRegion;
import com.dr.framework.rpc.ResultMapper;

/**
 * 组织机构同步
 * <p>
 *
 * @author dr
 */
public interface OrganizationService {

	/**
	 * 1.1.4通过字典类型获取字典信息表
	 *
	 * @param dictKIND 字典属性
	 * @param app_code 应用编码
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspDict> getDictBasicInfo(String dictKIND, String app_code);

	/**
	 * 1.1.5 通过父级字典获取字典基本信息
	 *
	 * @param dictKIND    字典属性
	 * @param app_code    应用编码
	 * @param parent_code 父级字典编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspDict> getDictBasicInfoByParent(String dictKIND, String app_code, String parent_code);

	/**
	 * 1.1.6通过字典类型以及字典编码获取字典名称
	 *
	 * @param dictKIND 字典属性
	 * @param dictCODE 字典编码
	 * @return 这个接口只返回了code和name
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspDict> getDictName(String dictKIND, String dictCODE);

	/**
	 * 1.1.7 通过字典类型获取字典编码和字典名称
	 *
	 * @param type 字典类型
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspDict> getDictType(String type);

	/**
	 * 1.1.8 通过app_code获取字典类型
	 *
	 * @param app_code 应用编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspDict> getDictKind(String app_code);

	/**
	 * 1.1.12 通过应用编码获取部门信息
	 *
	 * @param appCode 应用编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspOrganise> getOrganInfo(String appCode);

	/**
	 * 1.1.16 根据行政区划代码获取行政区划名称
	 *
	 * @param regionCode 区划编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspRegion> getRegionInfoByRegionCode(String regionCode);

	/**
	 * 1.1.17 根据当前区划CODE获取下级区划
	 *
	 * @param regionCode 区划编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspRegion> getChildRegionByRegionCode(String regionCode);

	/**
	 * 1.1.20 根据区划查询当前区划下的区划以及部门信息
	 *
	 * @param regionCode 区划编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "organ", targetClass = JSONObject.class)
	List<BspOrganise> getRegionOrgenByRegionCode(String regionCode);

	/**
	 * 1.1.31 获取机构类型信息
	 *
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	List<BspOrganType> getOrganType();

	/**
	 * 1.1.38 获取部门下用户
	 *
	 * @param orgCode 机构编码
	 * @return
	 */
	@ResultMapper(messageKey = "error", dataKey = "rows", targetClass = JSONObject.class)
	JSONArray getPostUserTree(String orgCode);

}
