package com.dr.digital.ofd.controller;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.ofd.service.OfdService;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/ofd")
public class OfdController {
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    FormDataService formDataService;
    @Autowired
    OfdService ofdService;
    @Value("${ofd.ip}")
    private String ofdIp;
    @Value("${scan}")
    private String bjIp;

    /**
     * 只是识别 文件夹下面文件转换成
     * 单层的ofd 文件
     *
     * @return
     */
    @RequestMapping(value = "/edcChange")
    public ResultEntity edcChange() {
        return ResultEntity.success(ofdService.edcToOfd());
    }

    /**
     * 下载
     *
     * @param ofdPath
     * @param response
     */
    @RequestMapping("ofdDownload")
    public void ofdDownload(String ofdPath, HttpServletResponse response) {
        ofdService.ofdPath(ofdPath, response);
    }

    @RequestMapping("pdfConvertOfd")
    public ResultEntity pdfConvertOfd(BaseQuery query,
                                      MultipartFile file) {
        //上传
        String path = ofdService.upload(file);
        //转换
        String ofdPath = ofdService.pdfConvertOfd(true, path);
        return ResultEntity.success(ofdPath);
    }

    /**
     * pdf 转换为 ofd
     *
     * @param request
     * @param query
     * @param type
     * @param childFormId
     * @param formDefinitionId
     * @param registerId
     * @param queryContent
     * @param person
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/turnChange")
    public ResultEntity turnChange(HttpServletRequest request,
                                   BaseQuery query,
                                   String type,
                                   String childFormId,
                                   String formDefinitionId,
                                   String registerId,
                                   @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                   @Current Person person) throws Exception {
        query.parseQuery(queryContent);
        long success = 0;
        long error = 0;
        List<FormData> dataList = dataManager.findDataByQuery(query);
        for (FormData formDataOne : dataList) {
            FormData formData = formDataService.selectOneFormData(formDefinitionId, formDataOne.getId());
            if (StringUtils.isEmpty(formData)) {
                return ResultEntity.error("未查到当前档案数据！");
            }
            final boolean b = ofdService.pdfToOfd(formData, childFormId, registerId, person);
            if (b) {
                success++;
            } else {
                error++;
            }
        }
        return ResultEntity.success(success + "条数据转换成功," + error + "条数据转换失败");
    }

    /**
     * 根据前台传过来的参数去找对应文件，找到后加密成云阅读链接，找不到则报异常
     *
     * @param fondCode
     * @param ajDh
     * @param fileType
     * @param archiveCode
     * @return
     */
    @RequestMapping("/getOfd")
    public ResultEntity getOfd(String fondCode, String ajDh, String fileType, String archiveCode) {
        String fileUrl = bjIp + String.join("/", "ofdPath", fondCode, ajDh, fileType, archiveCode + ".ofd");
        /* 云阅读根页面地址 */
        String ofdServerUrl = "http://" + String.join("/", ofdIp, "viewer", "pc", "index.html") + "?docuri=" + fileUrl;
        return ResultEntity.success(ofdServerUrl);
    }

}
