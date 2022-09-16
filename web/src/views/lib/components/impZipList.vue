<template>
    <section>
        <el-button type="primary" v-show="this.childrenIndex" @click="showDialog">原文导入</el-button>
        <el-dialog width="50%" title="原文导入" :visible.sync="dialogShow" :close-on-click-modal="false">
            <el-form label-width="100px">
                <el-upload style="text-align: center;margin-top: 50px"
                           ref="uploadFile"
                           action="api/batch/newBatch"
                           accept="text/xml, application/xml,.zip"
                           :before-upload="beforeUpload"
                           :on-success="Push"
                           :data="{
                             formDefinitionId:this.formId,
                             fondId:this.fond.id,
                             categoryId:this.category.id,
                             type:'IMP_YUANWEN',
                             categoryCode:this.category.code,
                             impSchemaId:this.expSchemaId,
                             name:this.eventBus.defaultForm.status_info,
                             sourceCode:this.sourceCode
                           }"
                           :limit="1"
                           :on-exceed="handleExceed"
                           :auto-upload="false">
                    <el-button slot="trigger" size="medium" type="primary" icon="el-icon-search">选取文件</el-button>
                    <div style="margin-bottom: 20px">可上传zip文件</div>
                </el-upload>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="dialogShow = false" class="btn-cancel">取 消</el-button>
                <el-button type="primary" @click="submitUpload" v-loading="loading" class="btn-submit">导 入</el-button>
            </div>
        </el-dialog>
    </section>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: 'impZipList',
        data() {
            return {selectData: [], expSchemaId: '', sourceCode: ''}
        },
        methods: {
            //显示弹出框
            showDialog() {
                this.$http.post('/impexpscheme/page', {page: false}).then(({data}) => {
                    if (data && data.success) {
                        //只展示接收的
                        this.selectData = data.data.filter(function (val) {
                            if (val.schemeType === '1') {
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
                this.dialogShow = true
            },
            beforeUpload(file) {
                const FileExt = file.name.replace(/.+\./, "");
                if (['zip'].indexOf(FileExt.toLowerCase()) === -1) {
                    this.$message({
                        type: 'warning',
                        message: '请上传符合后缀名的附件！'
                    });
                    return false;
                }
            },
            handleExceed(files, fileList) {
                this.$message.warning(`当前限制选择 1 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
            },
            Push() {
                this.$message.success('上传成功，请在原文上传记录中查看详情')
                this.dialogShow = false
            },
            submitUpload() {
                this.$refs.uploadFile.submit();
                this.timeout = setInterval(() => {
                    if (this.percent <= 99) {
                        const a = Math.round(Math.random() * 5 + 2)
                        this.percent = a + this.percent >= 100 ? 99 : a + this.percent
                    }
                }, 1000);
            }
        },
    }
</script>
