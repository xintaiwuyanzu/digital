package com.dr.digital.ofd.service.impl;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.ofd.OfdConfig;
import com.dr.digital.ofd.bo.*;
import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.service.OfdClient;
import com.dr.digital.ofd.service.OfdService;
import com.dr.digital.ofd.service.TokenClient;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class OfdServiceImpl implements OfdService {
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    FormDataService formDataService;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    TokenClient tokenClient;
    @Autowired
    OfdClient ofdClient;
    @Autowired
    OfdConfig ofdConfig;
    @Value("${filePath}")
    private String filePath;

    /**
     * 只是识别 文件夹下面文件转换成
     * 单层的ofd 文件
     *
     * @return
     */
    public String edcToOfd() {
        long timestamp = System.currentTimeMillis();
        TokenInfo tokenInfo = new TokenInfo(timestamp, ofdConfig.getClientId(), ofdConfig.getClientSecret(), ofdConfig.getApiServerName());
        TokenResult tokenResult = tokenClient.token(tokenInfo);
        FileByteInfo fileByteInfo = new FileByteInfo(UUIDUtils.getUUID(), ofdConfig.getSrcPath(), ofdConfig.getTargetPath(), tokenResult.getData().getAuthToken());
        FileStreamResult fileStreamResult = ofdClient.turnOFD(fileByteInfo);
        return fileStreamResult.getMsg();
    }

    @Override
    public boolean pdfToOfd(FormData formData, String childFormId, String registerId, Person person) {
        //如果有子类说明是案卷类档案 需要根据件号进行查询
        if (!StringUtils.isEmpty(childFormId)) {
            List<FormData> formList = formDataService.selectFormData(childFormId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            if (formList.size() > 0) {
                for (FormData volumesList : formList) {
                    String pdfName = "正文";
                    if (!"".equals(volumesList.get(ArchiveEntity.COLUMN_FILE_TYPE))) {
                        pdfName = volumesList.get(ArchiveEntity.COLUMN_FILE_TYPE);
                    }
                    String path = filePath + File.separator + "pdf" + File.separator + volumesList.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + volumesList.get(ArchiveEntity.COLUMN_AJDH) + File.separator + pdfName + File.separator + volumesList.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".pdf";
                    File fileVolumes = new File(path);
                    //判读文件是否存在，存在将转换，不存在就不做处理
                    if (fileVolumes.exists()) {
                        //双层pdf转换为双层的ofp，现在是单层的pdf转换成双层的ofd
                        long timestamp = System.currentTimeMillis();
                        TokenInfo tokenInfo = new TokenInfo(timestamp, ofdConfig.getClientId(), ofdConfig.getClientSecret(), ofdConfig.getApiServerName());
                        TokenResult tokenResult = tokenClient.token(tokenInfo);
                        FileByteInfo fileByteInfo = new FileByteInfo(UUIDUtils.getUUID(), ofdConfig.getSrcPath(), ofdConfig.getTargetPath(), tokenResult.getData().getAuthToken());
                        FileStreamResult fileStreamResult = ofdClient.turnOFD(fileByteInfo);
                        //增加ofd转换记录
                        insetOfdRecord(volumesList.get(ArchiveEntity.COLUMN_AJDH), fileStreamResult.getCode(), fileStreamResult.getMsg(), fileStreamResult.getData(), volumesList.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".ofd", ofdConfig.getTargetPath());
                        //完成之后 更新这条数据的转换状态
                        if ("0".equals(fileStreamResult.getCode())) {
                            formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                            formDataService.updateFormDataById(formData);
                            volumesList.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                            formDataService.updateFormDataById(volumesList);
                        }
                    }
                }
            }
        } else {
            String outOfd = filePath + File.separator + "pdf" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".pdf";
            File fileVolumes = new File(outOfd);
            //判读文件是否存在，存在将转换，不存在就不做处理
            if (fileVolumes.exists()) {
                long timestamp = System.currentTimeMillis();
                TokenInfo tokenInfo = new TokenInfo(timestamp, ofdConfig.getClientId(), ofdConfig.getClientSecret(), ofdConfig.getApiServerName());
                TokenResult tokenResult = tokenClient.token(tokenInfo);
                FileByteInfo fileByteInfo = new FileByteInfo(UUIDUtils.getUUID(), outOfd, ofdConfig.getTargetPath() + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".ofd", tokenResult.getData().getAuthToken());
                FileStreamResult fileStreamResult = ofdClient.turnOFD(fileByteInfo);
                //增加ofd转换记录
                insetOfdRecord(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), fileStreamResult.getCode(), fileStreamResult.getMsg(), fileStreamResult.getData(), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".ofd", outOfd);
                //完成之后 更新这条数据的转换状态
                if ("0".equals(fileStreamResult.getCode())) {
                    formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                    formDataService.updateFormDataById(formData);
                }
            }
        }
        return false;
    }

    /**
     * 增加ofd转换记录
     *
     * @param dirName
     * @param code
     * @param message
     * @param data
     * @param fileName
     * @param path
     */
    public void insetOfdRecord(String dirName, String code, String message, String data, String fileName, String path) {
        OfdRecord ofdRecord = new OfdRecord();
        ofdRecord.setId(UUIDUtils.getUUID());
        ofdRecord.setCreateDate(System.currentTimeMillis());
        ofdRecord.setArchiveCode(dirName);
        ofdRecord.setCode(code);
        ofdRecord.setMessage(message);
//        ofdRecord.setFhData(data);
        ofdRecord.setFileName(fileName);
        ofdRecord.setFilePath(path);
        commonMapper.insert(ofdRecord);
    }

    /**
     * 将前端上传的文件上传到指定文件夹中  修改 {pdfOfd}，更改文件上传位置
     *
     * @param multipartFile
     * @return 成功返回==文件生成路径及文件名，失败返回 == ""
     */
    @Override
    public String upload(MultipartFile multipartFile) {
        String pdfName = "";
        if (!multipartFile.isEmpty()) {
            String fileName = multipartFile.getOriginalFilename();
            pdfName = filePath+File.separator+"pdfOfd";
            File fileDate = new File(pdfName);
            if(!fileDate.exists()){
                fileDate.mkdirs();
            }
            pdfName = pdfName + File.separator + fileName;
            File file = new File(pdfName);
            try {
                //上传的文件需要保存的路径和文件名称，路径需要存在，否则报错
                multipartFile.transferTo(file);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
        return pdfName;
    }

    /**
     * 识别pdf档号名字是否一致
     *
     * @param query
     * @param path
     * @return 返回识别状态，成功ture 失败false
     */
    public boolean pdfmatching(BaseQuery query, String path) {
        if ("".equals(path)) {
            return false;
        }
        boolean check = false;
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile()) {
                String typeName = file.getName().split("\\.")[1];
                if ("pdf".equals(typeName.toLowerCase())) {
                    String fileNames = file.getName().split("\\.")[0];
                    List<FormData> formDataList = formDataService.selectFormData(query.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                        sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), fileNames)
                                .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                                .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                                .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                    });
                    if (formDataList.size() > 0) {
                        check = true;
                    }
                }
            }
        }
        return check;
    }
    @Override
    public void ofdPath(String ofdPath, HttpServletResponse response) {
        try {
            // path： 欲下载的文件的路径
            File file = new File(ofdPath);
            // 获取文件名 - 设置字符集
            String downloadFileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), "iso-8859-1");
            // 以流的形式下载文件
            InputStream fis;
            fis = new BufferedInputStream(new FileInputStream(ofdPath));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + downloadFileName);
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * 转换为ofd
     *
     * @param check 是否识别的状态
     * @param path  文件上传的地址
     * @return 成功返回转换后的地址，失败“”
     */
    @Override
    public String pdfConvertOfd(boolean check, String path) {
        if (!check) {
            return "";
        }
        File file = new File(path);
        String fileNames = file.getName().split("\\.")[0];
        long timestamp = System.currentTimeMillis();
        String outOfdPath = ofdConfig.getTargetPath() + File.separator + fileNames + ".ofd";
        String out ="D:\\shuzihua\\pdfOfd";
        String outOfdPaths = file.getName().substring(0,file.getName().lastIndexOf("."))+File.separator + fileNames + ".ofd";
        PdfOfdDetailed pdfOfdDetailed = new PdfOfdDetailed(fileNames,fileNames,"pdf",1,path);
        List<PdfOfdDetailed> list = new ArrayList<>();
        list.add(pdfOfdDetailed);
        TokenInfo tokenInfo = new TokenInfo(timestamp, ofdConfig.getClientId(), ofdConfig.getClientSecret(), ofdConfig.getApiServerName());
        TokenResult tokenResult = tokenClient.token(tokenInfo);
        FileByteInfo fileByteInfo = new FileByteInfo(UUIDUtils.getUUID(), out, out, tokenResult.getData().getAuthToken());
        FileStreamResult fileStreamResult = ofdClient.turnOFD(fileByteInfo);
        out = out+File.separator + fileNames + ".ofd";
        return out;
    }



}
