package com.dr.digital.ofd.bo;

/**
 * 文件基本信息
 * 这个是接口请求参数
 *
 * @author dr
 */
public class FileByteInfo {
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 源路径
     */
    private String srcPath;
    /**
     * 目标路径
     */
    private String targetPath;
    /**
     * 目标文件格式
     */
    private String targetFileFormat;
    /**
     * 转换类型
     */
    private String taskType;
    /**
     * 认证token,通过获取token接口获取
     */
    private String token;
    /**
     * 接受修订不能为空 0保持原样 1去修订
     */
    private int bAcceptRev;
    /**
     * 接受批注不能为空 0保持原样 1去除批注
     */
    private int bAcceptAnnotate;
    /**
     * 嵌入不能为空 0不嵌入 1嵌入字体
     */
    private int archive;

    public FileByteInfo(String taskId, String srcPath, String targetPath) {
        this.taskId = taskId;
        this.srcPath = srcPath;
        this.targetPath = targetPath;
        this.targetFileFormat = "ofd";
        this.bAcceptRev = 0;
        this.bAcceptAnnotate = 0;
        this.archive = 1;
        this.taskType = "pdf->ofd";
    }
    public FileByteInfo(String taskId, String srcPath, String targetPath,String token) {
        this.taskId = taskId;
        this.srcPath = srcPath;
        this.targetPath = targetPath;
        this.targetFileFormat = "ofd";
        this.bAcceptRev = 0;
        this.bAcceptAnnotate = 0;
        this.archive = 1;
        this.taskType = "pdf->ofd";
        this.token = token;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetFileFormat() {
        return targetFileFormat;
    }

    public void setTargetFileFormat(String targetFileFormat) {
        this.targetFileFormat = targetFileFormat;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getbAcceptRev() {
        return bAcceptRev;
    }

    public void setbAcceptRev(int bAcceptRev) {
        this.bAcceptRev = bAcceptRev;
    }

    public int getbAcceptAnnotate() {
        return bAcceptAnnotate;
    }

    public void setbAcceptAnnotate(int bAcceptAnnotate) {
        this.bAcceptAnnotate = bAcceptAnnotate;
    }

    public int getArchive() {
        return archive;
    }

    public void setArchive(int archive) {
        this.archive = archive;
    }
}
