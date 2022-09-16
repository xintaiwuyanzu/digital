package com.foxit.ofd.imgsdk;

/**
 * @brief OfdDocument
 */
public class OfdDocument {
    /**
     * @return 返回页对象。
     * @brief 新建一页，并追加至文档中。
     * @param[in] document        文档对象。
     */
    public static native long addPage(long document);

}
