package com.dr.digital.packet.entity;

import java.util.List;

/**
 * 档案归档信息
 *
 * @author dr
 */
public class ArchiveReceiveBo {
    /**
     * 移交目录元数据文件地址
     */
    private String xmlPath;
    private String classify;
    private String arrange;

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getArrange() {
        return arrange;
    }

    public void setArrange(String arrange) {
        this.arrange = arrange;
    }

    /**
     * 归档文件信息
     */

    private List<ArchiveFileInfo> files;

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public List<ArchiveFileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<ArchiveFileInfo> files) {
        this.files = files;
    }

    public static class ArchiveFileInfo {
        // 系统编号
        private String systemNum;
        //系统名称
        private String systemName;
        //业务主键
        private String businessId;
        //ofd数据包名
        private String ofdName;
        //下载地址
        private String path;
        //数字摘要
        private String digitaldigest;

        public String getSystemNum() {
            return systemNum;
        }

        public void setSystemNum(String systemNum) {
            this.systemNum = systemNum;
        }

        public String getSystemName() {
            return systemName;
        }

        public void setSystemName(String systemName) {
            this.systemName = systemName;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getOfdName() {
            return ofdName;
        }

        public void setOfdName(String ofdName) {
            this.ofdName = ofdName;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDigitaldigest() {
            return digitaldigest;
        }

        public void setDigitaldigest(String digitaldigest) {
            this.digitaldigest = digitaldigest;
        }
    }

}
