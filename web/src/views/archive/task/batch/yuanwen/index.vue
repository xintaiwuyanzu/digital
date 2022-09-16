<template>
    <section>
        <nac-info title="拆分记录">
            <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
                <el-form-item label="记录名称:">
                    <el-input v-model="searchForm.batchName" placeholder="请输入记录名称" clearable/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="search" size="mini">搜 索</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table ref="applyTable" :data="data" border height="100%" highlight-current-row>
                    <column-index :page="page"/>
                    <el-table-column label="记录名称" prop="batchName" show-overflow-tooltip align="center"/>
                    <el-table-column label="开始时间" prop="startDate"
                                     :formatter="dateFormatter"
                                     show-overflow-tooltip
                                     align="center"/>
                    <el-table-column label="结束时间" prop="endDate"
                                     :formatter="dateFormatter"
                                     show-overflow-tooltip
                                     align="center"/>
                    <el-table-column label="状态" prop="status"
                                     width="60"
                                     :formatter="(r,c,cell)=>cell==='1'?'成功':'失败'"
                                     show-overflow-tooltip
                                     align="center"/>
                    <el-table-column label="备注" prop="beizhu"
                                     width="120"
                                     show-overflow-tooltip
                                     align="center"/>
                    <el-table-column label="操作" align="center" header-align="center" width="210">
                        <template slot-scope="scope">
                            <el-link type="primary" @click="download(scope.row)">下载</el-link>
                            |
                            <el-link type="danger" @click="remove(scope.row.id)">删 除</el-link>
                            |
                            <el-link type="primary" @click="detail(scope.row.id)">详 情</el-link>
                            |
                            <el-link type="primary" @click="showDialog(scope.row)">检测结果</el-link>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <el-pagination
                    @current-change="index=>loadData({pageIndex:index-1},this.searchForm)"
                    :current-page.sync="page.index"
                    :page-size="page.size"
                    layout="total, prev, pager, next,jumper"
                    :total="page.total">
            </el-pagination>
            <el-dialog width="60%" title="检查报告" :visible.sync="dialogShow" append-to-body>
                <el-table :data="data1" border class="table-container">
                    <el-table-column prop="order" label="排序" width="80" header-align="center" align="center">
                        <template slot-scope="scope">
                            {{ scope.$index + 1 }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="archival_code" label="档号" show-overflow-tooltip align="center"/>
                    <el-table-column prop="createDate" label="检测时间" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="150px">
                        <template slot-scope="scope">
                            {{ scope.row.createDate|datetime }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="imgYeShu" label="图像页数" show-overflow-tooltip align="center"/>
                    <el-table-column prop="split" label="检测结果" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="80px">
                        <template slot-scope="scope">
                            {{ scope.row.split|dict({'0': '失败', '1': '成功'}) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="dHMs" label="说明" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="220px"/>
                </el-table>
                <div slot="footer" class="dialog-footer">
                    <el-button type="info" @click="dialogShow = false" class="btn-cancel">取 消</el-button>
                </div>
            </el-dialog>
        </div>
    </section>
</template>
<script>
    import indexMixin from '@/util/indexMixin'

    export default {
        mixins: [indexMixin],
        data() {
            return {
                loading: false,
                searchForm: {
                    batchName: "",
                },
                data: [],
                dialogShow: false,
                data1: [],
            }
        },
        methods: {
            dateFormatter(r, c, cell) {
                if (cell) {
                    return this.$moment(cell).format('YYYY-MM-DD HH:mm:ss')
                }
                return '-'
            },
            async loadData(params) {
                this.loading = true
                const {data} = await this.$post('/batch/page', {batchType: 'IMP_YUANWEN', ...params})
                this.data = data.data.data
                this.page.index = data.data.start / data.data.size + 1
                this.page.size = data.data.size
                this.page.total = data.data.total
                this.loading = false
            },
            remove(id) {
                this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    let ids = []
                    if (id === true) {
                        ids = [...this.selectIds]
                    } else if (id) {
                        ids = [id]
                    }
                    if (ids.length > 0) {
                        this.loading = true
                        //删除detail的方法
                        this.$http.post('/batch/deleteDetail', {batchId: id})
                        this.$http.post('/batch/delete', {id: ids.join(','), type: "IMP_YUANWEN"})
                            .then(({data}) => {
                                if (data.success) {
                                    this.$message.success('删除成功！')
                                    this.selectIds = []
                                    this.loadData()
                                } else {
                                    this.$message.error(data.message)
                                    this.loading = false
                                }
                            })
                    } else {
                        this.$message.warning('请选择要删除的数据列！')
                    }
                })
            },
            async download(row) {
                if (row.fileLocation != null) {
                    const {data} = await this.$post('/download/getUploadDownLoadPath', {
                        fileFullPath: row.fileLocation
                    })
                    if (data.success) {
                        window.open(data.data, '_blank')
                    }
                } else {
                    this.$message.error("服务器内部拆分不能下载！")
                }
            },
            detail(id) {
                this.$router.push({path: '/archive/task/batch/yuanwen/detail', query: {batchId: id}})
            },
            $init() {
                this.loadData()
            },
            search() {
                this.loadData(this.searchForm)
            },
            showDialog(row) {
                this.dialogShow = true
                this.loading = true
                this.$http.post('/batch/yuanWenDetail', {
                    id: row.id,
                    batchType: 'IMP_YUANWEN',
                    batchName: row.batchName
                }).then(({data}) => {
                    if (data && data.success) {
                        this.data1 = data.data
                    }
                    this.loading = false
                    this.dialogShow = true
                })
            },
        }
    }
</script>
