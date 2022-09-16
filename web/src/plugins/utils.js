import dayjs from 'dayjs'
import scrollto from 'vue-scrollto'

const fmtDate = (v, fmt) => {
    if (!v) {
        return v
    }
    try {
        return dayjs(v).format(fmt)
    } catch (e) {
        return v
    }
}
export default (vue, router, store) => {
    vue.use(scrollto)
    vue.prototype.$moment = dayjs
    vue.filter('null', (v) => (v && v !== 'null') ? v : '')
    vue.filter('date', (v, fmt) => fmtDate(v, fmt ? fmt : 'YYYY-MM-DD'))
    vue.filter('datetime', (v, fmt) => fmtDate(v, fmt ? fmt : 'YYYY-M-D H:m:s'))
    vue.directive('focus', {
        inserted: function (el) {
            el.childNodes.forEach(e => {
                if (e.tagName === 'INPUT' || e.tagName === 'input') {
                    e.focus()
                }
            })
        }
    })
    const archiveTypes = [
        {label: '案卷', id: '1'},
        {label: '文件', id: '0'},
        {label: '件盒', id: '2'}
    ]
    const impSchema = [
        {label: '导入', id: '1'},
        {label: '导出', id: '2'}
    ]
    const fileType = [
        {label: 'excel', id: '1'},
        {label: 'xml', id: '2'}
    ]
    const mineType = [
        {label: 'excel', id: 'application/vnd.ms-excel'},
        {label: 'xml', id: 'application/xml'},
        {label: 'dbf', id: 'application/x-dbf'},
        {label: 'access', id: 'application/x-msaccess'},
        {label: 'json', id: 'application/json'},
    ]
    const dicts = {archiveTypes}
    const appraisalType = [
        {label: '密级鉴定', id: 'securityLevel'},
        {label: '保管期限鉴定', id: 'saveTerm'},
        {label: '开放范围鉴定', id: 'openScope'},
        {label: '价值鉴定', id: 'worth'},
        {label: '销毁鉴定', id: 'destruction'}
    ]
    const securityLevel = [
        {label: '国内', id: '1'},
        {label: '内部', id: '2'},
        {label: '秘密', id: '3'},
        {label: '机密', id: '4'},
        {label: '绝密', id: '5'}
    ]
    const openScope = [
        {label: '开放', id: '1'},
        {label: '不开放', id: '2'}
    ]
    const formDisplayType = [
        {label: '列表页面', id: 'list'},
        {label: '添加页面', id: 'form'},
        {label: '查询表单', id: 'search'},
        {label: '质检表单', id: 'quality'},
        {label: '任务列表', id: 'task'},
        {label: '结果列表', id: 'result'}
    ]
    const testType = [
        {label: '完整性', id: 'INTEGRITY'},
        {label: '真实性', id: 'AUTHENTICITY'},
        {label: '安全性', id: 'SECURITY'},
        {label: '可用性', id: 'USABILITY'}
    ]
    /**
     * 虚拟库房：库房类型
     */
    const locType = [
        {label: '文字档案', id: '1'},
        {label: '图形档案', id: '2'},
        {label: '声像档案', id: '3'},
    ]
    const articleStatus = [
        {label: '下线', id: '0'},
        {label: '上线', id: '1'},
    ]
    dicts['cms.articleStatus'] = articleStatus
    dicts['impexp.schemeType'] = impSchema
    dicts['impexp.fileType'] = fileType
    dicts['impexp.mineType'] = mineType
    dicts['archive.appraisalType'] = appraisalType
    dicts['archive.securityLevel'] = securityLevel
    dicts['archive.openScope'] = openScope
    dicts['archive.testType'] = testType
    dicts['loc.type'] = locType
    dicts.formDisplayType = formDisplayType
    const impSourceTypes = [
        {label: '立档单位', id: '1'},
        {label: '打包接收', id: '2'},
        {label: '历史数据', id: '3'},
        {label: '数字化成果', id: '4'},
        {label: '征集档案', id: '5'},
        {label: '寄存档案', id: '6'}
    ]
    const checkName = [
        {label: '著录项目数据长度检测', id: 'AUTHENTICITY_LENGTH'},
        {label: '著录项目数据类别、字段格式检测', id: 'AUTHENTICITY_TYPE'},
        {label: '设定值域的著录项目值域符合度检测', id: 'AUTHENTICITY_RANGE'},
        {label: '著录项目数椐合理性检测，著录项目数据范围检测', id: 'AUTHENTICITY_RATIONALITY'},
        {label: '著录项目数据包含特殊字符检测', id: 'AUTHENTICITY_SPECIALCHARACTERS'},
        {label: '目录是否关联电子文件内容', id: 'AUTHENTICITY_ISRELATIONFILE'},
        {label: '电子文件内容数据大小的一致性检测', id: 'AUTHENTICITY_FILESIZE'},
        {label: '电子文件内容数据电子属性的一致性检测', id: 'AUTHENTICITY_FILEPROPERTY'},
        {label: '电子文件生效信息的有效性检测', id: 'AUTHENTICITY_FILEEFFECTIVEDATE'},
        {label: '电子文件目录必填著录项目检测', id: 'INTEGRITY_REQUIRED'},
        {label: '归档信息包元数据完整性检测', id: 'INTEGRITY_METADATA'},
        {label: '案卷级归档数椐包中卷内文件数量和实际条目数量的相符性检测', id: 'INTEGRITY_INNERFILENUM'},
        {label: '文件级归档信息包中文件数量和电子文件数的相符性检测', id: 'INTEGRITY_FILENUM'},
        {label: '归档信息包中目录数据的可读性检测', id: 'USABILITY_READ'},
    ]
    dicts['archive.checkName'] = checkName
    dicts.fieldDisplayType = [
        {label: '文本', id: 'input'},
        {label: '字典', id: 'dict'},
        {label: '数字', id: 'int'},
    ]
    dicts.impSourceTypes = impSourceTypes
    store.commit('dictLoaded', dicts)
}