package com.dr.digital.enums;

import com.dr.digital.manage.model.entity.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 档案门类，这里写死门类类型
 * 因为具体的门类需要有具体的业务逻辑
 *
 * @author zhangb
 * @author tuzl
 * @author haolsh
 * <p>
 * <p>
 * <p>
 * 文书档案、科技档案、人事档案、会计档案
 * <p>
 * 专业档案
 * <p>
 * 照片档案、录音档案、录像档案
 * <p>
 * 业务数据档案、公务电子邮件档案、网站信息档案、社交媒体档案
 * <p>
 * 实物档案
 * <p>
 * 其他档案
 */
public enum KindType {

    /**
     * 文书门类
     */
    Document(1, "文书门类", AbstractArchiveEntity.class, AbstractArchiveEntity4AJ.class, AbstractArchiveEntity4WJ.class, AbstractArchiveEntity4JH.class),
    /**
     * 人事门类
     */
    PERSONNEL(2, "人事门类", AbstractArchiveEntity.class, AbstractArchiveEntity.class, AbstractArchiveEntity.class),
    /**
     * 会计门类
     */
    Accountant(3, "会计门类", AbstractArchiveEntity.class, AbstractArchiveEntity.class, AbstractArchiveEntity.class),

    /**
     * 其他档案
     */
    OTHER(0, "默认分类", AbstractArchiveEntity.class, AbstractArchiveEntity4AJ.class, AbstractArchiveEntity4WJ.class, AbstractArchiveEntity4JH.class);
    /**
     * 编号
     */
    private final Integer code;
    /**
     * 显示名称
     */
    private final String value;
    /**
     * 项目表基础父类
     */
    private Class<? extends ArchiveEntity> proBaseClass;
    /**
     * 案卷基础父类
     */
    private Class<? extends ArchiveEntity> arcBaseClass;
    /**
     * 文件基础父类
     */
    private final Class<? extends ArchiveEntity> fileBaseClass;
    /**
     * 件盒基础父类
     */
    private Class<? extends ArchiveEntity> boxBaseClass;

    KindType(Integer code, String value, Class<? extends ArchiveEntity> fileBaseClass) {
        this.code = code;
        this.value = value;
        this.fileBaseClass = fileBaseClass;
    }

    KindType(Integer code, String value, Class<? extends ArchiveEntity> arcBaseClass, Class<? extends ArchiveEntity> fileBaseClass) {
        this.code = code;
        this.value = value;
        this.arcBaseClass = arcBaseClass;
        this.fileBaseClass = fileBaseClass;
    }

    KindType(Integer code, String value, Class<? extends ArchiveEntity> proBaseClass, Class<? extends ArchiveEntity> arcBaseClass, Class<? extends ArchiveEntity> fileBaseClass) {
        this.code = code;
        this.value = value;
        this.proBaseClass = proBaseClass;
        this.arcBaseClass = arcBaseClass;
        this.fileBaseClass = fileBaseClass;
    }

    KindType(Integer code, String value, Class<? extends ArchiveEntity> proBaseClass, Class<? extends ArchiveEntity> arcBaseClass, Class<? extends ArchiveEntity> fileBaseClass, Class<? extends ArchiveEntity> boxBaseClass) {
        this.code = code;
        this.value = value;
        this.proBaseClass = proBaseClass;
        this.arcBaseClass = arcBaseClass;
        this.fileBaseClass = fileBaseClass;
        this.boxBaseClass = boxBaseClass;
    }

    public static Integer getCode(Integer code) {
        KindType[] imageFormatTypes = values();
        for (KindType imageFormatType : imageFormatTypes) {
            if (imageFormatType.code().equals(code)) {
                return imageFormatType.code();
            }
        }
        return null;
    }

    public static String getValue(Integer code) {
        KindType[] imageFormatTypes = values();
        for (KindType imageFormatType : imageFormatTypes) {
            if (imageFormatType.code().equals(code)) {
                return imageFormatType.value();
            }
        }
        return null;
    }

    public Integer code() {
        return code;
    }

    public String value() {
        return value;
    }

    public Class<? extends ArchiveEntity> getProBaseClass() {
        return proBaseClass;
    }

    public Class<? extends ArchiveEntity> getArcBaseClass() {
        return arcBaseClass;
    }

    public Class<? extends ArchiveEntity> getFileBaseClass() {
        return fileBaseClass;
    }

    public Class<? extends ArchiveEntity> getBoxBaseClass() {
        return boxBaseClass;
    }

    static final List<Map<String, String>> mapList;

    public static List<Map<String, String>> getMapList() {
        return mapList;
    }

    static {
        mapList = Arrays.stream(KindType.values()).map(k -> {
            Map<String, String> map = new HashMap<>();
            map.put("key", k.value());
            map.put("value", k.name());
            return map;
        }).collect(Collectors.toList());
    }

}
