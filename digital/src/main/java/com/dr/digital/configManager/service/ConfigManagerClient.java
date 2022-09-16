package com.dr.digital.configManager.service;

import com.dr.digital.configManager.bo.*;
import com.dr.framework.rpc.ResultMapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ResultMapper
@FeignClient(url = "${znGdUrl}", name = "configManager")
public interface ConfigManagerClient {
    /**
     * 获取元数据方案
     *
     * @param code 选填 门类编码
     * @return
     */
    @GetMapping("metadata/getArchiveTypeSchema")
    List<Metadata> getArchiveTypeSchema(@RequestParam(value = "code",required = false) String code);
    /**
     * 根据组织机构获取全宗
     *
     * @param orgCode
     * @return
     */
    @GetMapping("/metadata/generalArchive")
    List<FondInfo> getMetadataConfig(@RequestParam("orgCode") String orgCode);

    /**
     * 获取所有可用的门类信息
     *
     * @return
     */
    @GetMapping("/archiveType/getArchiveType")
    List<CategoryInfo> getCategoryInfo();

    /**
     * 根据门类Id查询门类所有年度配置
     *
     * @param code
     * @return
     */
    @GetMapping("bsp/getDict")
    Map getArchiveBspDict(@RequestParam("code") String code);

    /**
     * 获取元数据字段
     *
     * @param
     * @return
     */
    @GetMapping("metadata/getMetadata")
    List<MataDataInfo> getCategoryMetadata(@RequestParam("code") String code, @RequestParam("classify") String classify, @RequestParam("arrange") String arrange, @RequestParam("id") String id);
    @GetMapping("archivedStandard/queryData")
    List<QueryData> getQueryData();

    /**
     * 获取元数据字段
     * @param id 智能归档2.1获取该id
     * @return
     */
    @GetMapping("metadata/getMetadata")
    List<MataDataInfo> getCategoryMetadata(@RequestParam("id") String id);

    /*
     * 获取方案元数据四性检测规则
     * (有条件的检测规则从这取,例 检测作者字段最大长度为10,条件 年度等于2019年)
     *
     * @param code 门类编码
     * @param classify 载体类型 1电子 2纸质化副本
     * @param arrange 1案件 2案卷
     * */
    @GetMapping("/metadata/metadataRuleTest")
    List<MetadataRuleTest> getMetadataRuleTest(@RequestParam("code") String code, @RequestParam("classify") String classify, @RequestParam("arrange") String arrange);

    /**
     * 查询包结构信息
     *
     * @param code
     * @param classify
     * @return
     */
    @GetMapping("archivedTypeFile/getArchivedTypeFile")
    ArrayList<ArchivedTypeFile> getArchivedTypeFile(@RequestParam("id")String id,
                                                    @RequestParam("code") String code,
                                                    @RequestParam("classify") String classify,
                                                    @RequestParam("arrange") String arrange);

}
