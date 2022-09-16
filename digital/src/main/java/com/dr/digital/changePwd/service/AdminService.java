package com.dr.digital.changePwd.service;


import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;

/**
 * 管理员
 *
 * @author dr
 */
public interface AdminService {

    /**
     * 管理员修改密码
     *
     * @param personId
     * @param oldPwd
     * @param newPwd
     */
    void changePassword(String personId, String oldPwd, String newPwd);

    /**
     * 重置密码
     *
     * @param personId
     * @param newPwd
     */
    void changePwd(String personId, String newPwd);

    /**
     * 查询管理员
     *
     * @param adminId
     * @return
     */
    Person getAdminByUserCode(String adminId);

    /**
     * 根据机构id获取默认人员
     *
     * @param sysId
     * @return
     */
    List<Person> getPersonByLibId(String sysId);

    /**
     * 新增管理员
     *
     * @param personId 创建人id
     * @param libId    系统id
     * @param admin
     * @return
     */
    void addAdmin(String personId, Person admin, String password, String libId);

    /**
     * 更新管理员
     *
     * @param personId
     * @param admin
     */
    void updateAdmin(String personId, Person admin);

    /**
     * 查询民政厅所有人员
     *
     * @return
     */
    List<Person> getPersonsByOrgId(String orgId);

    /**
     * 根据类型查用户
     *
     * @param personType
     * @return
     */
    List<Person> getPersonsByPersonType(String personType, String orgId);

    default List<Person> getPersonsByPersonType(String personType) {
        return getPersonsByPersonType(personType, "");
    }

    Organise getOrgById(String orgId);

    List<Person> getPersonAll();
}
