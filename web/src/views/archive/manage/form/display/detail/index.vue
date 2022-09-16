<template>
    <section>
        <nac-info title="字段显示方案" back>
            <el-button type="primary" size="mini" @click="addDisplay">添 加</el-button>
        </nac-info>
        <div class="index_main">
            <el-table class="table-container" :data="data" border height="100%">
                <el-table-column type="selection" width="40"/>
                <el-table-column type="index" label="序号" sortable width="55"/>
                <el-table-column prop="name" label="显示名称"/>
                <el-table-column prop="code" label="编码"/>
                <el-table-column prop="order" label="排序"/>
                <el-table-column prop="labelWidth" label="显示宽度"/>
                <el-table-column prop="remarks" label="组件显示宽度"/>
                <el-table-column prop="type" label="显示类型">
                    <template slot-scope="scope">
                        {{ scope.row.type|dict('fieldDisplayType') }}
                    </template>
                </el-table-column>
                <el-table-column fixed="right" label="操作" align="center" width="180">
                    <template slot-scope="scope">
                        <el-link type="primary" @click="editDisplay(scope.row)">编 辑</el-link>
                        <el-divider direction="vertical"></el-divider>
                        <el-link type="danger" @click="remove(scope.row.id)">删 除</el-link>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <el-dialog :title="edit?'编辑字段显示方案':'添加字段显示方案'"
                   :visible.sync="dialogVisible"
                   :close-on-click-modal=false
                   :close-on-press-escape=false
                   @close="('form')"
                   width="50%">
            <el-form ref="form" :model="form" inline label-width="100px" :rules="rules">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字段" prop="code">
                            <el-select v-model="form.code" filterable clearable placeholder="请选择">
                                <el-option v-for="item in fields" :key="item.fieldCode" :value="item.fieldCode"
                                           :label="item.label"/>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="显示类型" prop="type">
                            <select-dict type="fieldDisplayType" v-model="form.type"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="显示宽度" prop="labelWidth">
                            <el-input v-model="form.labelWidth" placeholder="请输入显示宽度"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="显示名称" prop="name">
                            <el-input v-model="form.name" placeholder="请输入显示名称"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="描述" prop="description">
                            <el-input v-model="form.description" type="textarea" placeholder="请输入描述"/>
                        </el-form-item>
                    </el-col>
                    <cl-sol :span="12">
                        <el-form-item label="组件显示宽度" prop="remarks">
                            <el-input v-model="form.remarks" placeholder="请输入组件显示宽度"/>
                        </el-form-item>
                    </cl-sol>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="字典编码" prop="metaDict" v-if="form.type==='dict'">
                            <el-input v-model="form.metaDict" placeholder="请输入字典编码"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序" prop="order">
                            <el-input v-model="form.order" placeholder="请输入排序"/>
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
                    code: [
                        {required: true, message: '显示名称不能为空', trigger: 'blur'}
                    ],
                    labelWidth: [
                        {required: true, message: '显示宽度不能为空', trigger: 'blur'}
                    ],
                    type: [
                        {required: true, message: '显示类型不能为空', trigger: 'blur'}
                    ],
                    metaDict: [
                        {required: true, message: '字典编码不能为空', trigger: 'blur'}
                    ]
                },
                displayId: this.$route.query.id,
                formDefinitionId: this.$route.query.formDefinitionId,
                code: this.$route.query.code,
                type: this.$route.query.type,
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
                this.$post('manage/form/selectDisplay', {formDisplayId: this.displayId}).then(({data}) => {
                    if (data && data.success) {
                        this.data = data.data.fields
                    }
                    this.loading = false
                });
            },
            editDisplay(row) {
                this.form = Object.assign({metaDict: row.meta.dict}, row)
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
                            const path = `/manage/form/${this.edit ? "updateFieldDisplay" : "addFieldDisplay"}`;
                            let params = Object.assign({
                                formDisplayId: this.displayId,
                                formDefinitionId: this.formDefinitionId,
                            }, this.form)
                            if (!params.name) {
                                const field = this.fields.find(d => d.fieldCode === this.form.code)
                                params.name = field ? field.label : ''
                            }
                            delete params.meta
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
                    this.$http.post("/manage/form/deleteFieldDisplay", {id}).then(({data}) => {
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
