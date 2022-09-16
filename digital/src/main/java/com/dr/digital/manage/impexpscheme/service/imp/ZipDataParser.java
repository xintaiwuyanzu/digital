package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.digital.manage.impexpscheme.service.DataParser;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.file.autoconfig.CommonFileConfig;
import com.dr.framework.common.file.model.FileInfo;
import com.dr.framework.common.file.resource.FileSystemFileResource;
import com.dr.framework.common.file.service.CommonFileService;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 压缩包数据处理器
 *
 * @author dr
 */
@Component
public class ZipDataParser implements DataParser {
    final MediaType mediaType = MediaType.parseMediaType("application/zip");

    /**
     * 根目录excel文件名称
     */
    @Value("${archive.common.zip.excelName:archive.xls}")
    String excelName = "archive.xls";
    @Autowired
    ExcelDataParser excelDataParser;
    @Autowired
    CommonFileService fileService;
    @Autowired
    CommonFileConfig commonFileConfig;


    @Override
    public boolean canHandle(String mine) {
        return mediaType.includes(MediaType.parseMediaType(mine));
    }

    @Override
    public String getFileSuffix(String mine) {
        return "zip";
    }

    @Override
    public String[] readKeys(InputStream source, String mine) throws IOException {
        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(source)) {
            ZipArchiveEntry zipEntry = zipInputStream.getNextZipEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().equalsIgnoreCase(excelName)) {
                    break;
                }
                zipEntry = zipInputStream.getNextZipEntry();
            }
            if (zipEntry == null) {
                return new String[0];
            }
            return excelDataParser.readKeys(zipInputStream, "");
        }
    }

    /**
     * 读取数据，同时也要读取附件
     *
     * @param source
     * @param mine
     * @return
     * @throws IOException
     */
    @Override
    public Iterator<Map<String, Object>> readData(InputStream source, String mine) throws IOException {
        //这里没有特别好的办法
        //先将所有的文件解压到临时文件夹
        String tempDir = commonFileConfig.getFullDirPath("temp", UUIDUtils.getUUID(), null);
        //然后解析excel
        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(source)) {
            while (true) {
                ZipArchiveEntry archiveEntry = zipInputStream.getNextZipEntry();
                if (archiveEntry == null) {
                    break;
                }
                if (archiveEntry.isDirectory()) {
                    continue;
                }
                String fileName = tempDir + File.separator + archiveEntry.getName().getBytes("GBK");
                File file = new File(fileName);
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                StreamUtils.copy(zipInputStream, outputStream);
                ImgVo imgVo = new ImgVo();
                String fileName1 = archiveEntry.getName().substring(0, archiveEntry.getName().lastIndexOf("."));
                String fileSize1 = Long.toString(archiveEntry.getSize());
                imgVo.setFilePath(fileName);
                imgVo.setFileName(fileName1);
                imgVo.setFileSize(fileSize1);
                outputStream.close();
            }
            return new ZipExcelIterator(tempDir);
        }
    }

    class ZipExcelIterator implements Iterator<Map<String, Object>>, Closeable {
        final String tempDir;
        Iterator<Map<String, Object>> excelIterator;

        ZipExcelIterator(String tempDir) throws IOException {
            this.tempDir = tempDir;
            excelIterator = excelDataParser.readData(new FileInputStream(new File(tempDir, excelName)), "");
        }

        @Override
        public void close() throws IOException {
            //关闭excel输入流
            if (excelIterator instanceof Closeable) {
                ((Closeable) excelIterator).close();
            }
            //删除临时文件夹
            FileUtils.deleteDirectory(new File(tempDir));
        }

        @Override
        public boolean hasNext() {
            return excelIterator.hasNext();
        }

        @Override
        public Map<String, Object> next() {
            Map<String, Object> map = excelIterator.next();
            //todo 根据档号上传文件 TODO,这里写死了
            String danghao = (String) map.get("档号");
            if (!StringUtils.isEmpty(danghao)) {
                String dirStr = tempDir + File.separator + "data";
                File fileDir = new File(dirStr, danghao);
                if (fileDir.exists()) {
                    File[] files = fileDir.listFiles(f -> !f.isDirectory());
                    if (files.length > 0) {
                        String id = UUIDUtils.getUUID();
                        try {
                            map.put(IdEntity.ID_COLUMN_NAME, id);
                            for (File file : files) {
                                //添加原文
                                fileService.addFile(new FileSystemFileResource(file), id);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
            return map;
        }
    }


    @Override
    public void writeData(String[] keys, Iterator<Map<String, Object>> data, String mine, OutputStream target) throws IOException {
        //创建Excel
        ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(target);
        //excel临时文件夹
        String excelFileDir = commonFileConfig.getFullDirPath("temp", "excel", null);
        File tempExcel = new File(excelFileDir, UUIDUtils.getUUID() + ".xls");
        //根据档号处理文件
        FileOutputStream fileOutputStream = new FileOutputStream(tempExcel);
        excelDataParser.writeData(keys, new ZipIterator(data, zipArchiveOutputStream), mine, fileOutputStream);
        fileOutputStream.close();
        ArchiveEntry archiveEntry = zipArchiveOutputStream.createArchiveEntry(tempExcel, excelName);
        zipArchiveOutputStream.putArchiveEntry(archiveEntry);
        //写excel
        FileInputStream fileInputStream = new FileInputStream(tempExcel);
        StreamUtils.copy(fileInputStream, zipArchiveOutputStream);
        zipArchiveOutputStream.closeArchiveEntry();
        //关闭临时文件流
        fileInputStream.close();
        //删除excel文件
        tempExcel.delete();
        //结束压缩
        zipArchiveOutputStream.finish();
    }

    class ZipIterator implements Iterator<Map<String, Object>> {
        final Iterator<Map<String, Object>> data;
        final ZipArchiveOutputStream zipArchiveOutputStream;

        public ZipIterator(Iterator<Map<String, Object>> data, ZipArchiveOutputStream zipArchiveOutputStream) {
            this.data = data;
            this.zipArchiveOutputStream = zipArchiveOutputStream;
        }

        @Override
        public boolean hasNext() {
            return data.hasNext();
        }

        @Override
        public Map<String, Object> next() {
            Map<String, Object> map = data.next();
            //这里写附件
            if (map.containsKey(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE)) {
                //主键
                String id = (String) map.get(IdEntity.ID_COLUMN_NAME);
                //档号
                String code = (String) map.get(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE);
                if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(code)) {
                    List<FileInfo> fileInfos = fileService.list(id);
                    if (!fileInfos.isEmpty()) {
                        try {
                            int count = 0;
                            //原文数据也不为空
                            for (FileInfo fileInfo : fileInfos) {
                                String fileName = "/data/" + code + "/" + count + "." + fileInfo.getSuffix();
                                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(fileName);
                                zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                                InputStream ips = fileService.fileStream(fileInfo.getId());
                                StreamUtils.copy(ips, zipArchiveOutputStream);
                                zipArchiveOutputStream.closeArchiveEntry();
                                ips.close();
                                count++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return map;
        }
    }

}
