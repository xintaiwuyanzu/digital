<template>
    <section>
        <nac-info back title="导入详情">
            <el-input v-model="searchForm.batchName" style="width: 120px" placeholder="请输入题名" clearable/>
            <el-button type="primary" @click="$init" size="mini">搜 索</el-button>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table ref="applyTable" :data="data" border height="100%" highlight-current-row>
                    <column-index :page="page"/>
                    <el-table-column label="全宗" prop="fonds_identifier" width="100" show-overflow-tooltip align="center"/>
                    <el-table-column label="档号" prop="archival_code" width="230" show-overflow-tooltip align="center"/>
                    <el-table-column label="题名" prop="title" show-overflow-tooltip align="center"/>
                    <el-table-column label="责任者" prop="author" width="100" show-overflow-tooltip align="center"/>
                    <el-table-column label="关键词" prop="keyword" show-overflow-tooltip align="center"/>
                    <el-table-column label="检测结果" prop="fourDetection" width="80" show-overflow-tooltip align="center"/>
                    <el-table-column label="操作" align="center" header-align="center" width="160">
                        <template slot-scope="scope">
                            <el-link type="primary" @click="showDialog(scope.row)">检查报告</el-link>
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
            <el-dialog width="50%" title="检查报告" :visible.sync="dialogShow" append-to-body>
                <el-table :data="data1" border class="table-container">
                    <el-table-column prop="order" label="排序" width="80" header-align="center" align="center">
                        <template slot-scope="scope">
                            {{ scope.$index + 1 }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="createDate" label="检测时间" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="150px">
                        <template slot-scope="scope">
                            {{ scope.row.createDate|datetime }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="testRecordType" label="检测类型" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="100px">
                        <template slot-scope="scope">
                            {{ scope.row.testRecordType|dict('archive.testType') }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="status" label="检测结果" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="80px">
                        <template slot-scope="scope">
                            {{ scope.row.status|dict({'0': '未检测', '1': '通过', '2': '不通过'}) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="testName" label="元数据" header-align="center" align="center"
                                     show-overflow-tooltip/>
                    <el-table-column prop="testResult" label="说明" header-align="center" align="center"
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
                page: {
                    index: 1,
                    size: 15,
                    total: 0
                },
            }
        },
        mounted() {
            this.loadData()
        },
        methods: {
            async loadData() {
                this.loading = true
                const {data} = await this.$post('/batch/batchDetailPage', {
                    id: this.$route.query.batchId,
                    batchType: 'IMP',
                    batchName: this.searchForm.batchName,
                    pageIndex: this.page.index - 1
                })
                if (data && data.success) {
                    this.data = data.data.data
                    this.page.index = data.data.start / data.data.size + 1
                    this.page.size = data.data.size
                    this.page.total = data.data.total
                }
                this.loading = false
            },
            showDialog(row) {
                this.dialogShow = true
                this.loading = true
                this.$http.post('/testrecord/page', {page: false, formDataId: row.formDataId}).then(({data}) => {
                    if (data && data.success) {
                        this.data1 = data.data
                    }
                    this.loading = false
                    this.dialogShow = true
                })
            },
            $init() {
                this.loadData()
            }
        }
    }
</script>