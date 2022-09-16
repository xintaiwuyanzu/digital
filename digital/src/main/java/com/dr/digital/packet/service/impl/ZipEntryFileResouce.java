package com.dr.digital.packet.service.impl;

import com.dr.framework.common.file.FileResource;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author caor
 * @date 2021-10-27 19:56
 */
public class ZipEntryFileResouce implements FileResource {
    private final ZipArchiveEntry entry;
    private final ZipFile file;
    private final long defaultTime = System.currentTimeMillis();


    public ZipEntryFileResouce(ZipArchiveEntry entry, ZipFile file) {
        this.entry = entry;
        this.file = file;
    }

    @Override
    public String getName() {
        String fileName = entry.getName();
        int index = fileName.lastIndexOf("/");
        if (index > 0) {
            fileName = fileName.substring(index + 1);
        }
        return fileName;
    }

    @Override
    public long getCreateDate() {
        System.out.println("entry.getCreationTime()=" + entry.getCreationTime());
        return Optional.ofNullable(entry.getCreationTime()).map(fileTime -> fileTime.to(TimeUnit.MILLISECONDS)).orElse(defaultTime);
    }

    @Override
    public long getLastModifyDate() {
        return Optional.ofNullable(entry.getLastModifiedTime()).map(fileTime -> fileTime.to(TimeUnit.MILLISECONDS)).orElse(defaultTime);
    }

    @Override
    public long getFileSize() {
        return entry.getSize();
    }

    @Override
    public String getDescription() {
        String fileName = entry.getName();
        int index = fileName.lastIndexOf("/");
        if (index > 0) {
            return fileName.substring(0, index);
        }
        return entry.getComment();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return file.getInputStream(entry);
    }
}
