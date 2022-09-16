//下面是头部的按钮
import resultTest from "@/views/lib/components/resultTest";
import search from "../lib/components/search";
import goBack from "../lib/components/goBack";
import seeOFD from "../lib/components/seeOFD";
import onlineHandover from "../lib/components/onlineHandover";
import packertAll from "../lib/components/packertAll";

/**
 * 渲染在table操作一栏的控件
 * @type {*[]}
 */
export const columnParts = [
    //封包
    //{component: packert, width: 60},
    {component: seeOFD, width: 60},
]
/**
 * 渲染在上面的控件
 * @type {*[]}
 */
export const headerParts = [
    //跳转到成果检测页面
    {component: resultTest},
    //搜索按钮
    {component: search},
    //图片合并转OFD
    //{component: splitAndMerge},
    //图片合并转pdf
    //{component: mergePhotoToPDF},
    //pdf转OFD
    //{component: PdfToOfd},
    //目录导出按钮
    //{component: expFilelist},
    //批量打包
    {component: packertAll},
    //在线移交
    {component: onlineHandover},
    //退回按钮
    {component: goBack},


    //ofd工具下载按钮
    // {component: ofd},
]
