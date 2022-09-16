<template>
    <section>
        <nac-info title="字段管理">
            <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
                <el-form-item label="字段名称" prop="label">
                    <el-input v-model="searchForm.label" placeholder="请输入字段名称"
                              clearable/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData" size="mini">搜 索</el-button>
                    <!--<el-button @click="$refs.searchForm.resetFields()" size="mini">重 置</el-button>-->
                </el-form-item>
                <el-form-item style="float: right">
                    <el-button type="primary" size="mini" @click="addField">添 加</el-button>
                    <!--TODO 后面再去做批量添加的功能
                    <el-button type="primary" size="mini" @click="addFieldList">批量添加</el-button>-->
                    <el-button type="success" size="mini" @click="back">返 回</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main">
            <el-table
                    class="table-container"
                    :data="data"
                    border
                    height="100%"
                    @selection-change="handleSelectionChange">
                <el-table-column
                        type="selection"
                        width="40">
                </el-table-column>
                <el-table-column
                        type="index"
                        label="序号"
                        align="center"
                        header-align="center"
                        sortable
                        width="55">
                </el-table-column>
                <el-table-column prop="label" label="字段名称" align="center" header-align="center">
                </el-table-column>
                <el-table-column prop="fieldCode" label="字段编号" align="center" header-align="center" sortable>
                </el-table-column>
                <el-table-column prop="fieldLength" label="字段长度" align="center" header-align="center" sortable>
                </el-table-column>
                <el-table-column prop="fieldType" label="字段类型" sortable align="center" header-align="center">
                </el-table-column>
                <el-table-column label="状态" prop="fieldState" align="center" header-align="center">
                    <template slot-scope="scope">
                        <el-tag v-if="scope.row.fieldState== 1" type="success">已启用</el-tag>
                        <el-tag v-else type="danger">已禁用</el-tag>
                    </template>
                </el-table-column>
                <el-table-column fixed="right" label="操作" align="center" width="200">
                    <template slot-scope="scope">
                        <el-button type="primary" size="mini" @click="editField(scope.row)">编 辑</el-button>
                        <el-button type="danger" size="mini" @click="remove(scope.row.fieldCode)">删 除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <el-dialog :title="form.id?'元数据编辑':'元数据添加'" :visible.sync="dialogVisible" :close-on-click-modal=false
                   :close-on-press-escape=false
                   :destroy-on-close=true
                   width="50%">
            <el-form ref="form" :model="form" inline label-width="100px" :rules="rules">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段编码" prop="fieldCode">
                            <el-input v-model="form.fieldCode" placeholder="请输入字段编码"
                                      v-focus></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段名称" prop="label">
                            <el-input v-model="form.label"
                                      placeholder="请输入字段名称"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段类型" prop="fieldTypeStr">
                            <el-select v-model="form.fieldTypeStr"
                                       placeholder="请选择字段类型">
                                <el-option v-for="item in fieldTypes"
                                           :key="item"
                                           :label="item"
                                           :value="item">
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段长度" prop="fieldLength">
                            <el-input v-model="form.fieldLength"
                                      placeholder="请输入字段长度"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段别名" prop="fieldAlias">
                            <el-input v-model="form.fieldAliasStr"
                                      placeholder="请输入字段别名"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段值" prop="fieldValue">
                            <el-input v-model="form.fieldValue"
                                      placeholder="请输入字段值"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="版本号" prop="version">
                            <el-input v-model="form.version"
                                      placeholder="请输入版本号"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="顺序号" prop="fieldOrder">
                            <el-input v-model="form.fieldOrder"
                                      @input="form.fieldOrder=form.fieldOrder.replace(/[^\d]/g,'')"
                                      placeholder="请输入顺序号"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="16">
                        <el-form-item label="字段描述" prop="description">
                            <el-input v-model="form.description" style="width: 280%" type="textarea"
                                      placeholder="请输入字段描述"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="16">
                        <el-form-item label="备注" prop="remark">
                            <el-input v-model="form.remark" style="width: 280%" type="textarea"
                                      placeholder="请输入备注"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="dialogVisible = false" class="btn-cancel">取 消</el-button>
                <el-button type="primary" @click="saveForm" v-loading="loading" class="btn-submit">提 交</el-button>
            </div>
        </el-dialog>
        <el-dialog :title="'批量添加元数据'" :visible.sync="dialogPlForm" :close-on-click-modal=false
                   :close-on-press-escape=false
                   :destroy-on-close=true
                   width="50%">
            <el-form ref="form" :model="form" inline label-width="100px" :rules="rules">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段编码" prop="fieldCode">
                            <el-input v-model="form.fieldCode" placeholder="请输入字段编码"
                                      v-focus></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段名称" prop="label">
                            <el-input v-model="form.label"
                                      placeholder="请输入字段名称"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段类型" prop="fieldTypeStr">
                            <el-select v-model="form.fieldTypeStr"  class="long-width"
                                       placeholder="请选择字段类型">
                                <el-option v-for="item in fieldTypes"
                                           :key="item"
                                           :label="item"
                                           :value="item">
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段长度" prop="fieldLength">
                            <el-input v-model="form.fieldLength"
                                      placeholder="请输入字段长度"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段别名" prop="fieldAlias">
                            <el-input v-model="form.fieldAliasStr"
                                      placeholder="请输入字段别名"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段值" prop="fieldValue">
                            <el-input v-model="form.fieldValue"
                                      placeholder="请输入字段值"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="版本号" prop="version">
                            <el-input v-model="form.version"
                                      placeholder="请输入版本号"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="顺序号" prop="fieldOrder">
                            <el-input v-model="form.fieldOrder"
                                      @input="form.fieldOrder=form.fieldOrder.replace(/[^\d]/g,'')"
                                      placeholder="请输入顺序号"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="16">
                        <el-form-item label="字段描述" prop="description">
                            <el-input v-model="form.description" style="width: 280%" type="textarea"
                                      placeholder="请输入字段描述"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="16">
                        <el-form-item label="备注" prop="remark">
                            <el-input v-model="form.remark" style="width: 280%" type="textarea"
                                      placeholder="请输入备注"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="dialogVisible = false" class="btn-cancel">取 消</el-button>
                <el-button type="primary" @click="saveForm" v-loading="loading" class="btn-submit">提 交</el-button>
            </div>
        </el-dialog>
    </section>
