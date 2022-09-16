<template>
    <section>
        <nac-info :title="title">
            <div align="center">
                <el-button type="primary" @click="save()" style="margin-right: 10px">保 存</el-button>
                <el-button @click="$router.back()" style="margin-right: 10px">返 回</el-button>
            </div>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <el-row style="height: 100%">
                <el-col :span="12">
                    <!--左侧表单字段数据-->
                    <el-card style="height: 80vh;overflow:scroll">
                        表单：
                        <el-select @change="loadFields" v-model="formId" placeholder="请选择表单" filterable>
                            <el-option v-for="item in formData"
                                       :label="item.formName"
                                       :key="item.id"
                                       :value="item.id"/>
                        </el-select>
                        <el-table
                                ref="field"
                                title="排序准备字段"
                                :data="fieldData"
                                tooltip-effect="dark">
                            <el-table-column label="序号" fixed align="center" width="50">
                                <template slot-scope="scope">
                                    {{ scope.$index + 1 }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                    prop="label"
                                    label="字段"
                                    show-overflow-tooltip>
                            </el-table-column>
                            <el-table-column
                                    prop="fieldCode"
                                    label="编码"
                                    show-overflow-tooltip>
                            </el-table-column>
                            <el-table-column label="操作">
                                <template slot-scope="scope">
                                    <el-button icon="el-icon-plus" type="primary" size="mini" circle
                                               @click="add(scope.$index,scope.row)"></el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-card>
                </el-col>
                <el-col :span="12">
                    <!--右侧方案项数据，可编辑-->

                    <el-card style="height: 80vh;overflow:scroll">
                        <div style="text-align: right">
                            <el-button type="info" size="medium" @click="clearData">清空</el-button>
                        </div>
                        <el-table
                                ref="field"
                                title="排序字段"
                                :data="itemData"
                                tooltip-effect="dark">
                            <el-table-column label="序号" fixed align="center" width="50px">
                                <template slot-scope="scope">
                                    {{ scope.$index + 1 }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                    prop="name"
                                    label="字段"
                                    show-overflow-tooltip>
                            </el-table-column>
                            <el-table-column
                                    prop="code"
                                    label="编码"
                                    show-overflow-tooltip>
                            </el-table-column>
                            <el-table-column
                                    prop="hashKey"
                                    label="标题">
                                <template slot-scope="scope">
                                    <el-input v-model="scope.row.hashKey"></el-input>
                                </template>
                            </el-table-column>
                            <el-table-column
                                    label="操作">
                                <template slot-scope="scope">
                                    <el-button icon="el-icon-minus" type="primary" size="mini" circle
                                               @click="subtract(scope.$index,scope.row)"></el-button>
                                    <el-button icon="el-icon-arrow-up" type="primary" size="mini" circle
                                               @click="up(scope.$index,scope.row)"></el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </section>
</template>

<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        name: "index",
        mixins: [indexMixin],
        components: {},
        data() {
            return {
                fieldData: [],
                formData: [],
                itemData: [],
                deleteItem: [],
                schemeId: '',
                formId: '',
                title: '',
            }
        },
        methods: {
            $init() {
                this.schemeId = this.$route.query.id
                this.title = this.$route.query.name
                this.loadForms()
                this.loadItems()
            },
            loadForms() {
                this.$http.post('/manage/form/findFormList').then(({data}) => {
                    if (data.success) {
                        this.formData = data.data
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            loadFields() {
                this.$http.post('/manage/form/findFieldList', {formDefinitionId: this.formId}).then(({data}) => {
                    if (data.success) {
                        this.fieldData = data.data
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            loadItems() {
                const param = Object.assign({}, {
                    page: false,
                    businessId: this.schemeId
                })
                this.$http.post('/impexpschemeitem/page', param)
                    .then(({data}) => {
                        if (data.success) {
                            this.itemData = data.data
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    })
            },
            //添加排序字段
            add(index, row) {
                const len = this.itemData.length
                row.order = len + 1
                row.code = row.fieldCode
                row.name = row.label
                row.id = null
                row.codeLength = row.fieldLength
                row.codeType = row.fieldTypeStr
                let flag = true
                for (const itemDatum of this.itemData) {
                    if (itemDatum.code == row.code) {
                        flag = false
                    }
                }
                if (flag) {
                    this.fieldData.splice(index, 1)
                    this.itemData.push(row)
                } else {
                    this.$message.warning("该字段方案中已存在")
                }
            },
            //将选中的排序字段删除
            subtract(index, row) {
                row.hashKey = ''
                this.itemData.splice(index, 1)
                if (this.formId) {
                    this.fieldData.push(row)
                }
                this.deleteItem.push(row)
            },
            //将点击的字段排序上移一位
            up(index, row) {
                if (index !== 0) {
                    this.itemData.splice(index, 1)
                    this.itemData.splice(index - 1, 0, row)
                }
            },
            //将字段信息存入catalog对象中
            save() {
                for (let i = 0; i < this.itemData.length; i++) {
                    this.itemData[i].order = i + 1
                    this.itemData[i].businessId = this.schemeId
                    if (!this.itemData[i].hashKey) {
                        this.$message.error("请将字段显示名补充完整")
                        return
                    }
                }
                if (this.deleteItem.length > 0) {
                    let ids = ''
                    for (const argument of this.deleteItem) {
                        if (argument.id) {
                            ids += argument.id + ","
                        }
                    }
                    this.$http.post('/impexpschemeitem/delete', {ids: ids})
                        .then(({data}) => {
                            if (data.success) {
                                this.deleteItem = []
                                this.saveAll()
                            } else {
                                this.$message.error(data.message)
                            }
                            this.loading = false
                        })
                } else {
                    this.saveAll()
                }
            },
            saveAll() {
                let flag = true
                for (let i = 0; i < this.itemData.length; i++) {
                    const itemDataElement = this.itemData[i]
                    if (itemDataElement.id) {
                        this.$http.post('/impexpschemeitem/update', itemDataElement)
                            .then(({data}) => {
                                if (data.success) {
                                    this.itemData[i] = data.data
                                } else {
                                    flag = false
                                }
                                this.result(i, flag, this.itemData.length)
                            })
                    } else {
                        this.$http.post('/impexpschemeitem/insert', itemDataElement)
                            .then(({data}) => {
                                if (data.success) {
                                    this.itemData[i] = data.data
                                } else {
                                    flag = false
                                }
                                this.result(i, flag, this.itemData.length)
                            })
                    }
                }
            },
            result(index, flag, size) {
                if ((index + 1) == size) {
                    if (flag) {
                        this.$message.success("保存成功")
                    } else {
                        this.$message.error("保存失败，请编辑后重新保存")
                    }
                }
            },
            clearData() {
                this.itemData = []
            }
        }
    }
</script>

<style scoped>
    .el-table__body tr.current-row > td {
        background-color: #ff784a !important;
        color: #fff;
    }
</style>
