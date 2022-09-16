import abstractFormIndex from "./abstractFormIndex";
import resultFormIndex from "./resultFormIndex";
import taskFormIndex from "./taskFormIndex";

/**
 * 创建单个组件
 */
const createSingle = (h, c, {currentRow, currentSelect}) => {
    return <c currentSelect={currentSelect} currentRow={currentRow}/>
}

/**
 * 根据参数创建所有的头部控件
 */
const createHeaderComponents = (h, props, headerParts) => {
    return headerParts.map(p => createSingle(h, p.component, props))
}
/**
 * 根据参数创建所有的列控件
 */
const createColumnComponents = (h, {currentRow, currentSelect}, columnParts) => {
    return {
        default: s => {
            return columnParts.map(a => {
                const c = a.component
                if (c) {
                    return <c row={s.row} currentSelect={currentSelect} currentRow={currentRow}/>
                }
            })
        }
    }
}
/**
 * 动态创建列
 * @param h
 * @param listFields
 * @returns {*}
 */
const createColumns = (h, listFields, context) => {
    return listFields.map(f => {
        if (f.meta && f.meta.dict) {
            //字典转换
            const formatter = (r, i, c) => {
                return context.$options.filters.dict(c, f.meta.dict)
            }
            return (<el-table-column label={f.name} prop={f.code}
                                     formatter={formatter}
                                     align="center"
                                     min-width={f.remarks}
                                     show-overflow-tooltip/>)
        } else {
            return <el-table-column label={f.name} prop={f.code}
                                    align="center"
                                    min-width={f.remarks}
                                    show-overflow-tooltip/>
        }
    })
}

const createColumnsEdit = (h, editWidth, columnSlots) => {
    if (editWidth > 20) {
        return (
            <el-table-column label="操作" align="center" class-name="editColumn"
                             width={editWidth}
                             fixed="right"
                             scopedSlots={columnSlots}>
            </el-table-column>
        )
    } else {
        return null
    }


}


/**
 * hoc 方法，动态创建表单渲染列表
 * @param h
 * @param query 默认查询条件
 * @param defaultForm 默认表单属性
 */
const createFormIndex = (h, {columnParts = [], headerParts = [], type}, is) => {
    let jCLei = '';
    if (is == '1') { //1代表列表页面
        jCLei = abstractFormIndex
    } else if (is == '2') { // 2代表 结果列表
        jCLei = resultFormIndex
    } else if (is == '3') { //3代表 任务列表
        jCLei = taskFormIndex
    }
    return {
        extends: jCLei,
        data() {
            return {
                //查询条件
                query: [
                    //状态作为查询条件
                    {key: 'status_info', type: 'i', value: type}
                ],
                defaultForm: {status_info: type}
            }
        },
        render(h) {
            const props = {
                currentRow: this.currentRow,
                currentSelect: this.currentSelect
            }
            const headers = createHeaderComponents(h, props, headerParts)
            let content = '加载中...'
            const {listFields} = this
            if (listFields.length > 0) {
                const filterColumnParts = columnParts.filter(c => {
                    if (c.target) {
                        return c.target !== this.categoryType
                    } else {
                        return true
                    }
                })
                const columnSlots = createColumnComponents(h, props, filterColumnParts)
                //计算操作栏宽度
                const editWidth = filterColumnParts
                    .map(c => c.width ? c.width : 30).reduce((t, a) => t + a + 6, 0) + 20
                const columns = createColumns(h, listFields, this)
                const columnsEdit = createColumnsEdit(h, editWidth, columnSlots)
                content = (
                    <div class="index_main">
                        <div class="table-container">
                            <el-table ref="multipleTable" data={this.data}
                                      on-row-click={this.rowClick}
                                      on-selection-change={this.select}
                                      border height="100%"
                                      v-loading={this.loading}
                                      element-loading-text="拼命加载中..."
                                      row-class-name={this.rowColor}>
                                <el-table-column fixed="left" type="selection" width="50" align="center">
                                    {/*       {{this.row.TRANSITION_STATE|dict('transition.state') }}*/}
                                </el-table-column>
                                <column-index page={this.page}/>
                                {columns}
                                {columnsEdit}
                            </el-table>
                        </div>
                        <page page={this.page} layout="total, prev, pager, next, jumper"
                              on-current-change={index => this.loadData({index})}/>
                    </div>)
            }
            return (<section key={this.key}>
                <nac-info title={this.title}>
                    {headers}
                </nac-info>
                {content}
            </section>)
        }
    }
}
export default createFormIndex