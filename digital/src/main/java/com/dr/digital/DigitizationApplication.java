package com.dr.digital;

import com.dr.digital.bsp.config.BspAutoConfig;
import com.dr.digital.configManager.service.ConfigManagerClient;
import com.dr.digital.ocr.service.OcrGeneralClient;
import com.dr.digital.ocr.service.OcrTableClient;
import com.dr.digital.ocr.service.OcrTemplateClient;
import com.dr.digital.ofd.service.OfdClient;
import com.dr.digital.ofd.service.TokenClient;
import com.dr.digital.packet.service.PacketsClient;
import com.dr.framework.core.orm.support.mybatis.spring.boot.autoconfigure.EnableAutoMapper;
import com.dr.framework.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/**
 * 项目启动类
 *
 * @author dr
 */
//spring启动入口
@SpringBootApplication(scanBasePackages = {Constants.PACKAGE_NAME, "com.dr.archive.common.dataClient"}, scanBasePackageClasses = com.dr.digital.DigitizationApplication.class)
//配置数据源，自动配置数据源，自动扫描mapper类
@EnableAsync
@EnableAutoMapper(basePackages = {Constants.PACKAGE_NAME, "com.dr.digital", "com.dr.archive"}, databases = {@EnableAutoMapper.DataBase(name = "one", primary = true, basePackages = {Constants.PACKAGE_NAME, "com.dr.digital", "com.dr.archive"})})
@EnableFeignClients(clients = {OcrTableClient.class, OcrGeneralClient.class, OcrTemplateClient.class, TokenClient.class, OfdClient.class, ConfigManagerClient.class, PacketsClient.class})
@Import(BspAutoConfig.class)
public class DigitizationApplication implements WebMvcConfigurer {
    @Value("${filePath}")
    private String filePath;

    public static void main(String[] args) {
        SpringApplication.run(DigitizationApplication.class, args);
        System.out.println("成功");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //文件分件图片映射地址
        registry.addResourceHandler("/template/**").addResourceLocations("/template/");
        registry.addResourceHandler("/filePath/**").addResourceLocations("file:" + filePath + File.separator + "filePath" + File.separator);
        registry.addResourceHandler("/pdfPath/**").addResourceLocations("file:" + filePath + File.separator + "pdf" + File.separator);
        registry.addResourceHandler("/ofdPath/**").addResourceLocations("file:" + filePath + File.separator + "ofd" + File.separator);
        registry.addResourceHandler("/download/**").addResourceLocations("file:" + filePath + File.separator + "zipPack" + File.separator);
        registry.addResourceHandler("/splitPath/**").addResourceLocations("file:" + filePath + File.separator + "splitPath" + File.separator);
        registry.addResourceHandler("/fileThumbnailPath/**").addResourceLocations("file:" + filePath + File.separator + "fileThumbnailPath" + File.separator);
    }

    /**
     * 设置文件上传大小
     *
     * @return
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //单个文件最大
        factory.setMaxFileSize(DataSize.ofMegabytes(500)); //MB
        //factory.setMaxFileSize(DataSize.ofKilobytes(80)); //KB
        //factory.setMaxFileSize(DataSize.ofGigabytes(80)); //Gb
        /// 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(500));
        return factory.createMultipartConfig();
    }

}