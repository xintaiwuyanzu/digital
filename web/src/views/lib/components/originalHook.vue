<template>
    <section>
        <el-button type="primary" size="mini" @click="hook">
            运维客户端的批次挂机
        </el-button>
        <el-dialog :visible.sync="dialogVisible" title="挂接原文" width="70%">
            <el-form :model="form" ref="form" label-width="180px">
                <el-form-item label="上传批次记录:" prop="clientBatchId" required>
                    <select-async v-model="form.clientBatchId" placeholder="选择客户端上传批次记录" style="width: 60%" clearable
                                  url="/dataBatch/page" :params="dataBatchParams" labelKey="batchName"/>
                </el-form-item>
                <el-form-item label="拆分方式:" v-if="this.category.code.indexOf('ws') != -1" prop="coverOrSplit" required>
                    <select-async v-model="form.coverOrSplit" placeholder="请选择拆分方式" style="width: 60%"
                                  :mapper="splitOptions"
                                  valueKey="value"/>
                </el-form-item>
                <el-form-item label="挂接方式:" v-else prop="coverOrAdd" required>
                    <select-async v-model="form.coverOrAdd" placeholder="请选择挂接方式" style="width: 60%"
                                  :mapper="hookOptions"
                                  valueKey="value"/>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible=false">取消</el-button>
                <el-button type="primary" v-if="this.category.code.indexOf('ws') != -1" @click="tifToJsp">
                    tif拆分
                </el-button>
                <el-button type="primary" v-else @click="doHook">开始挂接</el-button>
            </div>
        </el-dialog>
    </section>
</template>

<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: "originalHook",
        data() {
            return {
                dialogVisible: false,
                form: {},
                dataBatchParams: {page: false, batchType: 0},
                splitOptions: [
                    {
                        value: 'split',
                        label: '按目录拆分'
                    }, {
                        value: 'splitAll',
                        label: '按文件夹拆分'
                    }],
                hookOptions: [
                    {
                        value: 'ADD',
                        label: '追加'
                    }, {
                        value: 'COVER',
                        label: '清除现有原文并挂接'
                    }],
            }
        },
        methods: {
            hook() {
                this.dialogVisible = true
            },
            //拆分
            async tifToJsp() {
                const valid = await this.$refs.form.validate()
                if (valid) {
                    const {data} = await this.$post('uploadfiles/tiffToJpgByPath', {
                        ...this.form
                    })
                    if (data.success) {
                        this.$message.success(data.data)
                    } else {
                        this.$message.error(data.message)
                    }
                    this.dialogVisible = false
                } else {
                    this.$message.error('请填写完整表单')
                }
            },
            //挂机
            async doHook() {
                const valid = await this.$refs.form.validate()
                if (valid) {
                    const {data} = await this.$post('', {
                        type: "FILE",
                        ...this.form
                    })
                    if (data.success) {
                        this.$message.success(data.data)
                    } else {
                        this.$message.error(data.message)
                    }
                    this.dialogVisible = false
                } else {
                    this.$message.error('请填写完整表单')
                }
            },
        },

    }
</script>

<style scoped>

</style>