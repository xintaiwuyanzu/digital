<template>
    <section>
        <nac-info back title="拆分详情">
            <el-button type="danger" size="mini" @click="remove">批量删除</el-button>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table ref="multipleTable" :data="data" border height="100%" highlight-current-row
                          @selection-change="handleSelectionChange">
                    <el-table-column type="selection" width="55" align="center"/>
                    <column-index :page="page"/>
                    <el-table-column label="档号" prop="archival_code" show-overflow-tooltip align="center"/>
                    <el-table-column label="图片名" prop="imgName" show-overflow-tooltip align="center"/>
                    <el-table-column label="图片大小" prop="imgSize" show-overflow-tooltip align="center" min-width="120"/>
                    <el-table-column prop="split" label="拆分结果" header-align="center" align="center"
                                     show-overflow-tooltip
                                     width="80px">
                        <template slot-scope="scope">
                            {{ scope.row.split|dict({'0': '失败', '1': '成功'}) }}
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
                batchId: this.$route.query.batchId,
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
                    batchType: 'IMP_YUANWEN',
                    batchName: this.searchForm.batchName,
                    pageIndex: this.page.index - 1
                })
                this.data = data.data.data
                this.page.index = data.data.start / data.data.size + 1
                this.page.size = data.data.size
                this.page.total = data.data.total
                this.loading = false
            },
            $init() {
                this.loadData()
            },
            handleSelectionChange(val) {
                this.multipleSelection = val
            },
            remove() {
                let ids = ''
                if (this.multipleSelection) {
                    for (let i = 0; i < this.multipleSelection.length; i++) {
                        ids = ids + this.multipleSelection[i].id + ","
                    }
                }
                const param = Object.assign({}, {ids: ids, batchId: this.batchId})
                this.$confirm("确认删除？", '提示', {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning',
                    dangerouslyUseHTMLString: true
                }).then(() => {
                    this.$http.post('/batch/deleteDetail', param).then(({data}) => {
                        if (data.success) {
                            this.$message({
                                message: '操作成功！',
                                type: 'success'
                            });
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loadData()
                    })
                }).catch(() => {
                });
            },
        }
    }
</script>
