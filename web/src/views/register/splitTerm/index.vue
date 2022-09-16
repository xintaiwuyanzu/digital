<template>
    <section>
        <nac-info title="拆件条件配置">
            <el-form :model="searchForm" ref="searchForm" inline>
                <el-form-item label="文件类型" prop="fileType">
                    <el-input v-model="searchForm.fileType" placeholder="请输入文件类型" clearable/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData" size="mini">搜索</el-button>
                    <el-button type="primary" size="mini" @click="showDialog">添加</el-button>
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
                        prop="fileType"
                        label="文件类型"
                        align="center"
                        header-align="center"
                        sortable>
                    <template slot-scope="scope">
                        {{scope.row.fileType|dict({'001': '封面', '002': '正文','003': '附件', '004': '办理单', '005': '底稿'}) }}
                    </template>
                </el-table-column>
                <el-table-column
                        prop="conditionType"
                        label="条件编号"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="pdCondition"
                        label="方法名称"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="content"
                        label="描述"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="ifOrder"
                        label="判断顺序"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="isEnable"
                        label="是否启用"
                        align="center"
                        header-align="center"
                        sortable>
                    <template slot-scope="scope">
                        {{scope.row.isEnable|dict({'1': '是', '0': '否'}) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="120">
                    <template slot-scope="scope">
                        <el-button type="text" @click="edit(scope.row)">编辑</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                    @current-change="index=>loadData({pageIndex:index-1},$parent.searchForm)"
                    :current-page.sync="page.index"
                    :page-size="page.size"
                    layout="total, prev, pager, next,jumper"
                    :total="page.total">
            </el-pagination>
        </div>
        <el-dialog :title="title" :visible.sync="addDialogVisible" width="80%" inline="true">
            <el-form :model="addForm" ref="addForm" abel-width="160px">
                <el-row>
                    <el-col :span="8">
                        <el-form-item label="文件类型：" prop="fileType" placeholder="请输入文件类型">
                            <el-select v-model="addForm.fileType"
                                       clearable
                                       placeholder="请选择文件类型">
                                <el-option
                                        v-for="item in FileTypeData"
                                        :key="item.id"
                                        :label="item.name"
                                        :value="item.id">
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="条件编号：" prop="conditionType" placeholder="请输入条件类型">
                            <el-input v-model="addForm.conditionType" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="方法名称：" prop="pdCondition" placeholder="请输入条件">
                            <el-input v-model="addForm.pdCondition" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="8">
                        <el-form-item label="判断顺序：" prop="ifOrder" placeholder="请输判断顺序">
                            <el-input v-model="addForm.ifOrder" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="是否启用：" prop="ifOrder" placeholder="请选择是否启用">
                            <el-select v-model="addForm.isEnable"
                                       clearable
                                       placeholder="请选择是否启用">
                                <el-option
                                        v-for="item in conditionData"
                                        :key="item.id"
                                        :label="item.name"
                                        :value="item.id">
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="22">
                        <el-form-item label="描述：" prop="content" placeholder="请输入判断内容">
                            <el-input v-model="addForm.content" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <span slot="footer" class="dialog-footer">
              <el-button type="primary" @click="addDialogVisible = false">取 消</el-button>
              <el-button type="success" @click="save" v-loading="loading">保 存</el-button>
            </span>
        </el-dialog>
    </section>
</template>

<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        mixins: [indexMixin],
        name: "拆件条件配置",
        data() {
            return {
                title: '新增条件',
                searchForm: {
                    fileType: ''
                },
                addDialogVisible: false,
                addForm: {},
                page: {
                    index: 1,
                    size: 15,
                    total: 0
                },
                FileTypeData: [
                    {
                        id: '001',
                        name: '封面'
                    }, {
                        id: '002',
                        name: '正文'
                    }, {
                        id: '003',
                        name: '附件'
                    }, {
                        id: '004',
                        name: '办理单'
                    }, {
                        id: '005',
                        name: '底稿'
                    }],
                conditionData: [{
                    id: '1',
                    name: '是'
                }, {
                    id: '0',
                    name: '否'
                }],
                multipleSelection: [],
            }
        },
        mounted() {
            this.loadData()
        },
        methods: {
            loadData(params) {
                this.loading = true
                params = Object.assign({}, {
                    data1: this.searchForm.batch_name,
                    pageIndex: this.page.index - 1
                })
                this.$http.post('/wsSplit/page', params).then(({data}) => {
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
            showDialog() {
                this.optype = 'add'
                this.title = '新增条件'
                this.addForm = Object.assign({}, {})
                this.addDialogVisible = true
            },
            edit(row) {
                this.optype = 'edit'
                this.title = '修改条件'
                this.addForm = Object.assign({}, row)
                this.addDialogVisible = true
            },
            save() {
                this.$confirm('确认保存?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$refs.addForm.validate(valid => {
                        if (valid) {
                            this.loading = true
                            let path = ""
                            if (this.optype === 'add') {
                                path = '/wsSplit/insert'
                            } else if (this.optype === 'edit') {
                                path = '/wsSplit/update'
                            }
                            this.$http.post(path, this.addForm).then(({data}) => {
                                if (data && data.success) {
                                    this.$message.success('操作成功！')
                                    this.addDialogVisible = false
                                    this.loading = false
                                    this.loadData()
                                } else {
                                    this.loading = false
                                    this.$message.error(data.message)
                                }
                            })
                        }
                    })
                })
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
                    this.$http.post('/wsSplit/deleteByIds', param).then(({data}) => {
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
            handleSelectionChange(val) {
                this.multipleSelection = val
            }
        }
    }
</script>

<style scoped>

</style>