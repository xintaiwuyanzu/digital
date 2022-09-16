package com.dr.digital.manage.model.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 表单数据查询参数
 *
 * @author dr
 */
public class ArchiveDataQuery {
    public static final String QUERY_KEY = "_QUERY";
    static final ObjectMapper objectMapper = new JsonMapper();
    /**
     * 表单Id
     */
    private String formDefinitionId;
    /**
     * 全宗Id
     */
    private String fondId;
    /**
     * 分类Id
     */
    private String categoryId;

    /**
     * 档号
     */
    private String archiveCode;
    /**
     * 具体的查询条件
     */
    private List<QueryItem> queryItems = new ArrayList<>();

    /**
     * 解析前端传过来的
     * json 格式的查询条件
     *
     * @return
     */
    public ArchiveDataQuery parseQuery(String query) {
        if (!StringUtils.isEmpty(query)) {
            try {
                JsonNode node = objectMapper.readTree(query);
                if (node.isArray()) {
                    node.forEach(this::parseItem);
                } else {
                    parseItem(node);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    private void parseItem(JsonNode jsonNode) {
        if (jsonNode.has("key")) {
            String key = jsonNode.get("key").asText();
            QueryType type = null;
            if (jsonNode.has("type")) {
                String typeStr = jsonNode.get("type").asText();
                type = QueryType.from(typeStr);
            }
            if (type == null) {
                type = QueryType.EQUAL;
            }
            if (jsonNode.has("value")) {
                String value = jsonNode.get("value").asText();
                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                    queryItems.add(new QueryItem(key, value, type));
                }
            }
        }
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getFondId() {
        return fondId;
    }

    public void setFondId(String fondId) {
        this.fondId = fondId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<QueryItem> getQueryItems() {
        return queryItems;
    }

    public void setQueryItems(List<QueryItem> queryItems) {
        this.queryItems = queryItems;
    }

    public static String getQueryKey() {
        return QUERY_KEY;
    }

    /**
     * 查询类型
     */
    public enum QueryType {
        /**
         * 等于
         */
        EQUAL("e"),

        /**
         *
         */
        notEqual("ne"),
        /**
         * 模糊
         */
        LIKE("l"),
        /**
         * 以。。。开始
         */
        START_WITH("s"),
        /**
         * 以。。。结束
         */
        END_WITH("ed"),
        /**
         * in语句
         */
        IN("i");
        private final String code;

        QueryType(String code) {
            this.code = code;
        }

        static QueryType from(String code) {
            QueryType queryType = null;
            try {
                queryType = QueryType.valueOf(code);
            } catch (Exception ignored) {
            }
            if (queryType == null) {
                for (QueryType value : QueryType.values()) {
                    if (value.code.equalsIgnoreCase(code)) {
                        queryType = value;
                        break;
                    }
                }
            }
            return queryType;
        }
    }

    public static class QueryItem {
        /**
         * 查询字段
         */
        private String key;
        /**
         * 查询条件
         */
        private String value;
        /**
         * 查询类型
         */
        private QueryType type;

        public QueryItem(String key, String value, QueryType type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public QueryType getType() {
            return type;
        }

        public void setType(QueryType type) {
            this.type = type;
        }
    }


}
