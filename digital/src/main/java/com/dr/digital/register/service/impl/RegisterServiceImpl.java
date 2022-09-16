package com.dr.digital.register.service.impl;

import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.configManager.service.FlowPathService;
import com.dr.digital.manage.category.entity.CategoryConfig;
import com.dr.digital.manage.category.service.CategoryConfigService;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.log.service.ArchivesLogService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.service.impl.BasePermissionResourceService;
import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.ocr.entity.OcrQueue;
import com.dr.digital.ocr.entity.OcrQueueInfo;
import com.dr.digital.packet.service.PacketsDataService;
import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.CategoryService;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.resultTest.service.ResultTestService;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.FileUtil;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.bo.PermissionResource;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RegisterServiceImpl extends BasePermissionResourceService<Register> implements RegisterService {
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    FormDataService formDataService;
    @Autowired
    ArchiveBatchService batchService;
    @Autowired
    ArchivesLogService archivesLogService;
    @Autowired
    CategoryConfigService categoryConfigService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    PacketsDataService packetsDataService;
    @Autowired
    UploadFilesService uploadFilesService;
    @Autowired
    ResultTestService resultTestService;
    @Resource
    CommonMapper commonMapper;
    @Autowired
    FlowPathService flowPathService;
    @Value("${filePath}")
    private String filePath;
    //线程池
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public List<Register> selectList(SqlQuery<Register> sqlQuery) {
        sqlQuery.orderBy(RegisterInfo.ORDERBY);
        return commonMapper.selectByQuery(sqlQuery);
    }

    @Override
    protected String getCacheName() {
        return "batch_name";
    }

    @Override
    public void updateStatus(Person person, BaseQuery query, String type) {
        List<FormData> dataList = dataManager.findDataByQuery(query);
        if (dataList.size() > 0) {
            dataList.forEach(d -> this.updateDetail(person, d, type, "", query.getFondId()));
        }
    }

    @Override
    public ResultEntity lhUpdateType(Person person, BaseQuery query, String type, String childFormId, String status) {
        List<FormData> dataList = dataManager.findDataByQuery(query);
        if (dataList.size() > 0) {
            for (FormData list : dataList) {
                //更新状态
                executorService.execute(() -> updateDataDetail(person, list, type, childFormId, query, status));
            }
            return ResultEntity.success();
        } else {
            return ResultEntity.error("该流程下没有可操作的数据");
        }
    }

    @Override
    public ResultEntity resultUpdateType(Person person, List<FormData> formDataList, BaseQuery query) {
        if (formDataList.size() > 0) {
            for (FormData list : formDataList) {
                //更新状态
                executorService.execute(() -> updateDataDetail(person, list, list.get(ArchiveEntity.COLUMN_EXIT_FLOW_PATH), "", query, ""));
            }
            return ResultEntity.success();
        } else {
            return ResultEntity.error("该流程下没有可操作的数据");
        }
    }

    @Override
    public void lhUpdateStatus(String ids, String status, String formDefinitionId, String childFormId, String registerId, Person person) {
        String[] split = ids.split(",");
        for (String id : split) {
            FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
            executorService.execute(() -> updateDetail(person, formData, status, childFormId, registerId));
        }
    }

    //旧版
    public void updateDetail(Person person, FormData formDate, String type, String childFormId, String registerId) {
        //增加日志记录
        addLongs(person, formDate, type, registerId);
        if (!"".equals(childFormId) && childFormId != null) {
            lhUpdateDetail(formDate, type, childFormId);
        }
        formDate.put(ArchiveEntity.COLUMN_STATUS, type);
        formDataService.updateFormDataById(formDate);
    }

    /**
     * 查询当前状态下的卷内信息
     *
     * @param formData
     * @param type
     * @param childFormId
     */
    public void lhUpdateDetail(FormData formData, String type, String childFormId) {
        List<FormData> formList = formDataService.selectFormData(childFormId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        if (formList.size() > 0) {
            for (FormData formDate : formList) {
                formDate.put(ArchiveEntity.COLUMN_STATUS, type);
                formDataService.updateFormDataById(formDate);
            }
        }
    }

    /**
     * 新增操作环节记录
     *
     * @param person
     * @param formDate
     * @param type
     * @param registerId
     */
    public void addLongs(Person person, FormData formDate, String type, String registerId) {
        String status = formDate.get(ArchiveEntity.COLUMN_STATUS);
        if (!StringUtils.isEmpty(status)) {
            if ("RECEIVE".equals(status) && "SCANNING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_SCANNING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增图像扫描",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("SCANNING".equals(status) && "PROCESSING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_PROCESSING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增图像修改",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("PROCESSING".equals(status) && "IMAGES".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_VOLUMES,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增图像质检",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("IMAGES".equals(status) && "WSSPLIT".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_VOLUMES,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增手动拆件",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("WSSPLIT".equals(status) && "VOLUMES".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_WSSPLIT,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增目录著录",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");

            } else if ("VOLUMES".equals(status) && "QUALITY".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_QUALITY,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增初级验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("QUALITY".equals(status) && "RECHECK".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_RECHECK,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增复检验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("RECHECK".equals(status) && "OVER".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_OVER,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增数字化成果",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("OVER".equals(status) && "RECHECK".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_RECHECK,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回复检验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("RECHECK".equals(status) && "QUALITY".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_QUALITY,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回初检验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("QUALITY".equals(status) && "VOLUMES".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_VOLUMES,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回看图著录",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("VOLUMES".equals(status) && "WSSPLIT".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_VOLUMES,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回手动拆件",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("WSSPLIT".equals(status) && "IMAGES".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_PROCESSING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回图像质检",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("IMAGES".equals(status) && "PROCESSING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_PROCESSING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回图像修改",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("PROCESSING".equals(status) && "SCANNING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_SCANNING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回图像扫描",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("SCANNING".equals(status) && "RECEIVE".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_RECEIVE,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回登记",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            }
        }
    }

    @Override
    public List<? extends PermissionResource> getResources(String groupId) {
        return selectList(SqlQuery.from(Register.class));
    }

    @Override
    public String getType() {
        return "batch_name";
    }

    @Override
    public String getName() {
        return "批次权限";
    }

    long ocrFinishTime = 2;//ocr完成时间

    @Override
    public Map getPercentag(String businessId) {
        Map map = new HashMap();
        List<Category> categoryList = categoryService.selectCategoryByBusinessId(businessId);
        if (categoryList.size() == 0) return map;
        List<CategoryConfig> categoryConfigList = categoryConfigService.selectByBusinessId(categoryList.get(0).getId());
        if (categoryConfigList.size() == 0) return map;
        //拆tif
        long jpgFinish = formDataService.countId(categoryConfigList.get(0).getFileFormId(), (sqlQueryCount, formRelationWrapper) -> {
            sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), "1");
        });
        //ocr
        long ocrFinish = formDataService.countId(categoryConfigList.get(0).getFileFormId(), (sqlQueryCount, formRelationWrapper) -> {
            sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), "1");
        });
        //split
        long splitFinish = formDataService.countId(categoryConfigList.get(0).getFileFormId(), (sqlQueryCount, formRelationWrapper) -> {
            sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISASSEMBLY_STATE), "1");
        });
        //ofd
        long ofdFinish = formDataService.countId(categoryConfigList.get(0).getFileFormId(), (sqlQueryCount, formRelationWrapper) -> {
            sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3");
        });
        //打包
        long packetFinish = formDataService.countId(categoryConfigList.get(0).getFileFormId(), (sqlQueryCount, formRelationWrapper) -> {
            sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE), "1");
        });
        //总份数
        long All = formDataService.countId(categoryConfigList.get(0).getFileFormId(), (sqlQueryCount, formRelationWrapper) -> {
        });
        //从ocr队列查还没未ocr的页数
        SqlQuery<OcrQueue> count = SqlQuery.from(OcrQueue.class).count(OcrQueueInfo.ID)
                .equal(OcrQueueInfo.FORMDEFINITIONID, categoryConfigList.get(0).getFileFormId())
                // ocr队列状态 等待0 转换中1 转换成功2 转换失败3 暂停中4
                .notEqual(OcrQueueInfo.STATUS, 2).notEqual(OcrQueueInfo.STATUS, 3).notEqual(OcrQueueInfo.STATUS, 4);

        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(count);
        long ocrPage = Long.parseLong(ocrQueues.get(0).getId());

        long jpgNeedTime = countNeedTime(All, jpgFinish, 0.6);
        long ocrNeedTime = Math.round(ocrFinishTime * (ocrPage) / 300);
        long splitNeedTime = countNeedTime(All, splitFinish, 0.5);
        long ofdNeedTime = countNeedTime(All, ofdFinish, 1.1);
        long packetNeedTime = countNeedTime(All, packetFinish, 0.5);
        map.put("jpgFinish", jpgFinish);
        map.put("ocrFinish", ocrFinish);
        map.put("splitFinish", splitFinish);
        map.put("ofdFinish", ofdFinish);
        map.put("packetFinish", packetFinish);
        map.put("jpgNeedTime", jpgNeedTime);
        map.put("ocrNeedTime", ocrNeedTime);
        map.put("splitNeedTime", splitNeedTime);
        map.put("ofdNeedTime", ofdNeedTime);
        map.put("packetNeedTime", packetNeedTime);
        map.put("all", All);
        return map;
    }

    @Override
    public Map getTotalPercentage() {
        Map map = new HashMap();
        SqlQuery<Register> registerSqlQuery = SqlQuery.from(Register.class, false).column(RegisterInfo.ID, RegisterInfo.FORMDEFINITIONID);
        List<Register> registers = commonMapper.selectByQuery(registerSqlQuery);
        long jpgFinishTotal = 0, ocrFinishTotal = 0, splitFinishTotal = 0, ofdFinishTotal = 0, packetFinishTotal = 0, allTotal = 0;
        String formDefinitionId;
        for (Register register : registers) {
            formDefinitionId = register.getFormDefinitionId();
            long jpgFinish = formDataService.countId(formDefinitionId, (sqlQueryCount, formRelationWrapper) -> {
                sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), "1");
            });

            //ocr
            long ocrFinish = formDataService.countId(formDefinitionId, (sqlQueryCount, formRelationWrapper) -> {
                sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), "1");
            });

            //split
            long splitFinish = formDataService.countId(formDefinitionId, (sqlQueryCount, formRelationWrapper) -> {
                sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISASSEMBLY_STATE), "1");
            });
            //ofd
            long ofdFinish = formDataService.countId(formDefinitionId, (sqlQueryCount, formRelationWrapper) -> {
                sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3");
            });
            //打包
            long packetFinish = formDataService.countId(formDefinitionId, (sqlQueryCount, formRelationWrapper) -> {
                sqlQueryCount.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE), "1");
            });
            long all = formDataService.countId(formDefinitionId, (sqlQueryCount, formRelationWrapper) -> {
            });
            jpgFinishTotal += jpgFinish;
            ocrFinishTotal += ocrFinish;
            splitFinishTotal += splitFinish;
            ofdFinishTotal += ofdFinish;
            packetFinishTotal += packetFinish;
            allTotal += all;
        }
        SqlQuery<OcrQueue> count = SqlQuery.from(OcrQueue.class).count(OcrQueueInfo.ID)
                // ocr队列状态 等待0 转换中1 转换成功2 转换失败3 暂停中4
                .equal(OcrQueueInfo.STATUS, 0).or().equal(OcrQueueInfo.STATUS, 1);
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(count);
        long ocrPage = Long.parseLong(ocrQueues.get(0).getId());
        long jpgNeedTime = countNeedTime(allTotal, jpgFinishTotal, 0.6);
        long ocrNeedTime = Math.round(ocrFinishTime * (ocrPage) / 300);
        long splitNeedTime = countNeedTime(allTotal, splitFinishTotal, 0.5);
        long ofdNeedTime = countNeedTime(allTotal, ofdFinishTotal, 1.1);
        long packetNeedTime = countNeedTime(allTotal, packetFinishTotal, 0.5);
