package com.dr.digital.changePwd.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dr.digital.bsp.config.BspConfig;
import com.dr.digital.bsp.utils.SSOAesUtil;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.organise.query.PersonQuery;
import com.dr.framework.core.organise.service.LoginService;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.inspur.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"api/ssoLogin"})
public class SsoLoginController {
    @Autowired
    BspConfig bspConfig;
    @Autowired
    OrganisePersonService organisePersonService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    LoginService loginService;

    /**
     * 根据浪潮sso token校验登录信息
     *
     * @param token
     * @return
     * @throws Exception
     */
    @PostMapping("validate")
    public ResultEntity<String> validate(String token) throws Exception {
        String decodeStr = SSOAesUtil.decodeToString(token, bspConfig.getSsoSecretKey(), bspConfig.getSsoEncoding());
        JSONObject jsonObject = JSON.parseObject(decodeStr);
        String userId = jsonObject.getString("userId");
        Assert.isTrue(StringUtils.hasText(userId), "未找到用户Id");
        Person person = organisePersonService.getPerson(new PersonQuery.Builder().idEqual(userId).build());
        Assert.isTrue(person != null, "未找到指定的用户");
        String authToken = loginService.auth(person);
        //TODO 这里应该尝试同步一下用户信息
        return ResultEntity.success(authToken);
    }

    /**
     * 获取门户访问地址
     *
     * @return
     */
    @PostMapping("getPortalUrl")
    public ResultEntity<String> getPortalUrl() {
        Map<String, String> portalAppInfo = applicationService.findAppinfoByKey(bspConfig.getPortalCode());
        if (portalAppInfo != null) {
            return ResultEntity.success(portalAppInfo.get("URL"));
        }
        return ResultEntity.error("查询失败");
    }

}
