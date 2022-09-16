<template>
    <section>
        <nac-info title="元数据项" back>
            <el-button type="primary" size="mini" @click="addDisplay">添 加</el-button>
        </nac-info>
        <div class="index_main">
            <el-table class="table-container" :data="data" border height="100%">
                <el-table-column type="selection" width="40"/>
                <el-table-column type="index" label="序号" sortable width="55"/>
                <el-table-column prop="formFieldName" label="元数据名称"/>
                <el-table-column prop="formFieldCode" label="编码"/>
                <el-table-column prop="order" label="排序"/>
                <el-table-column prop="formFieldShort" label="字段最短值"/>
                <el-table-column prop="formFieldLong" label="字段最长值"/>
                <el-table-column prop="formFieldType" label="元数据类型">
                    <template slot-scope="scope">
                        {{ scope.row.formFieldType|dict('fieldDisplayType') }}
                    </template>
                </el-table-column>
                <el-table-column prop="specialCharacters" label="特殊字符"/>
                <el-table-column prop="ifRequired" label="是否必填项"/>
                <el-table-column fixed="right" label="操作" align="center" width="180">
                    <template slot-scope="scope">
                        <el-link type="primary" @click="editDisplay(scope.row)">编 辑</el-link>
                        <el-divider direction="vertical"></el-divider>
                        <el-link type="danger" @click="remove(scope.row.id)">删 除</el-link>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <el-dialog :title="edit?'编辑元数据项':'添加元数据项'"
                   :visible.sync="dialogVisible"
                   :close-on-click-modal=false
                   :close-on-press-escape=false
                   @close="dialogVisible=false"
                   width="50%">
            <el-form ref="form" :model="form" inline label-width="100px" :rules="rules">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="元数据名称" prop="formFieldCode">
                            <el-select v-model="form.formFieldCode" filterable clearable placeholder="请选择">
                                <el-option style="width: 210px" v-for="item in fields" :key="item.fieldCode"
                                           :value="item.fieldCode"
                                           :label="item.label"/>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="元数据类型" prop="formFieldType">
                            <select-dict style="width: 210px" type="fieldDisplayType" v-model="form.formFieldType"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段最短值" prop="formFieldShort">
                            <el-input style="width: 210px" v-model="form.formFieldShort" placeholder="请输入字段最短值"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="字段最长值" prop="formFieldLong">
                            <el-input style="width: 210px" v-model="form.formFieldLong" placeholder="请输入字段最长值"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="特殊字符" prop="specialCharacters">
                            <el-input style="width: 210px" v-model="form.specialCharacters" placeholder="请输入特殊字符"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否必填项" prop="ifRequired">
                            <el-select v-model="form.ifRequired">
                                <el-option v-for="item in options"
                                           :value="item"
                                           :key="item"
                                           :label="item"/>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="排序" prop="order">
                            <el-input style="width: 210px" v-model="form.order" placeholder="请输入排序"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="备注" prop="remark">
                            <el-input style="width: 210px" v-model="form.remark" type="textarea" placeholder="请输入备注"/>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="saveForm" v-loading="loading">保 存</el-button>
            </div>
        </el-dialog>
    </section>
</template>
<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        mixins: [indexMixin],
        computed: {
            edit() {
                return this.form ? !!this.form.id : false
            }
        },
        data() {
            return {
                rules: {
                    formFieldCode: [
                        {required: true, message: '字段编码不能为空', trigger: 'blur'}
                    ],
                    formFieldName: [
                        {required: true, message: '字段名称不能为空', trigger: 'blur'}
                    ],
                    ifRequired: [
                        {required: true, message: '是否必填项不能为空', trigger: 'blur'}
                    ]
                },
                displayId: this.$route.query.id,
                formDefinitionId: this.$route.query.formDefinitionId,
                formFieldCode: '',
                type: '',
                param: {
                    displayId: "",
                },
                data: [],
                fields: [],
                form: {
                    metaDict: ''
                },
                dialogVisible: false,
                active: false,
                activeState: 0,
                inactive: true,
                inactiveState: 1,
                options: ["是", "否"],
            }
        },
        methods: {
            $init() {
                this.loadData()
                this.loadFields()
            },
            loadFields() {
                this.loading = true
                this.$post('manage/form/findFieldList', {formDefinitionId: this.formDefinitionId}).then(({data}) => {
                    if (data && data.success) {
                        this.fields = data.data
                    }
                    this.loading = false
                });
            },
            loadData() {
                this.loading = true
                this.$post('fournatureschemeitemmethod/page', {
                    page: false,
                    fourNatureSchemeItemId: this.displayId
                }).then(({data}) => {
                    if (data && data.success) {
                        this.data = data.data
                    }
                    this.loading = false
                });
            },
            editDisplay(row) {
                this.form = Object.assign(row)
                this.dialogVisible = true;
            },
            addDisplay() {
                this.form = {}
                if (this.data) {
                    this.form.fieldOrder = this.data.length + 1
                } else {
                    this.form.fieldOrder = 1
                }
                this.dialogVisible = true;
            },
            saveForm() {
                if (this.$refs.form) {
                    this.loading = true
                    this.$refs.form.validate(valid => {
                        if (valid) {
                            const path = `/fournatureschemeitemmethod/${this.edit ? "update" : "insert"}`;
                            let params = Object.assign({
                                fourNatureSchemeItemId: this.displayId,
                                formDefineId: this.formDefinitionId,
                            }, this.form)
                            if (!params.formFieldName) {
                                const field = this.fields.find(d => d.fieldCode === this.form.formFieldCode)
                                params.formFieldName = field ? field.label : ''
                            }
                            this.$post(path, params).then(({data}) => {
                                if (data && data.success) {
                                    this.dialogVisible = false
                                    this.loadData()
                                    this.$message.success('保存成功！')
                                } else {
                                    this.$message.error(data.message)
                                }
                                this.loading = false
                            })
                        } else {
                            this.loading = false
                        }
                    })
                }
            },
            remove(id) {
                this.$confirm('确定删除?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$http.post("/fournatureschemeitemmethod/delete", {id}).then(({data}) => {
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
            }
        }
    }
</script>
