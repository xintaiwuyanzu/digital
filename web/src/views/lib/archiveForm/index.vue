<template>
    <el-form ref="form" :model="form" label-width="100px" inline v-if="listFields.length>0">
        <el-form-item :prop='field.code' :label='field.name' v-for="field in listFields" :key="field.id">
            <select-dict v-model="form[field.code]" :type='field.meta.dict' clearable
                         v-if="field.meta && field.meta.dict"
                         @select="computeArchiveCode()"/>
            <el-input v-model="form[field.code]" v-else :style="{width:field.remarks?(field.remarks+'px'):'auto'}"
                      @blur="computeArchiveCode()"/>
        </el-form-item>
    </el-form>
</template>
<script>
    /**
     * 档案编辑表单页面
     */
    export default {
        props: {
            formDefinitionId: {type: String},
            fondId: {type: String},
            categoryId: {type: String},
            form: {type: Object}
        },
        data() {
            return {
                labelWidth: '100px',
                required: false,
                listFields: [],
                codingItems: [],
                datalist: [],
                fond: '',
                category: '',
            }
        },
        watch: {
            formDefinitionId() {
                this.loadFormShowScheme()
            }
        },
        methods: {
            async loadFormShowScheme() {
                if (this.formDefinitionId) {
                    const {data} = await this.$http.post('/manage/form/selectDisplayByDefinition', {
                        formDefinitionId: this.formDefinitionId,
                        schemeType: 'form',
                        formCode: 'form',
                        fondId: this.fondId,
                        categoryId: this.categoryId,
                    })
                    if (data.success) {
                        this.labelWidth = `${data.data.labelWidth}`
                        console.log(data.data.fields)
                        this.listFields = data.data.fields
                        this.listFields.forEach(f => {
                            if (!this.form[f.code]) {
                                this.$set(this.form, f.code, '')
                            }
                        })
                        await this.computeArchiveCode(this.fondId, this.categoryId)
                    } else {
                        this.$message.error(data.message)
                    }
                }
            },
            validate() {
                return this.$refs.form.validate()
            },
            //动态配置档号信息
            async computeArchiveCode() {
                await this.getFieldCx()
                if (this.datalist.data.length > 0)
                    this.$nextTick(() => {
                        this.form.archival_code = ""
                        for (let i = 0; i < this.datalist.data.length; i++) {
                            let temp = this.datalist.data[i].code
                            let ljf = this.datalist.data[i].connector
                            this.form.archival_code += ljf + this.form[temp];
                        }
                        this.form.archival_code = this.form.archival_code.substring(1)
                        this.form.aj_archival_code = this.form.archival_code
                    })
            },
            //获取档号生成规则
            async getFieldCx() {
                this.fond = this.fondId, this.category = this.categoryId,
                    await this.$http.post('/codingscheme/getFieldCx', {
                        fondId: this.fond,
                        categoryId: this.category,
                    }).then(({data}) => {
                        this.datalist = data
                        if (data.success) {
                        } else {
                            if (!this.datalist.data) {
                            } else {
                                this.$message.error(data.message)
                            }
                        }
                        this.loading = false
                    })
                return this.datalist
            },
            $init() {
                this.getFieldCx()
                this.loadFormShowScheme()
            }
        }
    }
</script>
