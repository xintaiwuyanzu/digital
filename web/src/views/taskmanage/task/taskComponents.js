//下面是头部的按钮
import search from "../../lib/components/search";
import fenFA from "../../lib/components/fenFA";

/**
 * 渲染在table操作一栏的控件
 * @type {*[]}
 */
export const columnParts = []
/**
 * 渲染在上面的控件
 * @type {*[]}
 */
export const headerParts = [
    //搜索按钮
    {component: search},
    //任务下发按钮
    {component: fenFA},
]