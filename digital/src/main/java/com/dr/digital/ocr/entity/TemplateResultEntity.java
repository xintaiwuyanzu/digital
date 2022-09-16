package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-22 14:32
 */
public class TemplateResultEntity {
    private String code;
    private String message;
    private Data data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    public static class Data {
        private List<Results> results;
        private String template_name;
        private String msgId;
        private Raw raw;
        private String template_hash;
        private String update_time;

        public List<Results> getResults() {
            return results;
        }

        public void setResults(List<Results> results) {
            this.results = results;
        }

        public String getTemplate_name() {
            return template_name;
        }

        public void setTemplate_name(String template_name) {
            this.template_name = template_name;
        }

        public String getMsgId() {
            return msgId;
        }

        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }

        public Raw getRaw() {
            return raw;
        }

        public void setRaw(Raw raw) {
            this.raw = raw;
        }

        public String getTemplate_hash() {
            return template_hash;
        }

        public void setTemplate_hash(String template_hash) {
            this.template_hash = template_hash;
        }

        public String getUpdate_time() {
            return update_time;
        }

        public void setUpdate_time(String update_time) {
            this.update_time = update_time;
        }
    }
}
