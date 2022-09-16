package com.dr.digital.manage.filehook.service.service;

/**
 * @author caor
 * @date 2021-03-11 14:16
 */
public interface FileHookService {

    boolean isExists(String path);

    /**
     * 批量挂接
     *
     * @param souceFilesPath
     */
    void quantityHook(String souceFilesPath);
}
