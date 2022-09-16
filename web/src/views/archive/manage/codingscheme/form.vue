<template>
    <el-dialog
            :title="id? '编辑': '添加' "
            width="30%"
            @close="$parent.isShowDialog = false"
            :visible.sync="show" append-to-body>
        <el-form :model="showScheme" ref="form" style="" label-width="100px" :rules="rules">
            <el-form-item label="方案名称" prop="name" required>
                <el-input v-model="showScheme.name" placeholder="请输入方案名称"
                          clearable></el-input>
            </el-form-item>
            <el-form-item label="顺号位数" prop="digit" required>
                <el-input-number v-model="showScheme.digit"></el-input-number>
            </el-form-item>
            <el-form-item label="顺序号" prop="order">
                <el-input v-model="showScheme.order"
                          placeholder="请输入顺序号"
                          @input="showScheme.order=showScheme.order.replace(/[^\d]/g,'')"
                          clearable></el-input>
            </el-form-item>
            <el-form-item label="起始年度" prop="startYear" required>
                <el-date-picker
                        v-model="showScheme.startYear"
                        type="year"
                        placeholder="起始年度"
                        :clearable="true">
                </el-date-picker>
            </el-form-item>
            <el-form-item label="终止年度" prop="endYear" required>
                <el-date-picker
                        v-model="showScheme.endYear"
                        type="year"
                        placeholder="终止年度"
                        :clearable="true">
                </el-date-picker>
            </el-form-item>
            <el-form-item label="是否自动生成" prop="isAutoGeneration">
                <el-switch
                        :true-label="truelabel" :false-label="falselabel"
                        v-model="showScheme.isAutoGeneration"
                        active-color="#13ce66"
                        inactive-color="#ff4949">
                </el-switch>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button type="info" @click="$parent.isShowDialog = false" class="btn-cancel">取 消</el-button>
            <el-button type="primary" @click="save('form')" v-loading="loading" class="btn-submit">提 交</el-button>
        </div>
    </el-dialog>
</template>
<script>
    export default {
        data() {
            return {
                loading: false,
                formData: [],
                showScheme: {
                    businessId: this.businessId
                },
                rules: {
                    name: [
                        {required: true, message: '请输入方案名称', trigger: 'blur'},
                        {required: true, message: '请输入方案名称', trigger: 'change'}
                    ],
                    digit: [
                        {required: true, message: '请输入顺号位数', trigger: 'blur'},
                        {required: true, message: '请输入顺号位数', trigger: 'change'}
                    ],
                    code: [
                        {required: true, message: '请输入方案编码', trigger: 'blur'},
                        {required: true, message: '请输入方案编码', trigger: 'change'}
                    ],
                    startYear: [
                        {required: true, message: '请选择起始年度', trigger: 'blur'},
                        {required: true, message: '请选择起始年度', trigger: 'change'}
                    ],
                    endYear: [
                        {required: true, message: '请选择终止年度', trigger: 'blur'},
                        {required: true, message: '请选择终止年度', trigger: 'change'}
                    ]
                },
                truelabel: 1,
                falselabel: 0,
                show: true
            }
        },
        watch: {
            'id': {
                handler() {
                    if (this.id) {
                        this.$post('/codingscheme/detail', {id: this.id})
                            .then(({data}) => {
                                const form = data.data
                                form.startYear = '' + form.startYear
                                form.endYear = '' + form.endYear
                                this.dataSwitch()
                                this.showScheme = form
                                if (this.showScheme.startYear == 0) {
                                    this.showScheme.startYear = ''
                                }
                                if (this.showScheme.endYear == 0) {
                                    this.showScheme.endYear = ''
                                }
                            })
                    } else {
                        this.showScheme = {businessId: this.businessId}
                    }
                },
                deep: true,
                immediate: true,
            }
        },
        methods: {
            save(form) {
                this.$refs[form].validate((valid) => {
                    if (valid) {
                        const loading = this.$loading({
                            lock: true,
                            text: 'Loading',
                            spinner: 'el-icon-loading',
                            background: 'rgba(99, 99, 99, 0.7)'
                        });
                        let path = `/codingscheme/${this.id ? 'update' : 'insert'}`
                        this.dataSwitch()
                        this.showScheme.code = this.category.code
                        if (this.showScheme.endYear == 0 || this.showScheme.startYear <= this.showScheme.endYear) {
                            const {data} = this.$http.post(path, this.showScheme).then(({data}) => {
                                if (data.success) {
                                    this.$message.success("保存成功")
                                    this.show = false
                                    this.$emit('func')
                                } else {
                                    this.$message.error(data.message)
                                    if (this.showScheme.startYear == 0) {
                                        this.showScheme.startYear = null
                                    }
                                    if (this.showScheme.endYear == 0) {
                                        this.showScheme.endYear = null
                                    }
                                }
                                loading.close()
                            })
                        } else {
                            this.$message.error("起始年度不得大于终止年度！")
                            loading.close()
                        }
                    }
                })
            },
            dataSwitch() {
                if (undefined != this.showScheme.startYear && null != this.showScheme.startYear) {
                    this.showScheme.startYear = this.timestampToTime(this.showScheme.startYear)
                }
                if (undefined != this.showScheme.endYear && null != this.showScheme.endYear) {
                    this.showScheme.endYear = this.timestampToTime(this.showScheme.endYear)
                }
                if (!this.showScheme.startYear) {
                    this.showScheme.startYear = 0
                }
                if (!this.showScheme.endYear) {
                    this.showScheme.endYear = 0
                }
            },
            timestampToTime(timestamp) {
                if (timestamp != 0 && timestamp != undefined) {
                    return this.$moment(timestamp).format('YYYY')
                }
            },
            async getFormList() {
                const {data} = await this.$http.post('/codingscheme/page')
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
            //数据Id
            id: {type: String},
            //分类id
            businessId: {type: String}
        },
    }
</script>
