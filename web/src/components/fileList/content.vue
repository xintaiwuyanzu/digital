<template>
    <section style="height: 100%">
        <div class="table-container" style="height: 100%">
            <el-table :data="pdfList" border height="100%" v-loading="loading" element-loading-text="加载中...">
                <el-table-column label="排序" align="center" width="50">
                    <template slot-scope="scope">
                        {{ scope.$index + 1 }}
                    </template>
                </el-table-column>
                <el-table-column label="文件名称" prop="name" show-overflow-tooltip align="center" header-align="center"/>
                <el-table-column label="文件类型" prop="suffix" width="100px" align="center" header-align="center">
                </el-table-column>
                <el-table-column label="上传日期" prop="saveDate" width="100px" align="center"
                                 header-align="center">
                    <template slot-scope="scope">
                        {{ scope.row.saveDate|date }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" align="center" header-align="center" width="180">
                    <template slot-scope="scope">
                        <el-link type="success" v-if="print" @click="toPrint(scope.row)">打 印</el-link>
                        <span v-if="print"> | </span>
                        <el-link type="success" @click="downLoad(scope.row)">预 览</el-link>
                        <span v-if="deleter"> | </span>
                        <el-link type="danger" v-if="deleter" @click="deletePdf(scope.row)">删 除</el-link>
                        <span v-if="doTransform(scope.row)&&transform"> | </span>
                        <el-link type="warning" @click="fileToPdf(scope.row)"
                                 v-if="doTransform(scope.row)&&transform">
                            转pdf
                        </el-link>
                    </template>
                </el-table-column>
            </el-table>
            <el-dialog
                    center
                    fullscreen
                    :visible.sync="preview"
                    append-to-body
                    customClass="customWidth">
                <div v-if="showPdf">
                    <iframe :src="src" frameborder="0" ref="iframe" style="width: 100%; height:88vh"></iframe>
                </div>
                <div v-if="!showPdf">
                    <pdf-viewer :src="src" style="width: 100%; height:88vh"/>
                </div>
            </el-dialog>
        </div>
    </section>
</template>

<script>
    import indexMixin from '@/util/indexMixin'

    export default {
        mixins: [indexMixin],
        props: {
            //业务外键Id
            refId: {type: String},
            useType: {type: Boolean},
            refType: {default: 'default'},
            groupCode: {default: 'default'},
            //是否显示打印按钮
            print: {default: false},
            //是否显示删除按钮
            deleter: {default: true},
            //是否显示转换按钮
            transform: {default: true},
        },
        data() {
            return {
                src: '',
                preview: false,
                pdfList: [],
                currentPerson: Object,
                showPdf: false,
                readState: false
            }
        },
        watch: {
            refId() {
                this.loadData()
            }
        },
        methods: {
            $init() {
                this.loadData();
                this.getCurrentPerson();
            },
            downLoad(row) {
                if (!this.useType) {
                    this.$message.warning("此门类未添加利用权限，请联系管理员设置！");
                    return;
                }
                this.$http.post("/login/info", {}).then(({data}) => {
                    if (data.success) {
                        this.$http.post(`/readRole/usePower`, {
                            personId: data.data.id,
                            archiveId: row.refId
                        }).then(({data}) => {
                            if (data.success) {
                                if (row.suffix.toUpperCase() === "PDF") {
                                    this.showPdf = true
                                    if (this.currentPerson.userCode === "admin") {
                                        this.src = `api/files/downLoad/${row.id}?download=false`
                                        this.preview = true
                                    } else {
                                        this.src = `/api/watermark/showView?refId=${row.id}`
                                        this.preview = true
                                    }
                                } else {
                                    this.showPdf = false
                                    window.open(`api/files/downLoad/${row.id}?download=false`, "_blank")
                                }
                            } else {
                                this.$message.error(data.message)
                                return
                            }
                        })
                    } else {
                        this.$message.error(data.message)
                    }
                })
            },
            toPrint(row) {
                this.downLoad(row)
                this.$emit("toPrint", row)
            },
            deletePdf(row) {
                this.$confirm('此操作将进行删除信息, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$http.post('files/delete/' + row.id).then(({data}) => {
                        if (data.success) {
                            this.loadData()
                            this.$message.success("删除成功")
                        }
                    })
                })
            },
            fileToPdf(row) {
                this.loading = true
                this.$http.post('formatconversion/fileToPdf/', {fileId: row.id}).then(({data}) => {
                    if (data && data.success) {
                        this.$message.success(data.data)
                        this.loadData()
                    }
                    this.loading = false
                })
            },
            async loadData() {
                this.loading = true
                if (this.refId) {
                    const {data} = await this.$http.post('files/list', {
                        refId: this.refId,
                        refType: this.refType,
                        groupCode: this.groupCode
                    })
                    this.loading = false
                    if (data.success) {
                        this.loading = false
                        if (data && data.success) {
                            this.pdfList = data.data
                            this.$forceUpdate()
                        }
                    }
                }
            },
            apiPath() {
                return "/fileBatch"
            },
            handleTableSelect(val) {
                this.prefiling = val
            },
            doTransform(row) {
                return ['XLSX', 'XLS', 'DOC', 'DOCX', 'TXT', 'PPT', 'PPTX', 'PNG', 'JPG', 'JPEG', 'TIF'].indexOf(row.suffix.toUpperCase()) != -1
            },
            async getCurrentPerson() {
                const {data} = await this.$http.post("/login/info")
                if (data.success) {
                    this.currentPerson = data.data
                }
            }
        }
    }
</script>
<style lang="scss">
    .el-dialog__body {
        padding-top: 0;
    }
</style>







