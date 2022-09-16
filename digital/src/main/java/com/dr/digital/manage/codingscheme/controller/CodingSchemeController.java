package com.dr.digital.manage.codingscheme.controller;

import com.dr.digital.enums.FilesField;
import com.dr.digital.manage.codingscheme.entity.CodingScheme;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeInfo;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeItem;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeItemInfo;
import com.dr.digital.manage.codingscheme.service.CodingSchemeService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.util.Constants;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@ResponseBody
@RestController
@RequestMapping("/api/codingscheme")
public class CodingSchemeController extends BaseServiceController<CodingSchemeService, CodingScheme> {
    @Autowired
    CommonService commonService;

    @Override
    protected SqlQuery<CodingScheme> buildPageQuery(HttpServletRequest httpServletRequest, CodingScheme scheme) {
        SqlQuery<CodingScheme> sqlQuery = SqlQuery.from(CodingScheme.class)
                .equal(CodingSchemeInfo.BUSINESSID, scheme.getBusinessId());
        if (!StringUtils.isEmpty(scheme.getName())) {
            sqlQuery.like(CodingSchemeInfo.NAME, scheme.getName());
        }
        return sqlQuery;
    }

    @Override
    protected SqlQuery<CodingScheme> buildDeleteQuery(HttpServletRequest request, CodingScheme entity) {
        String aids = request.getParameter("aIds");
        Assert.isTrue(!StringUtils.isEmpty(aids), "删除的Id不能为空!");
        return SqlQuery.from(CodingScheme.class, false)
                .column(CodingSchemeInfo.ID)
                .in(CodingSchemeInfo.ID, aids.split(","));
    }

    @RequestMapping("/getFieldCheck")
    public ResultEntity getFieldCheck(@RequestParam(defaultValue = "false") Boolean fond) {
        FilesField[] values = FilesField.values();
        List<Map> list = new ArrayList<>();
        for (FilesField value : values) {
            Map<String, String> map = new HashMap<>();
            if (!fond && ArchiveEntity.COLUMN_ARCHIVE_CODE.equals(value.getCode())) {
            } else {
                map.put("code", value.getCode());
                map.put("name", value.getName());
                list.add(map);
            }
        }
        return ResultEntity.success(list);
    }

    @RequestMapping("/getFields")
    public ResultEntity getFields() {
        Field[] fields = AbstractArchiveEntity.class.getFields();
        List<Map> list = new ArrayList<>();
        for (Field field : fields) {
            Map<String, String> map = new HashMap<>();
            map.put("code", field.getName());
            map.put("name", field.getName());
            list.add(map);
        }
        return ResultEntity.success(list);
    }

    @RequestMapping("/getFieldCx")
    public ResultEntity getFieldCx(String fondId, String categoryId) {
        CodingScheme codingScheme = service.selectOne(SqlQuery.from(CodingScheme.class).equal(CodingSchemeInfo.BUSINESSID, fondId).equal(CodingSchemeInfo.ISDEFAULT, Constants.YES));
        List<CodingSchemeItem> list = new ArrayList<>();
        if (codingScheme != null) {
            String bussinessId = codingScheme.getId();
            list = commonService.selectList(SqlQuery.from(CodingSchemeItem.class).equal(CodingSchemeItemInfo.BUSINESSID, bussinessId).orderBy(CodingSchemeItemInfo.ORDERBY));
        }
        return ResultEntity.success(list);
    }

    /**
     * @param findId
     * @param index
     * @param size
     * @return
     */
    @PostMapping(value = "/findSchemeByFondId")
    public ResultEntity findSchemeByFondId(String findId,
                                           @RequestParam(defaultValue = "1") String index,
                                           @RequestParam(defaultValue = "15") String size) {
        return ResultEntity.success(service.findSchemeByFondId(findId, Integer.parseInt(index), Integer.parseInt(size)));
    }

    /**
     * 获取当前年的分类的编码方案
     *
     * @param businessId
     * @return
     */
    @PostMapping(value = "/findSchemeByBusinessId")
    public ResultEntity findSchemeByBusinessId(String businessId) {
        return ResultEntity.success(service.findSchemeByBusinessId(businessId));
    }

}