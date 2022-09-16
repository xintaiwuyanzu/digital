package com.dr.digital.bsp.service.impl;

import com.dr.digital.bsp.bo.*;
import com.dr.digital.bsp.config.BspConfig;
import com.dr.digital.bsp.service.AbstractBspSyncService;
import com.dr.digital.bsp.service.BspSyncService;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseEntity;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.organise.entity.UserLogin;
import com.dr.framework.core.organise.query.OrganiseQuery;
import com.dr.framework.core.organise.service.LoginService;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.dr.framework.core.security.entity.Role;
import com.dr.framework.core.security.service.SecurityManager;
import com.dr.framework.sys.service.RoleService;
import com.inspur.service.ApplicationService;
import com.inspur.service.UserAuthorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 封装工具方法
 *
 * @author dr
 */
@Service
public class BspSyncServiceImpl extends AbstractBspSyncService implements BspSyncService {
    public static final Logger logger = LoggerFactory.getLogger(BspSyncService.class);
    /**
     * 登录类型是bsp
     */
    public static String LOGIN_TYPE_BSP = "bsp";
    @Autowired
    BspConfig bspConfig;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    UserAuthorityService userAuthorityService;
    @Autowired
    OrganisePersonService organisePersonService;
    @Autowired
    RoleService roleService;
    @Autowired
    SecurityManager securityManager;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    LoginService loginService;

    @Override
    @Async
    public String syncBspData() {
        Map<String, BspOrganise> regionOrgMap = new HashMap<>();
        // 先把角色信息同步过来
        List<BspRole> roles = loadBspRole();
        long roleCount = syncRole(roles);
        // 先清空缓存
        synchronized (cacheManager) {
            for (String cacheName : cacheManager.getCacheNames()) {
                cacheManager.getCache(cacheName).clear();
            }
        }

        Map<String, BspRole> roleMap = roles.stream().collect(Collectors.toMap(AbstractBspIdEntity::getId, r -> r));
        // 先获取所有的行政区划
        List<BspRegion> regions = loadBspRegions();
        for (BspRegion region : regions) {
            // 根据区划查询所有区划的组织机构
            for (BspOrganise organise : organizationService.getRegionOrgenByRegionCode(region.getCode())) {
                regionOrgMap.put(organise.getId(), organise);
            }

        }
        // key是区划编码，value是区划下的 orglist
        Map<String, List<BspOrganise>> regionOrgList = new HashMap<>();
        // 在获取所有的组织机构
        List<BspOrganise> organises = loadBspOrganise();
        for (BspOrganise organise : organises) {
            if (regionOrgMap.containsKey(organise.getId())) {
                // 只有包含的组织机构才同步
                BspOrganise bspOrganise = regionOrgMap.get(organise.getId());
                List<BspOrganise> organiseList = regionOrgList.computeIfAbsent(bspOrganise.getRegionCode(),
                        c -> new ArrayList<>());
                organiseList.add(bspOrganise);
            }
        }
        long organiseCount = 0;
        long personCount = 0;
        long fondCount = 0;

        Map<String, String> formDefinitionMap = new HashMap<>();
        // 只处理过滤后的组织机构
        for (String regionCode : regionOrgList.keySet()) {
            List<BspOrganise> bspOrganises = regionOrgList.get(regionCode);
            List<BspOrganise> guanList = bspOrganises.stream().filter(BspOrganise::isCenter)
                    .collect(Collectors.toList());
            List<BspOrganise> juList = bspOrganises.stream().filter(BspOrganise::isLaw).collect(Collectors.toList());
            if (guanList.size() == 1 && juList.size() == 1) {
                BspOrganise guan = guanList.get(0);
                BspOrganise ju = juList.get(0);
                Set<String> permissionIds = new HashSet<>();
                // 同步档案馆
                organiseCount += syncOrganise(guan, null, Constants.ORG_TYPE_DAG);
                personCount += syncPerson(guan, roleMap, Constants.ORG_TYPE_DAG);
//				fondCount += fondDataSyncService.dataSyncFondByOrgCode(guan.getCode(), formDefinitionMap,
//						permissionIds);
                // 同步档案局
                organiseCount += syncOrganise(ju, guan, Constants.ORG_TYPE_DAJ);
                personCount += syncPerson(ju, roleMap, Constants.ORG_TYPE_DAJ);
//				fondCount += fondDataSyncService.dataSyncFondByOrgCode(ju.getCode(), formDefinitionMap, permissionIds);
                for (BspOrganise bspOrganise : bspOrganises) {
                    if (bspOrganise != guan && bspOrganise != ju) {
                        // 同步档案室
                        organiseCount += syncOrganise(bspOrganise, guan, Constants.ORG_TYPE_DAS);
                        personCount += syncPerson(bspOrganise, roleMap, Constants.ORG_TYPE_DAS);
//						fondCount += fondDataSyncService.dataSyncFondByOrgCode(bspOrganise.getCode(), formDefinitionMap,
//								permissionIds);
                    }
                }
                if (!permissionIds.isEmpty()) {
                    String[] permissionArr = permissionIds.toArray(new String[0]);
                    // 绑定档案馆所有档案室的权限给档案馆所有人员
                    securityManager.addPermissionToRole(guan.getId() + "_data", permissionArr);
                }
            } else {
                logger.warn("行政区划{}下档案馆机构数量为{}，档案局机构数量为{}，忽略处理", regionCode, guanList.size(), juList.size());
            }
        }
        // 在同步组织机构的用户
        // 最后同步用户角色
        synchronized (cacheManager) {
            for (String cacheName : cacheManager.getCacheNames()) {
                cacheManager.getCache(cacheName).clear();
            }
        }
        return String.format("同步机构数量%s,人员数量%s,角色数量%s,全宗门类数量%s", organiseCount, personCount, roleCount, fondCount);
    }

