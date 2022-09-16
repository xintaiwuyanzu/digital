package com.foxit.ofd.imgsdk;

/**
 * @brief OfdPackage
 */
public class OfdPackage {
    /**
     * @return 返回包对象。
     * @brief 创建一个空包。
     * @param[in] file                指定的文件名。
     */
    public static native long create(String file);

    /**
     * @return 成功返回文档对象。
     * @brief 新建一个文档，并追加至包中。
     */
    public static native long addDocument(long pack);

    /**
     * @return 无。
     * @brief 保存修改。
     * @param[in] package            包对象。
     */
    public static native void save(long pack);

    /**
     * @return 无。
     * @brief 关闭OFD包，并释放内存。
     */
    public static native void close(long pack);
}
