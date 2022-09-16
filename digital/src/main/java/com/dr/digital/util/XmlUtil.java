package com.dr.digital.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.util.StringUtils;

public class XmlUtil {

    /**
     * 解析xml
     *
     * @param xmlPath xml地址
     * @return
     */
    public static Map<String, String> xmlParsing(String xmlPath) {
        // 获取SAX解析器
        Map<String,String> map = new HashMap<>();
        SAXBuilder builder = new SAXBuilder();

        File file = new File(xmlPath);
        // 获取文档
        Document doc = null;
        if (file.isFile()){
            try {
                doc = builder.build(new File(file.getAbsolutePath()));
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (doc!=null){
            // 获取根节点
            Element root = doc.getRootElement();
            // 获取根节点下所有的子节点， 也可以根据标签名称获取指定的直接点
            List<Element> list = root.getChildren();
            for (int x = 0; x < list.size(); x++) {
                Element e = list.get(x);
                // 获取元素的名称和里面的文本
                String name = e.getName();
                //获取节点中文名如：全宗
//            String key = "";
//            if (e.getAttributes().size()>0&&!StringUtils.isEmpty(e.getAttributes().get(0))){
//                key = e.getAttributes().get(0).getValue();
//            }
                //获取对应的值如:0073
                String value=null;
                if (e.getContent().size()>0&&!StringUtils.isEmpty(e.getContent().get(0))){
                    value = e.getContent().get(0).getValue();
                }
                map.put(name,value);
            }
        }
        return map;
    }

}