    /**
     * 同步角色信息
     *
     * @param roles
     * @return
     */
    private long syncRole(List<BspRole> roles) {
        long result = 0;
        for (BspRole role : roles) {
            Role sysRole = newRole(role);
            Role oldRole = roleService.selectById(sysRole.getId());
            if (oldRole == null) {
                CommonService.bindCreateInfo(sysRole);
                result += roleService.insert(sysRole);
            } else {
                sysRole.setCode(oldRole.getCode());
                sysRole.setStatus(oldRole.getStatus());
                if (roleChange(sysRole, oldRole)) {
                    // 更新逻辑
                    result += roleService.updateById(sysRole);
                }
            }
        }
        return result;
    }

    /**
     * 同步组织机构
     *
     * @param bspOrganise
     * @param parent
     * @param organiseType
     * @return
     */
    private long syncOrganise(BspOrganise bspOrganise, BspOrganise parent, String organiseType) {
        Organise oldOrganise = organisePersonService
                .getOrganise(new OrganiseQuery.Builder().idEqual(bspOrganise.getId()).build());
        Organise newOrganise = newOrganise(bspOrganise, organiseType, parent == null ? null : parent.getId());
        // 这里只处理增加逻辑
        if (oldOrganise == null) {
            // 写死机构类型
            organisePersonService.addOrganise(newOrganise);
            return 1;
        } else {
            newOrganise.setOrganiseCode(oldOrganise.getOrganiseCode());
            if (organiseChange(newOrganise, oldOrganise)) {
                // 机构已经存在 更新机构
                organisePersonService.updateOrganise(newOrganise);
                return 1;
            }
        }
        return 0;
    }

