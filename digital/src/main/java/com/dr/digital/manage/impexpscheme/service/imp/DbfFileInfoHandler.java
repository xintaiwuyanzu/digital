package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.framework.common.file.FileResource;
import com.dr.framework.common.file.service.impl.DefaultFileInfoHandler;
import org.springframework.stereotype.Component;

@Component
public class DbfFileInfoHandler extends DefaultFileInfoHandler {

    public String fileMine(FileResource resource) {
        if (resource.getName().toLowerCase().contains(".dbf")) {
            return "application/x-dbf";
        }
        return super.fileMine(resource);
    }
}
