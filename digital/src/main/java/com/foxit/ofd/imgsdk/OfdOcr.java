package com.foxit.ofd.imgsdk;

/**
 * @brief OfdOcr
 * 当前sdk只会引入一个OCR引擎（目前支持KPOCR、WintoneOCR、YINGYUANOCR，根据项目需要选择）
 * OCR引擎不同，相关接口参数及支持功能有所不同，详见接口声明
 */
public class OfdOcr {
    /**
     * @brief OCR开始
     * @param[in] imgfile         图片文件路径。
     * return ocr handler.
     */
    public static native long Start(String imgfile);

    /**
     * @return 正确返回 0， 错误返回其它值
     * @brief OCR 继续
     * @param[in] handler         Start函数返回值。
     * @param[in] page            页面对象。
     * @param[in] x               图片在页面的X坐标。
     * @param[in] y               图片在页面的Y坐标。
     * @param[in] width           图片在页面的宽度。
     * @param[in] height          图片在页面的高度。
     */
    public static native int Continue(long handler, long page, float x, float y, float width, float height);

    /**
     * @brief 获取图片宽度和高度
     * @param[in] handler         Start函数返回值。
     * @param[in] nType           类型 0 宽度， 1高度。
     */
    public static native int Getimgwh(long handler, int nType);

    /**
     * @brief OCR结束
     * @param[in] handler         Start函数返回值。
     */
    public static native void End(long handler);

    /**
     * @return 正确返回 0， 错误返回其它值
     * @brief OCR识别图片输出到指定格式文件。
     * @param[in] srcFile           WintoneOCR:png,jpg格式图片,pdf文件;
     * KPOCR:png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
     * @param[in] destFile          txt、双层PDF、双层OFD, 结构化数据json文件(KPOCR)。
     * PDF/OFD输出尺寸大小：如果图片最长边小于A4最长边（297.0）的90%, 那么就取图片原始尺寸；
     * 其它尺寸按比例缩放为A4大小
     * @param[in] option            ocr扩展选项,目前用于WintoneOCR及KPOCR
     * WintoneOCR:((单双层)&0x01)|((txt文本字符编码&0x0f)<<1)|(OUTPUT_RTF_OPTION&0x1F)|((语言编码&0x3f)<<5)|(OCRFILE_OPTION&0x03<<11)
     * bit0: img2pdf选项，0-双层，1-单层图片，
     * bit1: img2txt，文本字符编码，具体值参见文通文档，下同
     * bit0-bit4: img2rtf, OUTPUT_RTF_OPTION
     * bit5-bit10: 语言编码
     * bit11-bit10: pdf识别参数,OCRFILE_OPTION
     * KPOCR:((单双层)&0x1)|((is_img&0x1)<<12)|((mode&0x1)<<32)|((use_cls&0x1)<<33)
     * bit0: 单双层，0-双层，1-单层图片，仅对图片文件有效, pdf/ofd输入直接转双层
     * bit12: ofd页面是否完全为img, 0-图文混合，1-完全图片，默认0
     * bit14: 是否添加分页符，默认0-否，1-添加分页符：“_FXKP_OFD2TXT_PAGE_${NO}_”，${NO}为页码从1开始递增
     * bit33: 是否使用方向分类器，1-使用，默认0不用
     * @deprecated reason this method is deprecated {will be removed in next version}
     * use {@link #OcrOutput(String srcFile, String destFile, String option)} instead
     */
    @Deprecated
    public static native int OcrOutput(String srcFile, String destFile, long option);

    /**
     * @return 正确返回 0， 错误返回其它值
     * @brief OCR识别图片输出到指定格式文件。
     * @param[in] srcFile           WintoneOCR:png,jpg格式图片,pdf文件;
     * png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
     * @param[in] destFile          txt、双层PDF、双层OFD, 结构化数据json文件
     * PDF/OFD输出尺寸大小：如果图片最长边小于A4最长边（297.0）的90%,
     * 或者指定img_scaling为100, 那么就取图片原始尺寸（DPI正常可计算尺寸）；
     * 其它按比例缩放为A4大小
     * @param[in] option            可选参数（格式:json），不用的参数就不要给，可以为null
     * {
     * "to_img":0     #OFD/PDF单双层选项, 0-双层，1-单层图片，仅对图片文件有效, pdf/ofd输入直接转双层
     * "img_scaling":0   #image转OFD（经ofd再pdf也可以）有效, 图片缩放百分比，默认0，自动处理；
     * 100-图像原始尺寸（限dpi正常可计算图像尺寸）
     * "pages":{"doc_idx":0,"start":0,"count":0}
     * #输入为多页文件（gif/tiff/tiff/pdf/ofd）时，可选择部分页{起点（从0开始,ofd可指定doc_idx），多少页}输出
     * 文通ocr对输入为tif,pdf，输出为txt，pdf,ofd时，直接走文通ocr接口，不支持页面选择
     * "margin":{"left":0,"right":0,"top":0,"bottom":0,}  #img转ofd或pdf时，页边距设置
     * "txt_encode":0 #文通OCR预留，0-UNICODE，1-GB2312, 2-TXT_BIG5
     * 3-TXT_SJIS, 4-TXT_KCS, 5-TXT_UTF8, 6-TXT_ISO1252
     * "rtf_option":0 #文通OCR支持，RTF 文件选项: 0-简单文本 ,1-输出字号 ,2-精确版面还原 ,
     * 3-类型 mask ,4-去除硬回车
     * "lang_code":0  #文通OCR支持（kpocr目前是修改配置实现）
     * 0-简体中文 ,1-繁体中文 ,2-纯英文和数字 ,3-日文 ,
     * 4-手写体 ,5-简体大字符集 ,6-繁体大字符集 ,7-韩文 ,14-阿拉伯
     * "ofd_img":0    #ofd页面是否完全为img, 0-图文混合，1-完全图片，默认0
     * "page_break":0 #kpocr支持，是否添加分页符，默认0-否，1-添加分页符：“_FXKP_OFD2TXT_PAGE_${NO}_”，${NO}为页码从1开始递增
     * "use_cls": 0   #kpocr支持,是否使用方向分类器，1-使用，默认0不用
     * "a4tif": 0     #kpocr支持，是否强制将tif转换成A4尺寸
     * }
     */
    public static native int OcrOutput(String srcFile, String destFile, String option);


    /**
     * @return 识别结果数据(json格式)
     * @brief OCR识别图片并输出结构化JSON格式数据
     * @param[in] srcFile           输入png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
     * @param[in] option            扩展选项((mode&0x1)<<32)|((use_cls&0x1)<<33)
     * bit33: #KPOCR支持，是否使用方向分类器，1-使用，默认0不用
     * @deprecated reason this method is deprecated {will be removed in next version}
     * use {@link #OcrOutStream(String srcFile, String option)} instead
     */
    @Deprecated
    public static native String OcrOutStream(String srcFile, long option);

    /**
     * @return 识别结果数据(json格式)
     * @brief OCR识别图片并输出结构化JSON格式数据
     * @param[in] srcFile           输入png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
     * @param[in] option            可选参数（格式:json），不用的参数就不要给，可以为null
     * {
     * "pages":{"doc_idx":0,"start":0,"count":0}
     * #输入为多页文件（gif/tiff/tiff/pdf/ofd）时，可选择部分页{起点（从0开始,ofd可指定doc_idx），多少页}输出
     * "use_cls": 0   #KPOCR支持，是否使用方向分类器，1-使用，默认0不用
     * }
     */
    public static native String OcrOutStream(String srcFile, String option);

    public static native int CreatePageWithOcrData(long page, String imageFile, String textFile);
}
