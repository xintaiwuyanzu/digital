package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-26 10:13
 */
public class Results {
    private String field_name;
    private List<String> results;

    public String getField_name() {
        return field_name;
    }

    public void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }
}
