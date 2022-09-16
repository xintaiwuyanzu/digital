<template>
    <div>
        <section>
            <el-dropdown placement="bottom" @command="operate">
                <el-button type="primary">
                    数据检查<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="repeat">开始检查</el-dropdown-item>
                    <el-dropdown-item command="reset">重置</el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
        </section>
        <el-dialog title="查重数据" :visible.sync="dialogShow" width="50%">
            <el-table :data="data" border style="margin-top: 20px" height="100%">
                <el-table-column label="排序" type="index" align="center" width="50"/>
                <el-table-column prop="archive_Code" label="档号" header-align="center" show-overflow-tooltip/>
                <el-table-column prop="count" label="数量" header-align="center" show-overflow-tooltip/>
                <el-table-column label="操作" width="130" align="center" header-align="center">
                    <template slot-scope="scope">
                        <el-button size="text" type="primary" @click="view(scope.row)">查看</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>
<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        data() {
            return {
                data: {},
                archvieCode: ''
            }
        },
        methods: {
            operate(command) {
                if (command === 'repeat') {
                    this.repeat()
                } else {
                    this.reset()
                }
            },
            //显示弹出框
            repeat() {
                let param = {fond: this.fond.code, category: this.category.code, formId: this.formId}
                let param2 = JSON.parse(this.eventBus.getQueryByQueryType()._QUERY)
                let status_info = param2.find(v => v.key == 'status_info')
                if (status_info) {
                    param.status = status_info.value
                }
                this.$http.post('/manage/formData/repeat', param).then(
                    ({data}) => {
                        if (data.success) {
                            if (data.data.length == 0) {
                                this.$message.success("没有查询到重复数据")
                            } else {
                                this.data = data.data
                                this.dialogShow = true
                            }
                        }
                    })
            },
            view(row) {
                this.archvieCode = row.archive_Code
                this.dialogShow = false
                this.eventBus.$emit("loadData")
            },
            getQuery() {
                if (this.archvieCode) {
                    return {key: 'archive_Code', value: this.archvieCode, type: 'i'}
                }
            },
            reset() {
                this.archvieCode = ''
                this.eventBus.$emit("loadData")
            }
        },
        mounted() {
            this.eventBus.$emit('addQueryItem', this)
        }
    }
</script>
