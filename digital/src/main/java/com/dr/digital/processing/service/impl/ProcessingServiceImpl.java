package com.dr.digital.processing.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.processing.service.ProcessingService;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.util.FileUtil;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ProcessingServiceImpl implements ProcessingService {
    @Value("${filePath}")
    private String filePath;
    @Autowired
    FormDataService formDataService;

    /**
     * 查询案卷下所有的图片信息
     *
     * @param id
     * @param type
     * @return
     */
    @Override
    public List<ImgVo> findImgList(String formDefinitionId, String id, String type) {
        Assert.isTrue(!StringUtils.isEmpty(id), "id不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(type), "档案类型不能为空！");
        FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
        File file = new File(String.join(File.separator, filePath, "filePath", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
        if (file.exists()) {
            String[] fileList = file.list();
            if (fileList.length == 0) {
                file = new File(String.join(File.separator, filePath, "split", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
            }
        } else {
            file.mkdirs();
        }
        List<ImgVo> imgVoList = isDirectory(file, formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), type);
        if (imgVoList.size() > 0) {
            return imgVoList;
        } else {
            List<ImgVo> isDirectory2Split = isDirectoryBox(file, formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), new ArrayList<>(), "filePath");
            return isDirectory2Split;
        }
    }

    /**
     * 查询卷内图片信息
     *
     * @param id
     * @return
     */
    @Override
    public List<ImgVo> findImgPageByVId(String formDefinitionId, String id, String type) {
        Assert.isTrue(!StringUtils.isEmpty(id), "id不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(type), "档案类型不能为空！");
        FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
        File file = new File(String.join(File.separator, filePath, "filePath", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
        if (!file.exists()) {
            file.mkdirs();
        }
        List<ImgVo> isDirectory2Split = isDirectory(file, formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), type);
        return isDirectory2Split;
    }

    /**
     * 案卷查询图片
     *
     * @param file
     * @param fondCode
     * @param dangHao
     * @param type
     * @return
     */
    public List<ImgVo> isDirectory(File file, String fondCode, String dangHao, String type) {
        List<ImgVo> imgVoList = new ArrayList<>();
        if (file.exists()) {
            List<File> files = FileUtil.orderByName(file);
            int index = 1;
            for (File fileA : files) {
                if (fileA.isFile()) {
                    String fileName = fileA.getName().toUpperCase();
                    // 只检测图片
                    if (fileName.indexOf(".JPG") != -1 || fileName.indexOf(".PNG") != -1 || fileName.indexOf(".BMP") != -1 || fileName.indexOf(".TIF") != -1 || fileName.indexOf(".PCX") != -1 || fileName.indexOf(".JPEG") != -1) {
                        ImgVo imgVo = new ImgVo();
                        imgVo.setFileName(fileA.getName());
                        imgVo.setFileSize(FileUtil.getPrintSize(fileA.length()));
                        imgVo.setFilePath("filePath" + File.separator + fondCode + File.separator + dangHao + File.separator + fileA.getName());
                        imgVo.setIndex(index++);
                        imgVoList.add(imgVo);
                    }
                }
            }
        }
        return imgVoList;
    }

    /**
     * 件盒
     *
     * @param file
     * @param fondCode
     * @param dangHao
     * @param path
     * @return
     */
    public List<ImgVo> isDirectoryBox(File file, String fondCode, String dangHao, List<ImgVo> imgVoList, String path) {
        if (file.exists()) {
            if (file.isFile()) {
                String fileName = file.getName().toUpperCase();
                // 只检测图片
                if (fileName.indexOf(".JPG") != -1 || fileName.indexOf(".PNG") != -1 || fileName.indexOf(".BMP") != -1 || fileName.indexOf(".TIF") != -1 || fileName.indexOf(".PCX") != -1 || fileName.indexOf(".JPEG") != -1) {
                    ImgVo imgVo = new ImgVo();
                    imgVo.setFileName(file.getName());
                    String[] split = file.getAbsolutePath().split("\\\\");
                    String YH = split[split.length - 2];
                    imgVo.setFileSize(FileUtil.getPrintSize(file.length()));
                    imgVo.setFilePath(path + File.separator + fondCode + File.separator + dangHao + File.separator + YH + File.separator + file.getName());
                    imgVoList.add(imgVo);
                }
            } else {
                File[] list = file.listFiles();
                if (list.length != 0) {
                    for (int i = 0; i < list.length; i++) {
                        isDirectoryBox(list[i], fondCode, dangHao, imgVoList, path);
                    }
                }
            }
        } else {
            System.out.println(filePath + File.separator + "split" + File.separator + "文件也不存在！");
        }
        return imgVoList;
    }

    /**
     * 展示卷内图片
     *
     * @param ajFormId
     * @param ajId
     * @param formId
     * @param Id
     * @param type
     * @return
     */
    @Override
    public Object findFileVolumesImg(String ajFormId, String ajId, String formId, String Id, String type) {
        Assert.isTrue(!StringUtils.isEmpty(ajFormId), "档案定义Id不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(ajId), "档案主键不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(formId), "卷内定义Id不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(Id), "卷内主键不能为空！");
        //这里主要是获取案卷表里的目录页数
        FormData formData = formDataService.selectOneFormData(ajFormId, ajId);
        int CatalogNum = 0;
        if (!StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_CATALOG_NUM))) {
            CatalogNum = Integer.parseInt(formData.get(ArchiveEntity.COLUMN_CATALOG_NUM));
        }
        //获取卷内这条数据的页数和页号用于展示原文数据
        FormData jnFormData = formDataService.selectOneFormData(formId, Id);
        int start = Integer.parseInt(StringUtils.isEmpty(jnFormData.get(ArchiveEntity.COLUMN_YH)) ? "0" : jnFormData.get(ArchiveEntity.COLUMN_YH));
        int yeShu = Integer.parseInt(StringUtils.isEmpty(jnFormData.get(ArchiveEntity.COLUMN_YS)) ? "0" : jnFormData.get(ArchiveEntity.COLUMN_YS));
        if (Integer.parseInt(jnFormData.get(ArchiveEntity.COLUMN_YH)) == 1) {
            yeShu += CatalogNum;
        } else {
            start += CatalogNum;
        }
        //查询案卷信息内的全部原文数据
        List<ImgVo> imgList = findImgList(ajFormId, ajId, type);
        Object[] objects = null;
        if (start <= imgList.size()) {
            if (start == 0 || yeShu == 0) {
                objects = Arrays.copyOfRange(imgList.toArray(), 0, yeShu);
            } else {
                objects = Arrays.copyOfRange(imgList.toArray(), start - 1, start - 1 + yeShu);
            }
        }
        if (objects != null) {
            List<Object> objects1 = new ArrayList<>();
            for (Object object : objects) {
                if (object != null) {
                    objects1.add(object);
                }
            }
            return objects1;
        } else {
            return objects;
        }
    }

    @Override
    public ImgVo findTxtByArchiveId(String path, String archiveCode, String fileName) {
        ImgVo imgVo = new ImgVo();
        String realPath = "";
        String fondCode = archiveCode.substring(0, archiveCode.indexOf("-"));
        String jpgPath = String.join(File.separator, filePath, "filePath", fondCode, archiveCode);
        File file = new File(jpgPath);
        //存在数据
        if (file.listFiles().length > 0) {
            //对文件进行排序操作
            List<File> orderFiles = FileUtil.orderByName(file);
            for (File txtFile : orderFiles) {
                if (txtFile.getName().contains("_")) {
                    String jpgName = txtFile.getName();
                    if (fileName.contains(jpgName.substring(0, jpgName.indexOf("_")))) {
                        String jpgPathL = String.join(File.separator, jpgPath, jpgName);
                        realPath = jpgPathL.replace("filePath", "txt").replace("jpg", "txt");
                        imgVo.setFilePath(jpgPathL);
                        break;
                    }
                } else {
                    realPath = path.replace("filePath", "txt").replace("jpg", "txt");
                    imgVo.setFilePath(path);
                    break;
                }
            }
        }


        List TxtContent = readTxtFile(realPath);
        imgVo.setListContent(TxtContent);
        return imgVo;
    }

    public List readTxtFile(String realPath) {
        List<String> listContent = new ArrayList<>();
        StringBuffer txtContent = new StringBuffer();
        File file = new File(realPath);
        if (file.isFile() && file.exists()) {
            try {
                String s = "";
                FileInputStream inputStream = new FileInputStream(file);
                InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(in);
                StringBuffer content = new StringBuffer();
                while ((s = br.readLine()) != null) {
                    content = content.append(s);
                }
                Map page = (Map) JSONObject.parseObject(content.toString(), Map.class);
                String enter = "\r\n";
                List<Map> list = (List<Map>) page.get("items");
                for (Map map : list) {
                    //txtContent.append(map.get("content"));
                    listContent.add(map.get("content") + "");
                }
                inputStream.close();
                in.close();
            } catch (Exception e) {
                txtContent.append("");
            }
        } else {
            return null;
        }
        return listContent;
    }

}
