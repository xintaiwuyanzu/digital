<template>
    <el-dialog
            @close="$parent.loadData"
            :title="id? '编辑': '添加' "
            width="30%"
            :close-on-click-modal="false"
            :visible.sync="show"
            :destroy-on-close=true
            :show-close=false>
        <el-form :model="categoryConfig" :rules="rules" ref="form" style="" label-width="100px">
            <el-form-item label="方案名称" prop="name">
                <el-input v-model="categoryConfig.name"
                          placeholder="请输入方案名称"
                          required></el-input>
            </el-form-item>
            <el-form-item label="起始年度" prop="startYear">
                <el-date-picker
                        v-model="categoryConfig.startYear"
                        type="year"
                        format="yyyy"
                        value-format="yyyy"
                        placeholder="起始年度"
                        :clearable="true">
                </el-date-picker>
            </el-form-item>
            <el-form-item label="终止年度" prop="endYear">
                <el-date-picker
                        v-model="categoryConfig.endYear"
                        type="year"
                        format="yyyy"
                        value-format="yyyy"
                        placeholder="终止年度"
                        :clearable="true">
                </el-date-picker>
            </el-form-item>
            <el-form-item label="项目表单" prop="proFormId"
                          v-if="category.archiveType==='2'">
                <el-select v-model="categoryConfig.proFormId"
                           clearable
                           placeholder="请选择项目表单">
                    <el-option
                            v-for="item in formData"
                            :key="item.id"
                            :label="item.formName"
                            :value="item.id">
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="案卷表单" prop="arcFormId" v-if="category.archiveType!=='0'">
                <el-select v-model="categoryConfig.arcFormId"
                           clearable
                           placeholder="请选择案卷表单" filterable>
                    <el-option
                            v-for="item in formData"
                            :key="item.id"
                            :label="item.formName"
                            :value="item.id">
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="文件表单" prop="fileFormId">
                <el-select v-model="categoryConfig.fileFormId"
                           clearable
                           placeholder="请选择文件表单" filterable>
                    <el-option
                            v-for="item in formData"
                            :key="item.id"
                            :label="item.formName"
                            :value="item.id">
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button type="info" @click="$parent.show = false" class="btn-cancel">取 消</el-button>
            <el-button type="primary" @click="save" v-loading="loading" class="btn-submit">提 交</el-button>
        </div>
    </el-dialog>
</template>
<script>
    export default {
        data() {
            return {
                loading: false,
                formData: [],
                categoryConfig: {},
                rules: {
                    name: [
                        {required: true, message: '请输入方案名称', trigger: 'blur'}
                    ],
                    startYear: [
                        {required: true, message: '请选择起始年度', trigger: 'blur'}
                    ],
                    endYear: [
                        {required: true, message: '请选择结束年度', trigger: 'blur'}
                    ]
                }
            }
        },
        watch: {
            show(v) {
                if (v) {
                    if (this.id) {
                        this.$post('/manage/categoryconfig/detail', {id: this.id})
                            .then(({data}) => {
                                const form = data.data
                                if (form.startYear === 0) {
                                    form.startYear = null
                                } else {
                                    form.startYear = '' + form.startYear
                                }
                                if (form.endYear === 0) {
                                    form.endYear = null
                                } else {
                                    form.endYear = '' + form.endYear
                                }
                                this.categoryConfig = form
                            })
                    } else {
                        this.categoryConfig = {}
                    }
                }
            }
        },
        methods: {
            async $init() {
                await this.getFormList()
            },
            async save() {
                const valid = await this.$refs.form.validate()
                if (valid) {
                    let path = `/manage/categoryconfig/${this.categoryConfig.id ? 'update' : 'insert'}`
                    if (!this.categoryConfig.startYear) {
                        this.categoryConfig.startYear = 0
                    }
                    if (!this.categoryConfig.endYear) {
                        this.categoryConfig.endYear = 0
                    }
                    this.categoryConfig.businessId = this.category.id
                    this.categoryConfig.code = this.category.id
                    this.categoryConfig.archiveType = this.category.archiveType
                    if (this.categoryConfig.startYear <= this.categoryConfig.endYear) {
                        this.loading = true
                        const {data} = await this.$http.post(path, this.categoryConfig)
                        if (data.success) {
                            this.$parent.show = false
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    } else {
                        this.$message.error("起始年度不得大于终止年度！")
                        this.loading = false
                    }
                }
            },
            timestampToTime(timestamp) {
                if (timestamp != 0 && timestamp != undefined) {
                    return this.$moment(timestamp).format('YYYY')
                }
            },
            async getFormList() {
                const {data} = await this.$http.post('/manage/form/findFormList')
                if (data.success) {
                    this.formData = data.data
                } else {
                    this.$message.error(data.message)
                }
            },
        },
        props: {
            //门类
            category: Object,
            //是否显示
            show: {default: false},
            //数据Id
            id: {type: String}
        }
    }
</script>
