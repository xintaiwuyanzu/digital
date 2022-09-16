package com.dr.digital.manage.impexpscheme.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * 用来解析和输出数据
 * <p>
 * 导入导出
 * <p>
 * 备份和恢复使用
 *
 * @author dr
 */
public interface DataParser {
    /**
     * 能否处理指定的文件类型
     *
     * @param mine
     * @return
     */
    boolean canHandle(String mine);

    /**
     * 获取文件后缀
     *
     * @param mine
     * @return
     */
    String getFileSuffix(String mine);

    /**
     * 读取指定文件的一条数据的所有key
     *
     * @param source
     * @param mine
     * @return
     * @throws IOException
     */
    String[] readKeys(InputStream source, String mine) throws IOException;

    /**
     * 批量读取数据
     *
     * @param source
     * @param mine
     * @return
     * @throws IOException
     */
    Iterator<Map<String, Object>> readData(InputStream source, String mine) throws IOException;

    /**
     * 批量写数据
     *
     * @param keys
     * @param data
     * @param mine
     * @param target
     * @throws IOException
     */
    void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) throws IOException;

}