//        jpgFinishTotal;
        map.put("jpgFinish", jpgFinishTotal);
        map.put("ocrFinish", ocrFinishTotal);
        map.put("splitFinish", splitFinishTotal);
        map.put("ofdFinish", ofdFinishTotal);
        map.put("packetFinish", packetFinishTotal);
        map.put("jpgNeedTime", jpgNeedTime);
        map.put("ocrNeedTime", ocrNeedTime);
        map.put("splitNeedTime", splitNeedTime);
        map.put("ofdNeedTime", ofdNeedTime);
        map.put("packetNeedTime", packetNeedTime);
        map.put("all", allTotal);
        return map;
    }


    @Override
    public Register getRegister(String fid) {
        Register register = commonMapper.selectOneByQuery(SqlQuery.from(Register.class).equal(RegisterInfo.FORMDEFINITIONID, fid));
        return register;
    }

    public static long countNeedTime(long all, long finish, double finishTime) {
        //剩余份数*完成秒数/60 = 分钟数
        return Math.round((all - finish) * finishTime / 60);
    }

    /**
     * 更新批次信息  跟新日志
     *
     * @param person
     * @param formDate
     * @param type
     * @param childFormId
     * @param query
     */
    public void updateDataDetail(Person person, FormData formDate, String type, String childFormId, BaseQuery query, String status) {
        //添加日志
        addLogLongs(person, formDate, type, query);
        //下面的和之前一样
        if (!"".equals(childFormId) && childFormId != null) {
            lhUpdateDetail(formDate, type, childFormId);
        }
        //从手动拆件向后面环节提交的时候，判断是否有标注，有标注的档案tif取出来放到指定文件夹目录内
        if (status != null && status.equals("manualOperation")) {
            if (formDate.get(ArchiveEntity.DISASSEMBLY_TAGGING).equals("1")) {
                String yuanWenPath = String.join(File.separator, filePath, "tifbeifen", formDate.get(ArchiveEntity.COLUMN_FOND_CODE));
                File yuanFileDirectory = new File(yuanWenPath);
                File[] files = yuanFileDirectory.listFiles();
                String sub = ".tif";
                for (File file : files) {
                    if (file.getName().contains(formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))) {
                        if (file.isFile()) {
                            sub = file.getName().substring(file.getName().lastIndexOf("."));
                            break;
                        }

                    }
                }
                File yuanFile = new File(yuanWenPath + File.separator + formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + sub);
                if (yuanFile.exists()) {
                    String path = String.join(File.separator, filePath, "tagging", formDate.get(ArchiveEntity.COLUMN_FOND_CODE), formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + sub);
                    File file = new File(path);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                    }
                    try {
                        FileUtils.copyFile(yuanFile, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            formDate.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, "2");
        }

        //提交到档案著录或是数字化成果的档案，自动开始ofd转换，生成ofd
        if ("VOLUMES".equals(type) || "OVER".equals(type)) {
            uploadFilesService.formDataOneToOfd(formDate, query.getFondId(), person);
            if ("OVER".equals(type)) {
                //提交到over环节自动生成xml
                resultTestService.xmlFormDataGenerateFile(formDate);
            }
        }
        formDate.put(ArchiveEntity.DISASSEMBLY_TAGGING, "0");
        formDate.put(ArchiveEntity.COLUMN_STATUS, type);
        //添加唯一性人员初始化
        formDate.put(ArchiveEntity.PEOPEL_CODE, "默认");
        formDate.put(ArchiveEntity.PEOPLE_NAME, "默认");
        formDataService.updateFormDataById(formDate);
    }

    //添加日志
    public void addLogLongs(Person person, FormData formDate, String type, BaseQuery query) {
        //获取当前流程
        String status = formDate.get(ArchiveEntity.COLUMN_STATUS);
        //判断是提交还是退回
        String judge = flowPathService.judge(status, type, query.getFormDefinitionId());
        //描述//新增或者退回到目标流程   需要fiomid查询出批次流程
        String logDescription = "";
        if (!StringUtils.isEmpty(query.getRemarks()) && !"".equals(query.getRemarks())) {
            logDescription = query.getRemarks();
        } else {
            logDescription = judge + flowPathService.convert(type, query.getFormDefinitionId());
        }
        //从新写的流程控制，要有 本次流程位置，下次流程位置。操作是退回还是提交的标识 批次的id。
        archivesLogService.addArchiveFlowLog(
                query.getFondId(),
                person.getUserName(),
                status,
                type,
                judge,
                formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                logDescription,
                formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                formDate.get(ArchiveEntity.COLUMN_AJH) + "",
                query.getFormDefinitionId());
    }

    //判断第一个人工环节
    public String manualLink(String flowName) {
        for (String link : LinkFlowPath.LinkFlowPath) {
            if (!link.equals(LinkFlowPath.RECEIVE)) {
                if (flowName.contains(link)) {
                    return link;
                }
            }
        }
        return LinkFlowPath.RECEIVE;
    }
}
