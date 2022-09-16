package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-26 9:35
 */
public class StructContent {
    private List<Page> page;
    private List<Paragraph> paragraph;
    private List<Row> row;

    public List<Page> getPage() {
        return page;
    }

    public void setPage(List<Page> page) {
        this.page = page;
    }

    public List<Paragraph> getParagraph() {
        return paragraph;
    }

    public void setParagraph(List<Paragraph> paragraph) {
        this.paragraph = paragraph;
    }

    public List<Row> getRow() {
        return row;
    }

    public void setRow(List<Row> row) {
        this.row = row;
    }
}
