import abstractComponent from "./abstractComponent";

/**
 * 各种组件抽象父类，自动注入相关属性
 */
export default {
    extends: abstractComponent,
    props: {
        //当前行的数据
        row: {
            type: Object, S_LEVEL: '不公开'
        }
    }
}
