<template>
    <section>
        <el-dropdown
                placement="bottom"
                trigger="click"
                @command="handleCommand">
            <el-button class="search-btn" type="primary" v-show="!this.parentIndex">导出目录<i
                    class="el-icon-arrow-down el-icon--right"/></el-button>
            <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="currentSelect.length>0" command="select">导出选中</el-dropdown-item>
                <el-dropdown-item command="all">导出所有</el-dropdown-item>
                <el-dropdown-item command="query">导出查询</el-dropdown-item>
            </el-dropdown-menu>
        </el-dropdown>
        <el-dialog width="50%" title="导出" :visible.sync="dialogShow" :close-on-click-modal="false">
            <el-form>
                <el-form-item label="选择导出方案">
                    <el-select v-model="expSchemaId" style="width: 200px">
                        <el-option v-for="item in selectData"
                                   :label="item.name"
                                   :value="item.id"
                                   :key="item.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="选择导出类型">
                    <select-dict v-model="mineType" type="impexp.mineType" placeholder="请选择导出类型" style="width: 200px"/>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="dialogShow = false" class="btn-cancel">取 消</el-button>
                <el-button type="primary" @click="onSubmit" v-loading="loading" class="btn-submit">提 交</el-button>
            </div>
        </el-dialog>
    </section>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: 'expFileList',
        data() {
            return {
                selectData: [],
                expSchemaId: '',
                mineType: '',
                dict: ['impexp.mineType'],
                expType: 'all'
            }
        },
        methods: {
            handleCommand(command) {
                this.expType = command
                this.showDialog()
            },
            //显示弹出框
            showDialog() {
                this.loading = true
                this.$http.post('/impexpscheme/page', {page: false}).then(({data}) => {
                    if (data && data.success) {
                        //只展示导出的
                        this.selectData = data.data.filter(function (val) {
                            if (val.schemeType === '2') {
                                return val
                            }
                        })
                        //如果 1 则直接赋值显示
                        if (this.selectData.length === 1) {
                            this.expSchemaId = this.selectData[0].id
                        }
                        this.dialogShow = true
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            onSubmit() {
                const query = this.eventBus.getQueryByQueryType(this.expType)
                this.$http.post('/batch/newBatch', {
                    impSchemaId: this.expSchemaId,
                    mineType: this.mineType,
                    type: 'EXP',
                    ...query
                }).then(({data}) => {
                    if (data && data.success) {
                        this.$message.success('正在导出...，请到【导出记录】查看结果')
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                    this.dialogShow = false
                })
            }
        },
    }
</script>
