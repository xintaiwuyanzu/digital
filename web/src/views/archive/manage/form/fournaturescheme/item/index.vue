<template>
    <section>
        <nac-info title="检测项" back>
            <el-button type="primary" size="mini" @click="addDisplay">添 加</el-button>
        </nac-info>
        <div class="index_main">
            <el-table class="table-container" :data="data" border height="100%">
                <el-table-column type="selection" width="40"/>
                <el-table-column prop="checkType" label="检测类型" show-overflow-tooltip>
                    <template slot-scope="scope">
                        {{ scope.row.checkType|dict('archive.testType') }}
                    </template>
                </el-table-column>
                <el-table-column prop="checkName" label="检测项目" show-overflow-tooltip>
                    <template slot-scope="scope">
                        {{ scope.row.checkName|dict('archive.checkName') }}
                    </template>
                </el-table-column>
                <el-table-column prop="checkTarget" label="检测对象" show-overflow-tooltip/>
                <el-table-column prop="checkAim" label="检测目的" show-overflow-tooltip/>
                <el-table-column prop="checkMethod" label="检测方法" show-overflow-tooltip/>
                <el-table-column fixed="right" label="操作" align="center" width="180">
                    <template slot-scope="scope">
                        <el-link type="primary" @click="editDisplay(scope.row)">编 辑</el-link>
                        <el-divider direction="vertical"></el-divider>
                        <el-link type="danger" @click="remove(scope.row.id)">删 除</el-link>
                        <el-divider direction="vertical"></el-divider>
                        <el-link type="primary" size="mini" @click="detail(scope.row)">查看详情</el-link>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <el-dialog :title="edit?'编辑检测项':'添加检测项'"
                   :visible.sync="dialogVisible"
                   :close-on-click-modal=false
                   :close-on-press-escape=false
                   @close="dialogVisible=false"
                   width="50%">
            <el-form ref="form" :model="form" inline label-width="100px" :rules="rules">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="检测类型" prop="checkType">
                            <select-dict style="width: 210px" type="archive.testType" v-model="form.checkType"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="检测项目" prop="checkName">
                            <select-dict style="width: 210px" type="archive.checkName" v-model="form.checkName"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="检测对象" prop="checkTarget">
                            <el-input style="width: 210px" v-model="form.checkTarget" placeholder="请输入检测对象"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="检测目的" prop="checkAim">
                            <el-input style="width: 210px" v-model="form.checkAim" placeholder="请输入检测目的"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="检测方法" prop="checkMethod">
                            <el-input style="width: 210px" v-model="form.checkMethod" type="textarea"
                                      placeholder="请输入检测方法"/>
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
                    checkType: [
                        {required: true, message: '检测类型不能为空', trigger: 'blur'}
                    ],
                    checkName: [
                        {required: true, message: '检测项目不能为空', trigger: 'blur'}
                    ]
                },
                displayId: this.$route.query.id,
                formDefinitionId: this.$route.query.formDefinitionId,
                param: {
                    displayId: "",
                },
                data: [],
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
            },
            loadData() {
                this.loading = true
                this.$post('fournatureschemeitem/page', {
                    page: false,
                    fourNatureSchemeId: this.displayId
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
                            const path = `/fournatureschemeitem/${this.edit ? "update" : "insert"}`;
                            let params = Object.assign({
                                fourNatureSchemeId: this.displayId,
                                formDefinitionId: this.formDefinitionId,
                            }, this.form)
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
                    this.$http.post("/fournatureschemeitem/delete", {id}).then(({data}) => {
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
            detail(row) {
                this.$router.push({
                    path: './item/detail',
                    query: {id: row.id, formDefinitionId: this.$route.query.formDefinitionId}
                })
            }
        }
    }
</script>