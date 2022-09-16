<template>
    <section>
        <el-link type="primary" @click="uniquenessJudge">复检</el-link>
        <el-link type="primary" @click="resultInspection">质检</el-link>
        <el-drawer
            :visible.sync="drawer"
            :direction="direction"
            size="50%"
            :before-close="handleClose">
            <el-row :gutter="10">
                <el-col :span="11">
                    <div class="grid-content bg-purple" style="text-align: center">
                        <span style="font-size: 20px">原文检测</span>
                        <el-table
                            :data="fileDetail"
                            style="width: 100%">
                            <el-table-column
                                prop="fileName"
                                label="原文名称"
                                width="100"
                                align="center">
                            </el-table-column>
                            <el-table-column
                                prop="filePath"
                                label="原文地址"
                                width="150"
                                align="center">
                            </el-table-column>
                            <el-table-column
                                prop="fileSize"
                                label="原文大小"
                                align="center">
                            </el-table-column>
                            <!--                <el-table-column-->
                            <!--                    prop="filePower"-->
                            <!--                    label="分辨率" align="center">-->
                            <!--                </el-table-column>-->
                            <el-table-column
                                prop="fileYs"
                                label="页数"
                                align="center">
                            </el-table-column>
                            <el-table-column
                                prop="initial"
                                label="初始格式"
                                align="center">
                            </el-table-column>
                            <el-table-column
                                prop="target"
                                label="目标格式"
                                align="center">
                            </el-table-column>
                        </el-table>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="grid-content bg-purple-light">
                        <span style="font-size: 20px">目录检测</span>
                        <el-table
                            :data="directoryDetection ">
                            <el-table-column
                                prop="detectionObject"
                                label="检测对象"
                                align="center"
                            >
                            </el-table-column>
                            <el-table-column
                                prop="requirements"
                                label="检测规则"
                                align="center"
                            >
                            </el-table-column>
                            <el-table-column
                                prop="detectionStatus"
                                label="检测状态"
                                align="center">
                            </el-table-column>
                        </el-table>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div class="grid-content bg-purple"></div>
                    <span style="font-size: 20px">原文目录对比</span>
                    <el-table
                        :data="comparison"
                        style="width: 100%">
                        <el-table-column
                            prop="comparisonObject"
                            label="对比对象"
                            align="center"
                        >
                        </el-table-column>
                        <el-table-column
                            prop="comparisonResults"
                            label="对比结果"
                            align="center"
                        >
                        </el-table-column>
                    </el-table>
                </el-col>
            </el-row>
        </el-drawer>
    </section>
</template>

<script>
import abstractColumnComponent from "./abstractColumnComponent";