    /**
     * 同步登录人员
     *
     * @param organise
     * @param roleMap
     * @param orgType
     * @return
     */
    private long syncPerson(BspOrganise organise, Map<String, BspRole> roleMap, String orgType) {
        long result = 0;
        // 先获取该机构下能够在门户系统登录的用户
        List<BspPerson> personList = userAuthorityService.getUserByOrg(organise.getCode(), bspConfig.getPortalCode(),
                "");
        // 根据机构和机构类型创建内置角色
        List<Role> selfRoles = bindOrganiseRole(organise, orgType);
        // 上面用户信息不全，需要根据Id重新查询一遍完整信息
        for (BspPerson bspPerson : personList) {
            bspPerson = userAuthorityService.getUserInfoById(bspPerson.getId());
            Person oldPerson = organisePersonService.getPersonById(bspPerson.getId());
            Person newPerson = newPerson(bspPerson);
            if (oldPerson == null) {
                // 先绑定机构用户
                organisePersonService.addPerson(newPerson, organise.getId());
                result++;
                if (StringUtils.hasText(bspPerson.getPassword()) && StringUtils.hasText(bspPerson.getAccount())) {
                    // 在创建bsp登录用户
                    loginService.addLogin(newPerson.getId(), LOGIN_TYPE_BSP, bspPerson.getAccount(),
                            bspPerson.getPassword());
                    result++;
                }
            } else {
                newPerson.setUserCode(oldPerson.getUserCode());
                if (personChange(newPerson, oldPerson)) {
                    // 更新用户基本信息
                    organisePersonService.updatePerson(newPerson);
                    result++;
                }
                // 登录账户
                if (StringUtils.hasText(bspPerson.getPassword()) && StringUtils.hasText(bspPerson.getAccount())) {
                    // 更新用户登录信息
                    List<UserLogin> logins = loginService.userLogin(newPerson.getId());
                    UserLogin oldLogin = null;
                    Set<String> otherTypes = new HashSet<>();
                    for (UserLogin login : logins) {
                        if (login.getLoginId().equals(oldPerson.getUserCode())
                                && login.getUserType().equals(LOGIN_TYPE_BSP)) {
                            oldLogin = login;
                        } else {
                            otherTypes.add(login.getUserType());
                        }
                    }
                    if (oldLogin == null) {
                        // 之前没有创建登录用户
                        loginService.addLogin(newPerson.getId(), LOGIN_TYPE_BSP, bspPerson.getAccount(),
                                bspPerson.getPassword());
                        result++;
                    } else if (!oldLogin.getPassword().equals(bspPerson.getPassword())) {
                        // 密码修改了
                        loginService.changePassword(newPerson.getId(), bspPerson.getPassword(),
                                otherTypes.toArray(new String[0]));
                        result++;
                    }
                }
            }
            syncPersonRole(bspPerson, roleMap);
            String[] roleIds = selfRoles.stream().map(BaseEntity::getId).toArray(String[]::new);
            // 给机构下用户绑定角色
            securityManager.addRoleToUser(bspPerson.getId(), roleIds);
        }
        return result;
    }

    private List<Role> bindOrganiseRole(BspOrganise organise, String orgType) {
        List<Role> roles = new ArrayList<>();
        roles.add(newOrganiseDataRole(organise, orgType));
        // 机构业务角色
        return roles;
    }

    /**
     * 同步用户角色
     *
     * @param bspPerson 用户对象
     * @param roleMap   角色map
     */
    private void syncPersonRole(BspPerson bspPerson, Map<String, BspRole> roleMap) {
        if (StringUtils.hasText(bspPerson.getRoleCode())) {
            Set<String> bspRoles = Arrays.stream(bspPerson.getRoleCode().split(",")).collect(Collectors.toSet());
            List<Role> savedUserRoles = securityManager.userRoles(bspPerson.getId());
            List<String> savedUserRoleIds = savedUserRoles.stream().map(BaseEntity::getId).collect(Collectors.toList());
            Set<String> toAddRoles = bspRoles.stream()
                    .filter(r -> roleMap.containsKey(r) && !savedUserRoleIds.contains(r)).collect(Collectors.toSet());
            if (!toAddRoles.isEmpty()) {
                // 添加的角色
                securityManager.addRoleToUser(bspPerson.getId(), toAddRoles.toArray(new String[0]));
            }
            Set<String> toRemoveRoles = savedUserRoleIds.stream()
                    .filter(r -> roleMap.containsKey(r) && !bspRoles.contains(r)).collect(Collectors.toSet());
            if (!toRemoveRoles.isEmpty()) {
                // 删除的角色
                securityManager.removeUserRole(bspPerson.getId(), toRemoveRoles.toArray(new String[0]));
            }
        }
    }

}