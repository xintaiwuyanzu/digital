import splitWs from "../lib/components/splitWs";
//import splitWsTagging from "../lib/components/splitWsTagging";
//下面是头部的按钮
import search from "../lib/components/search";
import submitting from "../lib/components/submitting";
import goBack from "../lib/components/goBack";
import ocrSplit from "../lib/components/ocrSplit";


/**
 * 渲染在table操作一栏的控件
 * @type {*[]}
 */
export const columnParts = [
    //手动拆分按钮
    {
        component: splitWs,
        //控件宽度，可以不写，默认30，只有在列表渲染的时候生效，用来控制操作栏的显示宽度，一般按照一个字15的宽度计算
        width: 30
    },
    //手动拆分标注按钮
    /*{
        component: splitWsTagging,
        width: 30
    },*/
]
/**
 * 渲染在上面的控件
 * @type {*[]}
 */
export const headerParts = [
    //搜索按钮
    {component: search},
    //拆件
    {component: ocrSplit},
    //提交按钮
    {component: submitting},
    //退回按钮
    {component: goBack},
]