package com.dr.digital.open.controller;

import com.dr.digital.open.vo.ReturnData;
import com.dr.digital.util.Img2PdfUtil;
import com.dr.framework.common.entity.ResultEntity;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author caor
 * @date 2021-12-14 18:01
 */

@RestController
@RequestMapping("/v1")
public class Opensv1Controller {

    /**
     * 加工模块统一接口示例 todo 接口文档有点乱 不确定这里参数是否正确
     *
     * @param byteList
     * @param destType   目标文件类型
     * @param handleType 加工操作类型
     * @param paramMap
     * @return
     */
    @RequestMapping("/ofd/byteStream")
    public ResultEntity byteStream(List<Map<String, Object>> byteList,
                                   String destType,
                                   String handleType,
                                   String paramMap) throws Exception {
        //1。校验
        if (handleType.equals("merge")) {
            //合并操作至少两个文件
            Assert.isTrue(byteList.size() > 1, "合并操作至少需要两个文件");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", destType);
        //输出地址
        String outpath = "D:\\archiveToPack\\cuiyj";
        //源文件目录 地址
        String outpath2 = outpath + "//" + UUID.randomUUID();
        for (int i = 0; i < byteList.size(); i++) {
            //源文件类型
            String srcType = (String) byteList.get(i).get("srcType");
            //源文件文件地址
            File fileout = new File(outpath2 + UUID.randomUUID() + srcType);
            //二进制流
            String bytes = (String) byteList.get(i).get("bytes");
            byte[] bytes1 = bytes.getBytes(StandardCharsets.ISO_8859_1);
            //写入源文件
            FileCopyUtils.copy(bytes1, fileout);
        }
        //把源文件 合并为pdf/ofd  返回输出文件地址
        String pdf = Img2PdfUtil.imagesToPdf(outpath2, outpath, UUID.randomUUID().toString());
        File file = new File(pdf);
        String bates = "";
        InputStream stream = new FileInputStream(file);
        final byte[] bytes = FileCopyUtils.copyToByteArray(stream);
        bates = Base64.encodeBase64String(bytes);
        map.put("bytes", bates);
        map.put("convertStatus", "0");
        return ResultEntity.success(map);
    }

    /**
     * 同类型文件合并 对应文档（23.合并(不支持多类型文件！请使用文档12或22中的合并)
     *
     * @param src  源文件目录
     * @param save 生成文件目录
     * @return
     */
    @RequestMapping("/ofd/mergeBatchPage")
    public ReturnData mergeBatchPage(
            String src,
            String save) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "ofd");
        String bates = "";
        try {
            String pdf = Img2PdfUtil.imagesToPdf(src, save, UUID.randomUUID().toString());
            File file = new File(pdf);
            InputStream stream = new FileInputStream(file);
            final byte[] bytes = FileCopyUtils.copyToByteArray(stream);
            bates = new String(bytes, StandardCharsets.ISO_8859_1);
            map.put("bytes", bates);
            map.put("convertStatus", "0");
        } catch (Exception e) {
            ReturnData data = new ReturnData();
            map.put("bytes", "");
            map.put("convertStatus", "1");
            data.setData(map);
            data.setReturnmsg("error");
            data.setReturncode("");
            return data;
        }
        return ReturnData.success(map);
    }

}
