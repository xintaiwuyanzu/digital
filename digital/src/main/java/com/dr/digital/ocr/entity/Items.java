package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-26 9:30
 */
public class Items {
    private String content;
    private List<Positions> positions;
    private List<CharPositions> char_positions;
    private List<Probabilities> probabilities;
    private String handwrite_info;
    private String importance_info;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Positions> getPositions() {
        return positions;
    }

    public void setPositions(List<Positions> positions) {
        this.positions = positions;
    }

    public List<CharPositions> getChar_positions() {
        return char_positions;
    }

    public void setChar_positions(List<CharPositions> char_positions) {
        this.char_positions = char_positions;
    }

    public List<Probabilities> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<Probabilities> probabilities) {
        this.probabilities = probabilities;
    }

    public String getHandwrite_info() {
        return handwrite_info;
    }

    public void setHandwrite_info(String handwrite_info) {
        this.handwrite_info = handwrite_info;
    }

    public String getImportance_info() {
        return importance_info;
    }

    public void setImportance_info(String importance_info) {
        this.importance_info = importance_info;
    }


    public static class CharPositions {
        private List<Positions> positions;

        public List<Positions> getPositions() {
            return positions;
        }

        public void setPositions(List<Positions> positions) {
            this.positions = positions;
        }
    }


    public static class Probabilities {
        private String char1;
        private String probability;

        public String getChar() {
            return char1;
        }

        public void setChar(String char1) {
            this.char1 = char1;
        }

        public String getProbability() {
            return probability;
        }

        public void setProbability(String probability) {
            this.probability = probability;
        }
    }
}
