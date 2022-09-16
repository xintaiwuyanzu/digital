package com.dr.digital.bsp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.dr.digital.bsp.bo.BspOrganise;
import com.dr.digital.bsp.bo.BspPerson;
import com.dr.digital.bsp.bo.BspRegion;
import com.dr.digital.bsp.bo.BspRole;
import com.dr.digital.bsp.config.BspConfig;
import com.dr.digital.util.CompareUtil;
import com.dr.digital.util.Constants;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.entity.Permission;
import com.dr.framework.core.security.entity.Role;
import com.dr.framework.core.security.service.SecurityManager;
import com.dr.framework.sys.service.PermissionService;
import com.dr.framework.sys.service.RoleService;
import com.inspur.service.ApplicationService;
import com.inspur.service.OrganizationService;
import com.inspur.service.UserAuthorityService;

/**
 * 封装工具方法
 *
 * @author dr
 */
public class AbstractBspSyncService {
    /**
     * 登录类型是bsp
     */
    public static String LOGIN_TYPE_BSP = "bsp";

    @Autowired
    BspConfig bspConfig;
    @Autowired
    UserAuthorityService userAuthorityService;
    @Autowired
    protected OrganizationService organizationService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    SecurityManager securityManager;
    @Autowired
    RoleService roleService;
    @Autowired
    PermissionService permissionService;

    /**
     * 获取bsp上智能归档配置系统配置的组织机构树
     *
     * @return
     */
    protected List<BspOrganise> loadBspOrganise() {
        return organizationService.getOrganInfo(bspConfig.getConfigSysCode());
    }

    /**
     * 加载bsp配置的角色信息
     *
     * @return
     */
    protected List<BspRole> loadBspRole() {
        return applicationService.getAppRoleTree(bspConfig.getDagCode()).stream()
                .map(r -> userAuthorityService.getRoleInfoById(r.getId())).collect(Collectors.toList());
    }

    /**
     * 获取bsp福州市所有区划
     *
     * @return
     */
    protected List<BspRegion> loadBspRegions() {
        List<BspRegion> rootRegion = organizationService.getRegionInfoByRegionCode(bspConfig.getRootRegionCode());
        List<BspRegion> childRegion = organizationService.getChildRegionByRegionCode(bspConfig.getRootRegionCode());
        childRegion.addAll(rootRegion);
        return childRegion;
    }

    /**
     * 根据bsp role创建系统角色
     *
     * @param role
     * @return
     */
    protected Role newRole(BspRole role) {
        Role sysRole = new Role();
        sysRole.setId(role.getId());
        sysRole.setName(role.getName());
        sysRole.setDescription(role.getRemark());
        // TODO 这里的类型需要映射
        sysRole.setType(role.getType());
        sysRole.setCode(role.getValue());
        sysRole.setOrder(role.getOrder());
        return sysRole;
    }

    /**
     * 根据机构创建数据权限
     *
     * @param organise
     * @param orgType
     * @return
     */
    protected Role newOrganiseDataRole(BspOrganise organise, String orgType) {
        // 机构数据角色
        String dataRoleId = organise.getId() + "_data";
        String dataRoleCode = "ARCHIVE_" + organise.getCode() + "_DATA";
        String roleName = "档案_" + organise.getName() + "_数据角色";
        String roleDescription = roleName + "【自动生成】";
        boolean isNew = false;
        Role role = roleService.selectById(dataRoleId);
        if (role == null) {
            // 没创建过角色
            isNew = true;
            role = new Role();
            role.setId(dataRoleId);
        }
        role.setOrder(organise.getOrder());
        role.setName(roleName);
        role.setDescription(roleDescription);
        role.setCode(dataRoleCode);
        if (isNew) {
            roleService.insert(role);
        } else {
            roleService.updateById(role);
        }
        String permissionName = Constants.ORG_TYPE_DAG.equalsIgnoreCase(orgType) ? "档案馆基础菜单" : "档案室基础菜单";
        Set<String> permissionIds = new HashSet<>();

        SqlQuery<Permission> permissionSqlQuery = SqlQuery.from(permissionService.getEntityRelation());
        permissionSqlQuery.equal(permissionService.getEntityRelation().getColumn("name"), permissionName);
        // 绑定基础菜单权限
        Permission basicMenuPermission = permissionService.selectOne(permissionSqlQuery);
        if (basicMenuPermission != null) {
            permissionIds.add(basicMenuPermission.getId());
        }
        // 绑定子系统权限
        permissionIds.add("subsys");
        securityManager.addPermissionToRole(role.getId(), permissionIds.toArray(new String[0]));
        return role;
    }

    protected Organise newOrganise(BspOrganise bspOrganise, String orgType, String parentId) {
        Organise organise = new Organise();
        organise.setId(bspOrganise.getId());

        String organiseName = bspOrganise.getName();
        if (bspOrganise.isRisk()) {
            organiseName = organiseName + "(已撤销)";
        }
        organise.setOrganiseName(organiseName);
        organise.setOrganiseOldName(bspOrganise.getShortName());
        organise.setOrganiseCode(bspOrganise.getCode());
        organise.setAddress(bspOrganise.getAddress());
        organise.setPhone(bspOrganise.getPhone());
        organise.setParentId(parentId);

        organise.setSummary(bspOrganise.getRemark());
        organise.setSourceRef(LOGIN_TYPE_BSP);
        organise.setOrganiseType(orgType);
        organise.setOrder(bspOrganise.getOrder());
        return organise;
    }

    /**
     * 根据bsp用户创建系统用户
     *
     * @param bspPerson
     * @return
     */
    protected Person newPerson(BspPerson bspPerson) {
        Person person = new Person();
        person.setId(bspPerson.getId());
        person.setUserName(bspPerson.getName());
        person.setIdNo(bspPerson.getIdentityNum());
        // TODO 这个字段有问题
        // person.setBirthday(bspPerson.getBirthday());
        person.setEmail(bspPerson.getEmail());
        person.setMobile(bspPerson.getMobile());
        person.setPhone(bspPerson.getPhone());
        person.setSourceRef(LOGIN_TYPE_BSP);
        person.setOrder(bspPerson.getOrder());
        person.setSex("0".equals(bspPerson.getGender()) ? 1 : 0);
        person.setUserCode(bspPerson.getAccount());
        return person;
    }

    protected boolean roleChange(Role source, Role target) {
        return CompareUtil.compare(source, target, Role::getName, Role::getOrder, Role::getDescription, Role::getType);
    }

    protected boolean organiseChange(Organise source, Organise target) {
        return CompareUtil.compare(source, target, Organise::getId, Organise::getOrganiseName,
                Organise::getOrganiseOldName, Organise::getAddress, Organise::getPhone, Organise::getParentId,
                Organise::getSummary, Organise::getOrganiseType, Organise::getOrder);
    }

    protected boolean personChange(Person source, Person target) {
        return CompareUtil.compare(source, target, Person::getOrder, Person::getUserName, Person::getIdNo,
                Person::getEmail, Person::getMobile, Person::getSex, Person::getUserCode);
    }

}
