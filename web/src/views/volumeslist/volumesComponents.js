import volumeEntry from "../lib/components/volumeEntry";
//下面是头部的按钮
import search from "../lib/components/search";
import submitting from "../lib/components/submitting";
import goBack from "../lib/components/goBack";
import splitAndMerge from "../lib/components/splitAndMerge";

/**
 * 渲染在table操作一栏的控件
 * @type {*[]}
 */
export const columnParts = [
    //编辑按钮
    {
        component: volumeEntry,
        //控件宽度，可以不写，默认30，只有在列表渲染的时候生效，用来控制操作栏的显示宽度，一般按照一个字15的宽度计算
        width: 60
    },
]
/**
 * 渲染在上面的控件
 * @type {*[]}
 */
export const headerParts = [
    //搜索按钮
    {component: search},
    //图片合并转OFD
    {component: splitAndMerge},
    //提交按钮
    {component: submitting},
    //退回按钮
    {component: goBack},
]