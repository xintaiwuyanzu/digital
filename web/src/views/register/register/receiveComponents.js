import edit from "../../lib/components/edit";
import look from "../../lib/components/look";
//下面是头部的按钮
import add from "../../lib/components/add";
import batchAdd from "../../lib/components/batchAdd";
import search from "../../lib/components/search";
import submitting from "../../lib/components/submitting";
import deleter from "../../lib/components/deleter";
import impCatalogList from "../../lib/components/impCatalogList";
import threeInOne from "../../lib/components/threeInOne";
import pdfOfd from "../../lib/components/pdfOfd";
//import originalHook from "../../lib/components/originalHook";
//import impZipList from "../../lib/components/impZipList";

/**
 * 渲染在table操作一栏的控件
 * @type {*[]}
 */
export const columnParts = [
    //编辑按钮
    {
        component: edit,
        //控件宽度，可以不写，默认30，只有在列表渲染的时候生效，用来控制操作栏的显示宽度，一般按照一个字15的宽度计算
        width: 30
    },
    {
        component: look,
        //控件宽度，可以不写，默认30，只有在列表渲染的时候生效，用来控制操作栏的显示宽度，一般按照一个字15的宽度计算
        width: 30
    },
]
/**
 * 渲染在上面的控件
 * @type {*[]}
 */
export const headerParts = [

    //批量添加按钮
    {component: batchAdd},
    //添加按钮
    {component: add},
    //搜索按钮
    {component: search},
    //提交按钮
    {component: submitting},
    //目录导入按钮
    {component: impCatalogList},
    //原文zip上传按钮
    //{component: impZipList},
    //原文挂接
    //{component: originalHook},
    //三合一按钮（拆分，识别，拆件）
    {component: threeInOne},
    //删除按钮
    {component: deleter},
]
