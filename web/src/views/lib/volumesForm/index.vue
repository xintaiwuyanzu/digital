<template>
    <el-form ref="form" :model="form" inline  label-width="100px" v-if="listFields.length>0">
        <el-form-item :prop='field.code' :label='field.name' v-for="field in listFields" :key="field.id">
            <select-dict v-model="form[field.code]" :type='field.meta.dict' clearable v-if="field.meta&&field.meta.dict"
                         @select="computeArchiveCode"/>
          <el-input v-model="form[field.code]" v-else :style="{width:'150px'}"
                    @blur="computeArchiveCode"/>
<!--            <el-input v-model="form[field.code]" v-else :style="{width:field.remarks?(field.remarks+'px'):'auto'}"-->
<!--                      @blur="computeArchiveCode"></el-input>-->
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
                this.loadFormShowScheme(0)
            }
        },
        methods: {
            async loadFormShowScheme(val) {
                if (this.formDefinitionId) {
                    const {data} = await this.$http.post('/manage/form/selectDisplayByDefinition', {
                        formDefinitionId: this.formDefinitionId,
                        schemeType: 'quality',
                        formCode: 'quality',
                        fondId: this.fondId,
                        categoryId: this.categoryId,
                    })
                    if (data.success) {
                        this.labelWidth = `${data.data.labelWidth}`
                        this.listFields = data.data.fields
                        this.listFields.forEach(f => {
                            if (!this.form[f.code]) {
                                this.$set(this.form, f.code, '')
                            }
                        })
                        this.computeArchiveCode(this.fondId, this.categoryId)
                    } else {
                        this.$message.error(data.message)
                    }
                }
            },
            validate() {
                return this.$refs.form.validate()
            },
            //动态配置档号
            async computeArchiveCode() {
                await this.getFieldCx()
                if (!this.form.id) {
                    this.$nextTick(() => {
                        this.form.archival_code = ""
                        this.form.aj_archival_code = ""
                        for (let i = 0; i < this.datalist.data.length; i++) {
                            let temp = this.datalist.data[i].code
                            let ljf = this.datalist.data[i].connector
                            this.form.archival_code += ljf + this.form[temp];
                        }
                        this.form.aj_archival_code = this.form.archival_code.substring(1)
                        if (this.form.archives_item_number != null) {
                            this.form.archival_code = this.form.archival_code.substring(1) + '-' + this.form.archives_item_number;
                        } else {
                            this.form.archival_code = this.form.archival_code.substring(1)
                        }
                    })
                }
            },
            //获取配置类默认配置方案
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
                                this.$message.error("未配置档号生成规则！")
                            } else {
                                //  this.$message.error(data.message)
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