</template>

<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        mixins: [indexMixin],
        data() {
            return {
                searchForm: {
                    label: ''
                },
                formId: '',
                param: {
                    formId: "",
                },
                data: [],
                fieldTypes: [],
                form: {},
                dialogVisible: false,
                dialogPlForm: false,
                active: false,
                activeState: 0,
                inactive: true,
                inactiveState: 1,
                rules: {
                    fieldCode: [
                        {required: true, message: '字段编码不能为空', trigger: 'change'},
                        {required: true, message: '字段编码不能为空', trigger: 'blur'}
                    ],
                    label: [
                        {required: true, message: '字段名称不能为空', trigger: 'blur'}
                    ],
                    fieldTypeStr: [
                        {required: true, message: '字段类型不能为空', trigger: 'blur'}
                    ],
                    fieldLength: [
                        {required: true, message: '字段长度不能为空', trigger: 'blur'}
                    ]
                },
            }
        },
        methods: {
            $init() {
                this.formId = this.$route.query.formId
                this.loadData()
                this.findFieldType()
            },
            handleSelectionChange(val) {
                this.multipleSelection = val
            },
            findFieldType() {
                this.$http.post('manage/form/findFieldTypes').then(({data}) => {
                    if (data && data.success) {
                        this.fieldTypes = data.data
                    }
                    this.loading = false
                });
            },
            loadData() {
                this.loading = true
                const param = Object.assign({}, {
                    fieldName: this.searchForm.fieldName,
                    formDefinitionId: this.formId
                })
                this.$http.post('manage/form/findFieldList', param).then(({data}) => {
                    if (data && data.success) {
                        this.data = data.data
                    }
                    this.loading = false
                });
            },
            editField(row) {
                this.form = row
                this.dialogVisible = true;
            },
            addField() {
                this.form = {}
                if (this.data) {
                    this.form.fieldOrder = this.data.length + 1
                } else {
                    this.form.fieldOrder = 1
                }
                this.dialogVisible = true;
            },
            addFieldList() {

            },
            saveForm() {
                const fieldLength = /^[0-9]*$/;
                if (this.$refs.form) {
                    this.$refs.form.validate(valid => {
                        if (valid) {
                            if (fieldLength.test(this.form.fieldLength) === false) {
                                this.$message.error("长度只能为数字！")
                                return
                            }
                            if (fieldLength.test(this.form.fieldOrder) === false) {
                                this.$message.error("顺序号只能为数字！")
                                return
                            }
                            this.loading = true
                            let path = '/manage/form'
                            if (this.form.id) {
                                path = path + '/updateField'
                            } else {
                                path = path + '/addField'
                            }
                            this.form.formDefinitionId = this.formId
                            this.$http.post(path, this.form).then(({data}) => {
                                if (data && data.success) {
                                    this.dialogVisible = false
                                    this.formId = data.data.formDefinitionId
                                    this.loadData()
                                    this.$message.success('保存成功！')
                                } else {
                                    this.$message.error(data.message)
                                }
                                this.loading = false
                            })
                        }
                    })
                }
            },
            remove(code) {
                this.$confirm('确定删除?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    const param = Object.assign({}, {formDefinitionId: this.formId, fieldCode: code})
                    this.$http.post("/manage/form/deleteField", param).then(({data}) => {
                        if (data && data.success) {
                            this.formId = data.data.formDefinitionId
                            this.loadData()
                            this.$message.success('删除成功！')
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    })
                })
            },
            back() {
                this.$router.push({path: '/archive/manage/form'})
            }
        }
    }
</script>