<template>
    <section>
        <nac-info title="表单管理">
            <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
                <el-form-item label="表单名称" prop="fondName">
                    <el-input v-model="searchForm.formName" prefix-icon="el-icon-search" placeholder="请输入表单名称"
                              clearable/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="$init" size="small">搜 索</el-button>
                    <el-button type="primary" size="small" v-on:click="addOne">添加</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table :data="data" height="100%" stripe border
                          :default-sort = "{prop: 'createDate', order: 'descending'}"
                          @selection-change="handleSelectionChange">
                    <column-index :page="page"/>
                  <el-table-column
                      prop="formCode"
                      label="表单名称"
                      align="center"
                      header-align="center"
                      width="80">
                  </el-table-column>
                  <el-table-column
                      prop="formName"
                      label="表单类型"
                      align="center"
                      header-align="center"
                      width="80">
                  </el-table-column>
                  <el-table-column
                      prop="formTable"
                      label="数据库表名"
                      align="center"
                      header-align="center"
                      width="80">
                  </el-table-column>

<!--                    <el-table-column-->
<!--                            prop="formName"-->
<!--                            label="表单名称"-->
<!--                            header-align="center"-->
<!--                            align="center"-->
<!--                            width="250">-->
<!--                        <template slot-scope="scope">-->
<!--                            <el-popover trigger="hover" placement="top">-->
<!--                                <p>表单名称: {{ scope.row.formName }}</p>-->
<!--                                <p>表单编码: {{ scope.row.formCode }}</p>-->
<!--                                <p>数据表名: {{ scope.row.formTable }}</p>-->
<!--                                <p>表单版本: {{ scope.row.version }}</p>-->
<!--                                <div slot="reference" class="name-wrapper">-->
<!--                                    <el-tag size="medium">{{ scope.row.formName }}</el-tag>-->
<!--                                </div>-->
<!--                            </el-popover>-->
<!--                        </template>-->
<!--                    </el-table-column>-->
                    <el-table-column
                            prop="formType"
                            label="类型"
                            align="center"
                            header-align="center"
                            width="80">
                        <template slot-scope="scope">
                            {{ scope.row.formType|dict('archiveTypes') }}
                        </template>
                    </el-table-column>
                    <el-table-column
                            prop="description"
                            label="表单描述"
                            align="center"
                            header-align="center">
                    </el-table-column>
                    <el-table-column
                            prop="version"
                            label="版本"
                            align="center"
                            width="80"
                            header-align="center">
                    </el-table-column>
                  <el-table-column label="创建时间" prop="createDate" show-overflow-tooltip
                                   align="center">
                    <template v-slot="scope">
                      {{ getTime(scope.row.createDate) }}
                    </template>
                  </el-table-column>
                    <el-table-column
                            prop="default"
                            label="默认版本"
                            align="center"
                            width="80"
                            header-align="center">
                        <template slot-scope="scope">
                            <el-tag size="medium" v-if="scope.row.default" type="success">是</el-tag>
                            <el-tag size="medium" v-if="!scope.row.default" type="danger">否</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" align="center" width="280">
                        <template slot-scope="scope">
                            <el-button @click.native.prevent="editOne(scope.row)" type="text">编辑
                            </el-button>
                            <el-button @click.native.prevent="fieldManage(scope.row.id)" type="text">字段管理
                            </el-button>
                            <el-button @click.native.prevent="displayManage(scope.row.id)" type="text">显示方案
                            </el-button>
                            <el-button @click.native.prevent="fournaturescheme(scope.row.id)" type="text">检测方案
                            </el-button>
                            <el-button type="text" @click.native.prevent="delOne(scope.row.id)">删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <page :page="page" @size-change="handleSizeChange" @current-change="index=>loadData(index-1)"/>
            <el-dialog
                    :title="title"
                    :visible.sync="centerDialogVisible"
                    :close-on-click-modal=false
                    :close-on-press-escape=false
                    v-if="centerDialogVisible"
                    width="50%">
                <div>
                    <el-form ref="addForm" :model="addForm" label-width="100px" :rules="rules">
                        <el-form-item label="id" prop="id" :hidden="true">
                            <el-input v-model="addForm.id" placeholder="请输入id"></el-input>
                        </el-form-item>
                        <el-form-item label="表单名称" prop="formName">
                            <el-input v-model="addForm.formName" placeholder="请输入表单名称"
                                      v-focus></el-input>
                        </el-form-item>
                        <el-form-item label="表单编码" prop="formCode">
                            <el-input v-model="addForm.formCode"
                                      placeholder="请输入表单编码"></el-input>
                        </el-form-item>
                        <el-form-item label="表单类型" prop="formType"
                                      :rules="[
                                      { required: true, message: '表单类型不能为空'}
                                    ]" required>
                            <select-dict v-model="addForm.formType" type="archiveTypes"/>
                        </el-form-item>
                        <el-form-item label="表单描述" prop="description">
                            <el-input v-model="addForm.description" type="textarea"
                                      placeholder="请输入表单描述"></el-input>
                        </el-form-item>
