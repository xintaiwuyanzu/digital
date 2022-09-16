package com.dr.digital.ocr.entity;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskInfoList {
    public enum TaskStatus {
        START, RUNNING, FINISHED
    }
    //任务信息类
    public static class TaskInfo {
        String uuid;
        String name = "";
        Long startTime;
        Long endTime;
        TaskStatus status;
        Integer total = 0;
        Integer process = 0;
        Integer size = 0;
        //最后生成的文件存放的地址
        String url = "";

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public void setStatus(TaskStatus status) {
            this.status = status;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getProcess() {
            return process;
        }

        public void setProcess(Integer process) {
            this.process = process;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static Map<String, TaskInfo> taskList = new HashMap<>();

    /**
     * 添加新任务并返回当前所有任务的总数
     *
     * @param task
     * @return
     */
    public Integer addNewTask(TaskInfo task) {
        taskList.put(task.getUuid(), task);
        return taskList.size();
    }

    /**
     * 更新任务状态
     *
     * @param task
     */
    public void update(TaskInfo task) {
        taskList.put(task.getUuid(), task);
    }
    /**
     * 根据uuid移除相关的任务信息
     *
     * @param task
     */
    public void remove(TaskInfo task) {
        taskList.remove(task.getUuid());
    }
    /**
     * 根据uuid查询
     *
     * @param uuid
     * @return
     */
    public TaskInfo search(String uuid) {
        return taskList.get(uuid);
    }

    //系统初始化的时候
    @PostConstruct
    private void init() {
    }

    //系统结束运行的时候
    @PreDestroy
    public void destroy() {
        //系统运行结束
        taskList.clear();
    }
}