export default {
    extends: abstractColumnComponent,
    name: 'recheck',
    data() {
        return {
            drawer: false,
            direction: 'ttb',
            fileDetail: [],
            directoryDetection: [],
            comparison: [
                {comparisonObject: '档号', comparisonResults: '档号相同'},
                {comparisonObject: '年度', comparisonResults: '年度相同'},
                {comparisonObject: '页数', comparisonResults: '页数相同'},
            ],
          original_format_data: [{
            value: '1',
            label: 'PDF'
          }, {
            value: '2',
            label: 'TIF'
          },
            {
              value: '3',
              label: '纸质'
            }],
            target_format_data: [
                {value: '2', label: 'OFD'}
            ],
        };
    },
    methods: {
        $init() {
            this.initJpgData();
        },
        initial() {
            let type = 1;
            if (this.parentIndex) {
                type = 2
                this.message.formId = this.childrenIndex.formId;
                this.message.ajFormId = this.formId;
                this.message.ajFondId = this.fond.id;
            } else {
                this.message.formId = this.formId;
                this.message.ajFormId = this.formId;
                this.message.ajFondId = this.fond.id;
            }
            this.message.rtype = this.row.status_info//
            this.message.id = this.row.id;
            this.message.dangHao = this.row.archival_code;
            this.message.fondId = this.fond.id;
            this.message.registerId = this.fond.registerId;
            this.message.type = type;
            this.message.code = this.category.code;

            const nowPage = (parseInt(this.eventBus.page.index) - 1) * parseInt(this.eventBus.page.size)
            const _query = this.eventBus.getQueryByQueryType('query')
            const t = this.IfStatus(this.eventBus.defaultForm.status_info)
            this.$router.push({
                path: '../../recheck/inspect',
                query: {
                    message: this.message,
                    ajRow: this.row,
                    type: t,
                    index: parseInt(this.row.$index) + nowPage + 1,
                    _query
                }
            })
        },
        //存放参数
        obj(){
            let obj = new Object();
            obj.formDefinitionId = this.formId
            obj.archivesId = this.row.id
            return obj
        },
        uniquenessJudge(){
            const obj = this.obj()
            this.$http.post("/manage/formData/uniquenessJudge", obj).then(({data}) => {
                if (data.data.success) {
                    //成功后跳转到加工页面
                    this.initial()
                } else {
                    this.$message.error(data.data.message);
                }
            })
        },
        handleClose(done) {
            this.$confirm('确认关闭？')
                .then(_ => {
                    done();
                })
                .catch(_ => {
                });
        },
        async initJpgData() {
            const {data} = await this.$post("/jpgQueue/getArchiveCodeByJpg", {archiveCode: this.row.archival_code})
            this.fileDetail = data.data
            if (this.fileDetail[0] && this.fileDetail[0].formDefinitionId) {
                const {data} = await this.$post("/register/getFormDefinitionIdByRegister", {formDefinitionId: this.fileDetail[0].formDefinitionId})
                if (data && data.success) {
                    this.original_format_data.forEach(i => {
                        if (i.value == data.data[0].original_format) {
                            this.fileDetail[0].initial = i.label
                        }
                    })
                    this.target_format_data.forEach(i => {
                        if (i.value == data.data[0].target_format) {
                            this.fileDetail[0].target = i.label
                        }
                    })
                } else {
                    this.$message.error("找不到表单id对应的批次")
                }
            }
        },
        async initDirectory() {
            let archivalCode = {detectionObject: "档号", requirements: "不能有中文", detectionStatus: ""}
            let fondsIdentifier = {detectionObject: "全宗", requirements: "四位数字", detectionStatus: ""}
            let year = {detectionObject: "年度", requirements: "四位数字", detectionStatus: ""}
            let pageNum = {detectionObject: "页数", requirements: "大于零的数字", detectionStatus: ""}
            if (this.row.archival_code) {
                //检验是否有中文或非法字符
                archivalCode.detectionStatus = this.checkJudge(this.row.archival_code)
            } else {
                archivalCode.detectionStatus = "检测未通过，档号为空"
            }

            if (this.row.fonds_identifier) {
                //检验是否有中文
                fondsIdentifier.detectionStatus = this.checkNum(this.row.fonds_identifier)
            } else {
                fondsIdentifier.detectionStatus = "检测未通过，全宗为空"
            }

            if (this.row.archivers_year) {
                //检验是否有中文或非法字符
                year.detectionStatus = this.checkNum(this.row.archivers_year)
            } else {
                year.detectionStatus = "检测未通过，年度为空"
            }

            if (this.row.total_number_of_pages) {
                //检验是否大于0的数字
                if (!new RegExp(/^[0-9]+$/).test(this.row.total_number_of_pages)) {
                    pageNum.detectionStatus = "检测未通过，页数应该为数字"
                } else if (this.row.total_number_of_pages <= 0) {
                    pageNum.detectionStatus = "检测未通过，页数应该大于0"
                } else {
                    pageNum.detectionStatus = "检测通过"
                }
            } else {
                pageNum.detectionStatus = "检测未通过，页数不存在"
            }
            this.directoryDetection = []
            this.directoryDetection.push(archivalCode, fondsIdentifier, year, pageNum)

        },
        //判断是否是中文
        checkJudge(s) {
            let regAccountCN = /[\u4e00-\u9fa5]+/g
            if (regAccountCN.test(s)) {
                return "检测未通过，档号含中文"
            }
            return "检测通过"
        },
        //是否为四位数字
        checkNum(s) {
            if (s.length !== 4) return "检测未通过，长度应为四位"
            let numReg = new RegExp(/^[0-9]+$/)
            if (!numReg.test(s)) {
                return "检测未通过，应该为四位数字"
            }
            return "检测通过"
        },
        //原文目录对比
        comparisonResult() {
            let archiveCode = {comparisonObject: '档号', comparisonResults: '档号相同'}
            let fondCode = {comparisonObject: '全宗', comparisonResults: '全宗相同'}
            let pageNum = {comparisonObject: '页数', comparisonResults: '页数相同'}
            if (this.fileDetail.length > 0) {
                if (!this.row.archival_code && !this.fileDetail[0].archiveCode || this.row.archival_code !== this.fileDetail[0].archiveCode) {
                    archiveCode = {comparisonObject: '档号', comparisonResults: '档号不同'}
                }
                if (!this.row.fonds_identifier || this.row.fonds_identifier !== this.fileDetail[0].fondCode) {
                    fondCode = {comparisonObject: '全宗', comparisonResults: '全宗不同'}
                }
                if (!this.row.total_number_of_pages || this.row.total_number_of_pages !== this.fileDetail[0].fileYs) {
                    pageNum = {comparisonObject: '页数', comparisonResults: '页数不同'}
                }
                this.comparison = []
                this.comparison.push(archiveCode, fondCode, pageNum)
                this.drawer = true
            } else {
                this.$message.error("找不到jpg队列")
            }
        },
        resultInspection() {
            this.initDirectory()
            this.comparisonResult()
        }
    },
}
</script>