<!--                        <el-form-item label="顺序号" prop="formOrder">
                            <el-input v-model="addForm.formOrder"
                                      @input="addForm.formOrder=addForm.formOrder.replace(/[^\d]/g,'')"
                                      placeholder="请输入顺序号"></el-input>
                        </el-form-item>-->
                    </el-form>
                </div>
                <div slot="footer" class="dialog-footer">
                    <el-button type="info" @click="centerDialogVisible = false" class="btn-cancel">取 消</el-button>
                    <el-button type="primary" @click="onSubmit" v-loading="loading" class="btn-submit">提 交</el-button>
                </div>
            </el-dialog>
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
                centerDialogVisible: false,
                title: '新增',
                formStateOptions: [],
                multipleSelection: [],
                warningMsg: '请选择一条记录！',
                delMsg: '请至少选择一条记录',
                isDisabled: false,
                active: 1,
                inactive: 0,
                dict: ['isDisabled'],
                optype: 'add',
                addForm: {
                    id: '',
                    formName: '',
                    displaySchemeName: '',
                    startYear: '',
                    endYear: '',
                    isEnabled: 1
                },
                searchForm: {
                    formName: '',
                    edit: true
                },
                rules: {
                    formCode: [
                        {required: true, message: '表单编码不能为空', trigger: 'change'}
                    ],
                    formName: [
                        {required: true, message: '表单名称不能为空', trigger: 'change'}
                    ],
                    formType: [
                        {required: true, message: '表单类型不能为空', trigger: 'change'}
                    ]
                },

            }
        },
        methods: {
            loadData(index) {
                this.loading = true
                this.page.pageIndex = index
                const params = Object.assign({}, this.page, {
                  formName: this.searchForm.formName
                })
                this.$http.post('/manage/form/findFormPage', params).then(({data}) => {
                    if (data.success) {
                      console.log(data.data.data)
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
            handleSizeChange(val) {
              this.page.size = val
            },
            $init() {
                this.loadData(0)
            },
            handleSelectionChange(val) {
                this.multipleSelection = val
            },
            addOne() {
                this.addForm = {
                    id: '',
                    formName: '',
                    displaySchemeName: '',
                    startYear: '',
                    endYear: '',
                    isEnabled: 1
                }
                this.centerDialogVisible = true
                this.title = '新增'
                this.optype = 'add'
            },
            editOne(row) {
                this.title = '修改'
                this.optype = 'edit'
                this.centerDialogVisible = true
                this.addForm = Object.assign({}, row)
                this.addForm.fields = []
                this.addForm.startYear = row.startYear == null ? '' : row.startYear + ''
                this.addForm.endYear = row.endYear == null ? '' : row.endYear + ''
            },
            validateForm() {
                if (!this.addForm.formName) {
                    this.$message.error('表单名称不能为空！')
                    return false
                } else {
                    return true
                }
            },
            onSubmit() {
                this.addForm.formFieldList = []
                if (this.optype === 'add') {
                    this.$refs.addForm.validate(valid => {
                        if (valid) {

                            this.loading = true
                            this.$http.post('/manage/form/addForm', this.addForm).then(({data}) => {
                                if (data && data.success) {
                                    this.$message.success('操作成功！')
                                    this.centerDialogVisible = false
                                    this.loadData(0)
                                    this.loading = false
                                } else {
                                    this.loading = false
                                    this.$message.error(data.message)
                                }
                            })

                        }
                    })
                }
                if (this.optype === 'edit') {
                    if (this.validateForm()) {
                        this.loading = true
                        this.addForm.fieldNames = ""
                        delete this.addForm.meta
                        this.$http.post('/manage/form/updateForm', this.addForm).then(({data}) => {
                            if (data && data.success) {
                                this.$message.success('操作成功！')
                                this.centerDialogVisible = false
                                this.loadData(0)
                                this.loading = false
                            } else {
                                this.loading = false
                                this.$message.error(data.message)
                            }
                        })
                    } else {
                        return
                    }
                }
            },
            fieldManage(formId) {
                this.$router.push({path: '/archive/manage/form/field', query: {formId: formId}})
            },
            displayManage(formId) {
                this.$router.push({path: '/archive/manage/form/display', query: {formId: formId}})
            },
            fournaturescheme(formId) {
                this.$router.push({path: '/archive/manage/form/fournaturescheme', query: {formId: formId}})
            },
            delOne(id) {
                this.$confirm('确定删除?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loading = true
                    this.$http.post('/manage/form/deleteForm', {
                      id: id
                    }).then(
                        ({data}) => {
                            if (data.success) {
                                this.$message.success('操作成功！')
                                this.loading = false
                                this.loadData(this.page.index - 1)
                            } else {
                                this.loading = false
                                this.$message.error(data.message)
                            }
                        })
                })
            },
          getTime(time) {
            return this.$moment(this.$moment(parseInt(time))).format('YYYY-MM-DD HH:mm:ss')
          },
        }
    }
</script>
