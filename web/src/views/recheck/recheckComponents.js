import recheck from "../lib/components/recheck";
//下面是头部的按钮
import search from "../lib/components/search";
import submitting from "../lib/components/submitting";
import goBack from "../lib/components/goBack";
import inspectionReport from "@/views/lib/components/inspectionReport";

/**
 * 渲染在table操作一栏的控件
 * @type {*[]}
 */
export const columnParts = [
    //复检按钮
    {
        component: recheck,
        //控件宽度，可以不写，默认30，只有在列表渲染的时候生效，用来控制操作栏的显示宽度，一般按照一个字15的宽度计算
        width: 30
    },
]
/**
 * 渲染在上面的控件
 * @type {*[]}
 */
export const headerParts = [
    // //质检报告
    // {component: inspectionReport},
    //搜索按钮
    {component: search},
    //提交按钮
    {component: submitting},
    //退回按钮
    {component: goBack},
]