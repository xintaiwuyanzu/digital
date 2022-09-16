import abstractArchiveLib from "./abstractArchiveLib";
import createFormIndex from "../archiveFormIndex";

/**
 * 创建列表显示页面
 * @param h createElement
 * @param title 显示标题
 * @param formId 表单Id
 * @param id id，用来设置refs
 * @param params index创建参数
 * @returns {JSX.Element}
 */
const createIndex = (h, {title, formId, id}, params, is) => {
    const formIndex = createFormIndex(h, params, is)
    return <formIndex title={title} formId={formId} categoryType={id}/>
}
/**
 * 创建渲染列表对象
 */
const createLib = (params, is) => {
    return {
        //继承抽象父类，父类实现相关的逻辑，字类实现渲染相关
        extends: abstractArchiveLib,
        render(h) {
            const {archives} = this
            let content
            if (archives.length > 0) {
                // let arc =new Array();
                // if(this.category.code.toUpperCase().indexOf('WS') >= 0){
                //     for(let i=0;i<archives.length;i++){
                //         if(archives[i].id==='ARC'){
                //             arc.push(archives[i])
                //         }
                //     }
                // }
                content = archives.map(a => createIndex(h, a, params, is))
            } else {
                content = (<span>请选择左侧全宗门类树或者指定的分类没有配置元数据</span>)
            }
            return (
                <section>
                    <section class="archiveLibIndex">
                        <el-card class="fondTree" shadow="hover">
                            <fond-tree showHide={true} autoSelect on-check={this.check} on-fond={d => this.fond = d}
                                       fondId={this.fondId}
                                       ref="fondTree" withPermission={true}/>
                        </el-card>
                        <el-card class="tables" shadow="hover">
                            {content}
                        </el-card>
                    </section>
                </section>
            )
        }
    }
}
export default createLib
