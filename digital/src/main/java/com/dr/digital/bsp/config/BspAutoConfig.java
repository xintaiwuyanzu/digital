package com.dr.digital.bsp.config;

import com.inspur.service.ApplicationService;
import com.inspur.service.OrganizationService;
import com.inspur.service.UserAuthorityService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * bsp相关自动配置
 *
 * @author dr
 */
@Configuration
@EnableConfigurationProperties(BspConfig.class)
@ConditionalOnClass(name = "org.apache.dubbo.config.spring.ReferenceBean")
public class BspAutoConfig {

    /**
     * 配置dubbo客户端基本信息
     *
     * @return
     */
    @Bean
    ApplicationConfig applicationConfig() {
        ApplicationConfig config = new ApplicationConfig();
        config.setName("digital");
        return config;
    }

    /**
     * 配置bsp组织机构服务
     *
     * @param bspConfig
     * @return
     */
    @Bean
    ReferenceBean<OrganizationService> organizationServiceReferenceBean(BspConfig bspConfig) {
        ReferenceBean<OrganizationService> bean = new ReferenceBean<OrganizationService>();
        bean.setUrl("dubbo://" + bspConfig.getDubboUrl());
        bean.setId("OrganizationService");
        bean.setInterface("com.inspur.service.OrganizationService");
        bean.setTimeout(30 * 1000);
        bean.setGroup("bsp");
        bean.setLazy(true);
        return bean;
    }

    /**
     * 配置bsp应用接口服务
     *
     * @param bspConfig
     * @return
     */
    @Bean
    ReferenceBean<ApplicationService> bspApplicationClientReferenceBean(BspConfig bspConfig) {
        ReferenceBean<ApplicationService> bean = new ReferenceBean<ApplicationService>();
        bean.setUrl("dubbo://" + bspConfig.getDubboUrl());
        bean.setInterface("com.inspur.service.ApplicationService");
        bean.setId("ApplicationService");
        bean.setTimeout(30 * 1000);
        bean.setGroup("bsp");
        bean.setLazy(true);
        return bean;
    }

    @Bean
    ReferenceBean<UserAuthorityService> bspUserServiceReferenceBean(BspConfig bspConfig) {
        ReferenceBean<UserAuthorityService> bean = new ReferenceBean<UserAuthorityService>();
        bean.setUrl("dubbo://" + bspConfig.getDubboUrl());
        bean.setId("UserAuthorityService");
        bean.setInterface("com.inspur.service.UserAuthorityService");
        bean.setTimeout(30 * 1000);
        bean.setGroup("bsp");
        bean.setLazy(true);
        return bean;
    }

    /**
     * 适配浪潮密码加密策略
     *
     * @return
     */

   /* @Bean
    BspMd5PassWordEncrypt bspMd5PassWordEncrypt() {
        return new BspMd5PassWordEncrypt(StandardCharsets.UTF_8.name());
    }*/

}
