package com.dr.digital.configManager.bo;

import java.util.List;

/**
 * 判断是否存在该环节
 *
 * @author Mr.Zhu
 * @date 2022/8/21 - 13:57
 */
public interface LinkFlowPath {
    /**
     * 人工环节
     */
    String[] LinkFlowPath = {"RECEIVE", "SCANNING", "PROCESSING",
            "IMAGES", "WSSPLIT", "VOLUMES", "QUALITY", "RECHECK", "OVER"};
    /**
     * 人工环节中文
     */
    String[] LinkFlowPathChinese = {"任务登记", "图像扫描", "图像处理",
            "图像质检", "手动拆件", "档案著录", "初检", "复检", "数字化成果"};
    /**
     * 自动环节
     */
    String[] LinkFlowAutoPath = { "YUANWENTOJPG","OCR","CHAIJIAN","OFD","ZIPPACKET"};

    String RECEIVE = "RECEIVE";
    String RECEIVE_Chinese = "任务登记";

    String SCANNING = "SCANNING";
    String SCANNING_Chinese = "图像扫描";

    String PROCESSING = "PROCESSING";
    String PROCESSING_Chinese = "图像处理";

    String IMAGES = "IMAGES";
    String IMAGES_Chinese = "图像质检";

    String WSSPLIT = "WSSPLIT";
    String WSSPLIT_Chinese = "手动拆件";

    String VOLUMES = "VOLUMES";
    String VOLUMES_Chinese = "档案著录";

    String QUALITY = "QUALITY";
    String QUALITY_Chinese = "初检";

    String RECHECK = "RECHECK";
    String RECHECK_Chinese = "复检";

    String OVER = "OVER";
    String OVER_Chinese = "数字化成果";

    String YUANWENTOJPG = "YUANWENTOJPG";
    String YUANWENTOJPG_Chinese = "自动拆jpg";

    String OCR = "OCR";
    String OCR_Chinese = "ocr识别";

    String CHAIJIAN = "CHAIJIAN";
    String CHAIJIAN_Chinese = "自动拆件";

    String OFD = "OFD";
    String OFD_Chinese = "ofd转换";

    String ZIPPACKET = "ZIPPACKET";
    String ZIPPACKET_Chinese = "打包入库";
}
