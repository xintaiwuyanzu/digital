<template>
    <section>
        <nac-info>
            <el-form :model="searchForm" ref="searchForm" inline>
                <el-form-item label="档号" prop="archiveCode">
                    <el-input v-model="searchForm.archiveCode" placeholder="请输入档号" clearable/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData" size="mini">搜索</el-button>
                    <el-button type="danger" size="mini" @click="remove">删 除</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <el-table border height="100%" class="table-container"
                      ref="multipleTable" :data="data"
                      @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" align="center"/>
                <column-index :page="page"/>
                <el-table-column
                        prop="archiveCode"
                        label="档号"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="fileName"
                        label="文件名称"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="filePath"
                        label="存放地址"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="code"
                        label="识别状态"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="message"
                        label="返回信息"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
            </el-table>
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
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        mixins: [indexMixin],
        name: "index",
        data() {
            return {
                page: {index: 0, size: 15},
                searchForm: {
                    archiveCode: ''
                },
                multipleSelection: [],
            }
        },
        mounted() {
            this.loadData()
        },
        methods: {
            loadData(params) {
                this.loading = true
                this.$http.post('/ofdRecord/page', params).then(({data}) => {
                    if (data.success) {
                        this.data = data.data.data
                        this.page.index = data.data.start / data.data.size + 1
                        this.page.size = data.data.size
                        this.page.total = data.data.total
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            handleSelectionChange(val) {
                this.multipleSelection = val
            },
            remove() {
                if (this.multipleSelection.length === 0) {
                    this.$message.error("请至少选择一条数据")
                    return
                }
                let ids = ''
                for (let i = 0; i < this.multipleSelection.length; i++) {
                    ids = ids + this.multipleSelection[i].id + ","
                }
                const param = Object.assign({}, {ids: ids})
                this.$confirm("确认删除？", '提示', {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning',
                    dangerouslyUseHTMLString: true
                }).then(() => {
                    this.$http.post('/ofdRecord/deleteByIds', param).then(({data}) => {
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

<style scoped>

</style>