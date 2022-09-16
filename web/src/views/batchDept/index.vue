<template>
    <section>
        <nac-info>
            <el-form :model="searchForm" ref="searchForm" inline>
                <el-form-item label="批次名" prop="batch_name">
                    <el-input v-model="searchForm.batch_name" placeholder="请输入批次名称" clearable/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData" size="mini">搜索</el-button>
                    <el-button v-if="organiseId" type="primary" size="mini" @click="showdialog">添加</el-button>
                    <el-button v-if="organiseId" type="danger" size="mini" @click="remove">删 除</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main card">
            <el-row>
                <el-col :span="5">
                    <el-card shadow="hover">
                        <div slot="header">
                            <strong>部门单位</strong>
                        </div>
                        <div style="min-height:78vh;overflow:auto">
                            <el-tree class="sysMenuTree"
                                     :data="menuData"
                                     default-expand-all
                                     @node-click="click"
                                     ref="menuTree">
                                <div style="flex: 1;margin: 2px; " slot-scope="{ node, data }">
                                    <span v-if="organiseId==data.data.id" style=" color: red;font-family: 等线">{{ data.label }}</span>
                                    <span v-if="organiseId!=data.data.id" style=" color: #409EFF;font-family: 等线">{{ data.label }}</span>
                                </div>
                            </el-tree>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="19">
                    <el-card shadow="hover" style="min-height:85vh;overflow:auto">
                        <div slot="header">
                            <strong>批次详情</strong>
                        </div>
                        <div class="table-container" style="height: 65vh">
                            <el-table border height="100%" class="table-container"
                                      ref="multipleTable" :data="data"
                                      @selection-change="handleSelectionChange">
                                <el-table-column type="selection" width="55" align="center"/>
                                <column-index :page="page"/>
                                <el-table-column
                                        prop="batch_name"
                                        label="批次号"
                                        align="center"
                                        header-align="center"
                                        sortable>
                                </el-table-column>
                                <el-table-column
                                        prop="receiver"
                                        label="登记人"
                                        align="center"
                                        header-align="center"
                                        sortable>
                                </el-table-column>
                                <el-table-column label="操作" align="center" width="120">
                                    <template slot-scope="scope">
                                        <el-button type="text" @click="edit(scope.row)">编 辑</el-button>
                                        <el-button type="text" @click="setTree(scope.row)">分类树</el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                            <el-pagination
                                    @current-change="index=>loadData(index,$parent.searchForm)"
                                    :current-page.sync="page.index"
                                    :page-size="page.size"
                                    layout="total, prev, pager, next,jumper"
                                    :total="page.total">
                            </el-pagination>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </div>
        <el-dialog :title="title" :visible.sync="addDialogVisible" width="80%" inline="true">
            <el-form :model="addForm" ref="addForm" abel-width="160px" :rules="rules">
                <el-row>
                    <el-col :span="8">
                        <el-form-item label="批次号：" prop="batch_name" placeholder="请输入批次号">
                            <el-input v-model="addForm.batch_name" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="单位名称：" prop="shiftOutName" placeholder="请输入单位名称">
                            <el-input v-model="addForm.shiftOutName" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="单位负责人： " prop="shiftOutCharge" placeholder="请输入单位负责人">
                            <el-input v-model="addForm.shiftOutCharge" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="8">
                        <el-form-item label="接收单位名称：" prop="receiveName" placeholder="请输入接收单位名称">
                            <el-input v-model="addForm.receiveName" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="接收单位负责人：" prop="receiveCharge" placeholder="请输入接收单位负责人">
                            <el-input v-model="addForm.receiveCharge" style="width: 60%"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="箱号:" prop="bigBoxNumber" placeholder="请输入接收箱号">
                            <el-input v-model="addForm.bigBoxNumber" style="width: 60%"></el-input>
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
        name: "index",
        data() {
            return {
                organiseId: "",
                menuData: [],
                page: {index: 0, size: 15},
                title: '新增批次',
                searchForm: {
                    batch_name: ''
                },
                addForm: {},
                optype: 'add',
                multipleSelection: [],
                addDialogVisible: false,
                rules: {
                    batch_name: [
                        {required: true, message: '批次号不能为空！', trigger: 'blur'}
                    ],
                }
            }
        },
        mounted() {
            this.loadData()
            this.loadLibRoot();
        },
        methods: {

            loadData(params) {
                this.loading = true
                params = Object.assign({}, this.page, {
                    data1: this.searchForm.batch_name,
                    index: this.page.index,
                    defaultOrganiseId: this.organiseId,
                })
                this.$http.post('/register/page', params).then(({data}) => {
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
            loadLibRoot() {
                this.loading = true
                this.$http.post('/organise/organiseTree', {all: true, sysId: this.sysId}).then(({data}) => {
                    if (data.success) {
                        this.menuData = data.data ? data.data : []
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
                this.ConfigForm = {}
            },
            setTree(row) {
                this.$router.push({path: '/home/batchtree', query: {id: row.id}})
            },
            edit(row) {
                this.optype = 'edit'
                this.title = '修改批次'
                this.addForm = Object.assign({}, row)
                this.addDialogVisible = true
            },
            click(data) {
                this.ConfigForm = data.data
                this.organiseId = this.ConfigForm.id
                this.orgName = data.data.name
                this.loadData()
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
                    this.$http.post('/register/deleteByids', param).then(({data}) => {
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
            showdialog() {
                let date = this.$moment(new Date()).format('YYYYMMDD')
                this.addForm = {batch_name: date}
                this.optype = 'add'
                this.title = '新增批次'
                this.addDialogVisible = true
            },
            save() {
                if (!this.addForm.batch_name) {
                    this.$message.error('批次号不能为空！')
                    return
                }
                this.$confirm('确认保存?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$refs.addForm.validate(valid => {
                        if (valid) {
                            let path = ""
                            if (this.optype === 'add') {
                                path = '/register/insert'
                                this.addForm.registerStatus = '0'
                            } else if (this.optype === 'edit') {
                                this.addForm.receiverDate = this.timestampToTime(this.addForm.receiverDate)
                                path = '/register/update'
                            }
                            if (undefined != this.addForm.receiverDate && null != this.addForm.receiverDate) {
                                this.addForm.receiverDate = new Date(this.addForm.receiverDate).getTime()
                            }
                            this.loading = true
                            this.$http.post(path, this.addForm).then(({data}) => {
                                if (data && data.success) {
                                    this.$message.success('操作成功！')
                                    this.$emit('func', 1, this.$parent.searchForm)
                                    if (this.optype === 'add') {
                                        let date = this.$moment(new Date()).format('YYYYMMDD')
                                        this.addForm = {batch_name: date}
                                    }
                                    this.addDialogVisible = false
                                    this.loadData()
                                    this.loading = false
                                } else {
                                    this.loading = false
                                    this.$message.error(data.message)
                                }
                            })
                        }
                    })
                })
            },
            timestampToTime(timestamp) {
                if (timestamp != 0 && timestamp != undefined) {
                    return this.$moment(timestamp).format('YYYY-MM-DD')
                }
            },
        }
    }
</script>

<style scoped>

</style>