package com.dr.digital.changePwd.controller;

import com.dr.digital.changePwd.service.AdminService;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.organise.query.OrganiseQuery;
import com.dr.framework.core.organise.query.PersonQuery;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.dr.framework.core.util.Constants;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员Controller
 *
 * @author dr
 */
@RestController
@RequestMapping("/api/gestion")
public class GestionInfoController extends BaseController<Person> {
    @Autowired
    OrganisePersonService organisePersonService;
    @Autowired
    AdminService adminService;

    /**
     * 重置密码
     *
     * @param newPwd
     * @return
     */
    @PostMapping("/changePwd")
    public ResultEntity<String> changePwd(@Current Person person, HttpServletRequest request, String id, String newPwd, String operated) {
        adminService.changePwd(id, newPwd);
        ResultEntity res = ResultEntity.success();
        return res;
    }

    /**
     * 登录人所在机构人员
     *
     * @param organise
     * @return
     */
    @PostMapping("/getPersonListByorganiseId")
    public ResultEntity getPersonListByorganiseId(@Current Organise organise, @Current Person person) {
        List<Person> list = new ArrayList<>();
        if (person.getPersonType().equals("gwy")) {
            Person per = organisePersonService.getPersonById(person.getId());
            list.add(per);
        } else {
            list = organisePersonService.getOrganiseDefaultPersons(organise.getId());
        }
        return ResultEntity.success(list);
    }

    @PostMapping("/getPersonListByorganiseId2")
    public ResultEntity getPersonListByorganiseId2(String organiseId) {
        List<Person> list = organisePersonService.getOrganiseDefaultPersons(organiseId);
        return ResultEntity.success(list);
    }

    /**
     * 当前登录人
     *
     * @param person
     * @return
     */
    @PostMapping("/getPersonById")
    public ResultEntity getPersonById(@Current Person person) {
        Person per = organisePersonService.getPersonById(person.getId());
        return ResultEntity.success(per);
    }

    /**
     * 登录机构
     *
     * @param organise
     * @return
     */
    @PostMapping("/getLoginOrganise")
    public ResultEntity getLoginOrganise(@Current Organise organise) {
        return ResultEntity.success(organise);
    }

    @PostMapping("/getPersonOne")
    public ResultEntity getPersonOne(@Current Person person) {
        Person person1 = organisePersonService.getPerson(new PersonQuery.Builder().idEqual(person.getId()).build());
        return ResultEntity.success(person1);
    }

    @RequestMapping("/organiseList")
    public ResultEntity organiseList(boolean all,
                                     @RequestParam(defaultValue = Constants.DEFAULT) String groupId,
                                     @RequestParam(defaultValue = Organise.DEFAULT_ROOT_ID) String parentId
    ) {
        OrganiseQuery.Builder builder = new OrganiseQuery.Builder()
                .parentIdEqual(parentId)
                .groupIdEqual(groupId);
        if (!all) {
            builder.statusEqual(Organise.STATUS_ENABLE_STR);
        }
        List<Organise> organises = organisePersonService.getOrganiseList(builder.build());
        return ResultEntity.success(organises);
    }

}
