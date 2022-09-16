package com.dr.digital.ofd.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface OfdService {

    String edcToOfd();

    /**
     * PDF 转换成 OFD接口
     *
     * @param formData
     * @param childFormId
     * @param registerId
     * @param person
     * @return
     * @throws Exception
     */
    boolean pdfToOfd(FormData formData, String childFormId, String registerId, Person person) throws Exception;

    boolean pdfmatching(BaseQuery query, String path);

    String upload(MultipartFile file);

    String pdfConvertOfd(boolean check, String path);

    void ofdPath(String ofdPath, HttpServletResponse request);
}
