package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-26 9:31
 */
public class Tables {
    private String table_id;
    private String row;
    private List<Cells> cells;

    public String getTable_id() {
        return table_id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public List<Cells> getCells() {
        return cells;
    }

    public void setCells(List<Cells> cells) {
        this.cells = cells;
    }

    public static class Cells {
        private String content;
        private String start_row;
        private String end_row;
        private String start_col;
        private String end_col;
        private List<Positions> positions;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getStart_row() {
            return start_row;
        }

        public void setStart_row(String start_row) {
            this.start_row = start_row;
        }

        public String getEnd_row() {
            return end_row;
        }

        public void setEnd_row(String end_row) {
            this.end_row = end_row;
        }

        public String getStart_col() {
            return start_col;
        }

        public void setStart_col(String start_col) {
            this.start_col = start_col;
        }

        public String getEnd_col() {
            return end_col;
        }

        public void setEnd_col(String end_col) {
            this.end_col = end_col;
        }

        public List<Positions> getPositions() {
            return positions;
        }

        public void setPositions(List<Positions> positions) {
            this.positions = positions;
        }
    }
}
