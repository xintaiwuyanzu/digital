package com.dr.digital.changePwd.service.impl;

import com.dr.digital.changePwd.service.AdminService;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.organise.query.OrganiseQuery;
import com.dr.framework.core.organise.query.PersonQuery;
import com.dr.framework.core.organise.service.LoginService;
import com.dr.framework.core.organise.service.OrganisePersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 管理员管理
 *
 * @author dr
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    LoginService loginService;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    OrganisePersonService organisePersonService;

    /**
     * 管理员修改密码
     */
    @Override
    public void changePassword(String personId, String oldPwd, String newPwd) {
        Assert.isTrue(!StringUtils.isEmpty(personId), "管理员记录号不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(oldPwd), "管理员密码不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(newPwd), "新密码不能为空！");
        Person person = organisePersonService.getPerson(new PersonQuery.Builder().idEqual(personId).build());
        Assert.notNull(person, "该管理员不存在");
        loginService.login(person.getUserCode(), oldPwd);
        loginService.changePassword(personId, newPwd);
    }

    /**
     * 重置密码
     */
    @Override
    public void changePwd(String personId, String newPwd) {
        Charset charset = StandardCharsets.UTF_8;
        newPwd = Base64Utils.encodeToString(
                //1、拼接密码和加密盐
                (newPwd.getBytes(charset))
        );
        Assert.isTrue(!StringUtils.isEmpty(newPwd), "新密码不能为空！");
        Person person = organisePersonService.getPerson(new PersonQuery.Builder().idEqual(personId).build());
        loginService.changePassword(personId, newPwd);
    }

    /**
     * 查询管理员
     *
     * @param admincode
     * @return
     */
    @Override
    public Person getAdminByUserCode(String admincode) {
        return organisePersonService.getPerson(new PersonQuery.Builder().
                userCodeLike(admincode).
                build()
        );
    }

    /**
     * 根据机构id获取默认人员
     *
     * @param sysId
     * @return
     */
    @Override
    public List<Person> getPersonByLibId(String sysId) {
        return organisePersonService.getOrganiseDefaultPersons(sysId);
    }

    /**
     * 新增管理员
     *
     * @param personId 创建人id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(String personId, Person admin, String password, String libId) {
        admin.setId(UUIDUtils.getUUID());
        admin.setCreateDate(System.currentTimeMillis());
        admin.setCreatePerson(personId);
        Charset charset = StandardCharsets.UTF_8;
        password = Base64Utils.encodeToString(
                //1、拼接密码和加密盐
                (password.getBytes(charset))
        );
        organisePersonService.addPerson(admin, libId, true, password);
    }

    /**
     * 更新管理员
     * TODO 这里的userCode不能修改
     *
     * @param personId
     * @param admin
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdmin(String personId, Person admin) {
        organisePersonService.updatePerson(admin);
        admin.setUpdatePerson(personId);
        admin.setUpdateDate(System.currentTimeMillis());
        commonMapper.updateIgnoreNullById(admin);
    }

    @Override
    public List<Person> getPersonsByOrgId(String orgId) {
        PersonQuery.Builder mzt = new PersonQuery.Builder()
                .organiseIdEqual(orgId)
                .dutyLike("mzt");
        return organisePersonService.getPersonList(mzt.build());
    }

    @Override
    public List<Person> getPersonsByPersonType(String personType, String orgId) {
        PersonQuery.Builder builder = new PersonQuery.Builder()
                .organiseIdEqual(orgId)
                .typeLike(personType);
        return organisePersonService.getPersonList(builder.build());
    }

    @Override
    public Organise getOrgById(String orgId) {
        OrganiseQuery.Builder builder = new OrganiseQuery.Builder()
                .idEqual(orgId);
        Organise organise = organisePersonService.getOrganise(builder.build());
        return organise;
    }

    @Override
    public List<Person> getPersonAll() {
        PersonQuery build = new PersonQuery.Builder().build();
        return organisePersonService.getPersonList(build);
    }

}
