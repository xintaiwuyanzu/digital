<template>
    <div>
        <el-dialog
                title="选择排序字段"
                :visible.sync="$parent.dialogVisible"
                width="50%">
            <el-row>
                <el-col :span="9">
                    <el-table
                            ref="field"
                            title="排序准备字段"
                            height="400px"
                            :data="field"
                            tooltip-effect="dark">
                        <el-table-column label="序号" fixed align="center" width="50">
                            <template slot-scope="scope">
                                {{ scope.$index + 1 }}
                            </template>
                        </el-table-column>
                        <el-table-column
                                prop="name"
                                label="字段"
                                width="100vw">
                        </el-table-column>
                        <el-table-column
                                label="操作"
                                width="80vw">
                            <template slot-scope="scope">
                                <el-button icon="el-icon-plus" type="primary" size="mini" circle
                                           @click="add(scope.$index,scope.row)"></el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-col>
                <el-col :span="15">
                    <el-table
                            ref="field"
                            title="排序字段"
                            :data="pitchItems"
                            height="300px"
                            tooltip-effect="dark">
                        <el-table-column label="序号" fixed align="center" width="50px">
                            <template slot-scope="scope">
                                {{ scope.$index + 1 }}
                            </template>
                        </el-table-column>
                        <el-table-column
                                prop="connector"
                                label="连接符"
                                width="80px">
                            <template slot-scope="scope">
                                <el-select v-model="scope.row.connector">
                                    <el-option v-for="item in options"
                                               :value="item"
                                               :key="item"
                                               :label="item"/>
                                </el-select>
                            </template>
                        </el-table-column>
                        <!--<el-table-column
                            prop="digit"
                            label="位数"
                            width="110px">
                          <template slot-scope="scope">
                            <el-input-number v-model="scope.row.digit" style="width: 100px" controls-position="right"
                                             ></el-input-number>
                          </template>
                        </el-table-column>-->
                        <el-table-column
                                prop="name"
                                label="字段"
                                width="80px">
                        </el-table-column>
                        <el-table-column
                                label="操作"
                                width="80px">
                            <template slot-scope="scope">
                                <el-button icon="el-icon-minus" type="primary" size="mini" circle
                                           @click="subtract(scope.$index,scope.row)"></el-button>
                                <el-button icon="el-icon-arrow-up" type="primary" size="mini" circle
                                           @click="up(scope.$index,scope.row)"></el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div style="margin-left: 120px;height: 100px;margin-top: 10px">
                        <div style="background-color: #eeeeee;
                        height: 50px;width: 18vw;
                        border-radius: 20px;">
                            <!--<div style="margin-left: 20px;color: #1e89db">
                                生成示例：
                            </div>-->
                            <div style="color: red;margin-left: 40px;padding-top: 15px">
                                <span v-for="item in pitchOnTrue" :key="item.name" style="height: 50px">
                                    {{ item.name }}{{ item.connector }}</span>顺位编号
                            </div>
                        </div>
                    </div>
                </el-col>
                <div align="center" style="float: right">
                    <el-button type="info" @click="exit">取 消</el-button>
                    <el-button type="primary" @click="save" v-loading="loading">提 交</el-button>
                </div>
            </el-row>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                options: ["-", "·"],
                deleteItem: [],
                pitchItems: [],
                loading: false
            }
        },
        props: {
            pitchOn: Array,
            field: Array,
            showSchemeId: String
        },
        computed: {
            pitchOnTrue() {
                return this.pitchOn.filter(i => i.name)
            }
        },
        watch: {
            'pitchOn': {
                handler(oldValue, newValue) {
                    this.pitchItems = this.pitchOn
                }
            }
        },
        methods: {
            //关闭窗口提示
            handleClose(done) {
                this.$confirm('确认关闭？')
                    .then(() => {
                        done();
                    })
                    .catch(() => {
                    });
            },
            //添加排序字段
            add(index, row) {
                const len = this.pitchItems.length
                row.order = len + 1
                row.connector = "-"
                // row.digit = 1
                this.field.splice(index, 1)
                /*if (this.pitchItems.length > 0) {
                  this.pitchItems[this.pitchItems.length - 1].connector = "-"
                }*/
                this.pitchItems.push(row)
            },
            //将选中的排序字段删除
            subtract(index, row) {
                this.pitchItems.splice(index, 1)
                this.field.push(row)
                this.deleteItem.push(row)
                const length = this.pitchItems.length
                /*if (length > 0) {
                  this.pitchItems[length - 1].connector = ""
                }*/
            },
            //将点击的字段排序上移一位
            up(index, row) {
                /*if (index == (this.pitchItems.length - 1)) {
                  this.pitchItems[index - 1].connector = ""
                }*/
                if (index !== 0) {
                    this.pitchItems.splice(index, 1)
                    this.pitchItems.splice(index - 1, 0, row)
                }
            },
            //返回时将字段值全部放入field中
            exit() {
                this.$emit("fun")
                this.pitchItems = []
                this.$parent.dialogVisible = false;
            },
            //将字段信息存入catalog对象中
            save() {
                for (let i = 0; i < this.pitchItems.length; i++) {
                    this.pitchItems[i].order = i + 1
                    this.pitchItems[i].businessId = this.showSchemeId
                    if (this.pitchItems[i].fieldCode) {
                        this.pitchItems[i].code = this.pitchItems[i].fieldCode
                        this.pitchItems[i].name = this.pitchItems[i].fieldName
                    }
                }
                if (this.deleteItem.length > 0) {
                    for (const argument of this.deleteItem) {
                        if (argument.id) {
                            this.$http.post('/codingscheme/schemeitem/delete', {id: argument.id})
                                .then(({data}) => {
                                    if (data.success) {
                                        // this.saveAll()
                                    } else {
                                        this.$message.error(data.message)
                                    }
                                    this.loading = false
                                }).finally(res => {
                                this.deleteItem = []
                                this.saveAll()
                            })
                        }
                    }
                } else {
                    this.saveAll()
                }
                this.exit()
            },
            saveAll() {
                for (const pitchOnElement of this.pitchItems) {
                    if (pitchOnElement.id) {
                        this.$http.post('/codingscheme/schemeitem/update', pitchOnElement)
                            .then(({data}) => {
                                if (data.success) {
                                    this.loading = false
                                } else {
                                    this.$message.error(data.message)
                                }
                                this.loading = false
                            })
                    } else {
                        this.$http.post('/codingscheme/schemeitem/insert', pitchOnElement)
                            .then(({data}) => {
                                if (data.success) {
                                    this.loading = false
                                } else {
                                    this.$message.error(data.message)
                                }
                                this.loading = false
                            })
                    }
                }
            }
        }
    }
</script>
<style scoped>
    .btn-submit {
        width: 101px;
        height: 37px;
        /*background-image: linear-gradient(159deg, rgba(196, 245, 255, 0.64) 0%, rgba(196, 241, 255, 0.51) 21%, #c3ecff 100%), linear-gradient(#2d79f0, #2d79f0);*/
        background-blend-mode: overlay, normal;
        border-radius: 18px;
    }

    .btn-cancel {
        width: 101px;
        height: 37px;
        /*background-image: linear-gradient(-23deg, #9ebac7 0%, #b7cedb 73%, #d0e1ef 100%), linear-gradient(#ccd5e8, #ccd5e8);*/
        background-blend-mode: overlay, normal;
        border-radius: 18px;
    }
</style>
