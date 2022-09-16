package com.dr.digital.ofd.service.impl;

import com.dr.digital.ofd.service.PacketRecordService;
import com.dr.digital.packet.entity.PacketRecord;
import com.dr.digital.packet.entity.PacketRecordInfo;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class PacketRecordServiceImpl implements PacketRecordService {

    @Autowired
    CommonMapper commonMapper;

    /**
     * 删除打包记录以及打包文件
     * @param id
     */
    @Override
    public void removePacket(String id) {

        List<PacketRecord> packetRecords = commonMapper.selectByQuery(SqlQuery.from(PacketRecord.class).in(PacketRecordInfo.ID, id.split(",")));
        String property = System.getProperty("user.dir");
        for (PacketRecord packetRecord : packetRecords) {
            String path = property + File.separator + "zipPack" + File.separator + packetRecord.getFondCode();
            deleteDir(new File(path));
            commonMapper.deleteById(PacketRecord.class, packetRecord.getId());
        }

    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
