<template>
    <section>
        <el-button type="primary" v-if="this.status != '2' " @click="showDialog">目录导入</el-button>
        <el-dialog width="50%" title="目录导入" :visible.sync="dialogShow" :close-on-click-modal="false">
            <el-form label-width="100px">
                <el-form-item label="导入类型">
                    <el-select v-model="expSchemaId" style="width: 200px" placeholder="选择导入类型">
                        <el-option v-for="item in selectData"
                                   :label="item.name"
                                   :value="item.id"
                                   :key="item.id"/>
                    </el-select>
                </el-form-item>
                <el-form-item label="数据来源">
                    <select-dict type="impSourceTypes" style="width: 200px" v-model="sourceCode" placeholder="请选择数据来源"/>
                </el-form-item>
                <div style="margin-bottom: 20px; color: red">注意：没有导入类型时请先在[配置管理]-配置导入方案</div>
                <el-form-item>
                    <el-upload style="text-align: center;margin-top: 50px"
                               ref="uploadFile"
                               action="api/batch/newBatch"
                               accept="text/xml, application/xml,.xml,.xlsx,.xls,.dbf,.accdb"
                               :before-upload="beforeUpload"
                               :on-success="Push"
                               :data="{
                         formDefinitionId:this.formId,
                         fondId:this.category.id,
                         // fondCode:this.fond.code,
                         categoryId:this.category.id,
                         type:'IMP',
                         categoryCode:this.category.code,
                         impSchemaId:this.expSchemaId,
                         name:this.eventBus.defaultForm.status_info,
                         sourceCode:this.sourceCode
                     }"
                               :limit="1"
                               :on-exceed="handleExceed"
                               :auto-upload="false">
                        <el-button slot="trigger" size="medium" type="primary" icon="el-icon-search">选取文件</el-button>
                        <div style="margin-bottom: 20px; ">可上传xlsx,xls,xml,dbf,accdb文件</div>
                    </el-upload>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="dialogShow = false" class="btn-cancel">取 消</el-button>
                <el-button type="primary" @click="submitUpload" v-loading="loading" class="btn-submit">上 传</el-button>
            </div>
        </el-dialog>
    </section>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: 'impCataloglist',
        data() {
            return {
                selectData: [],
                expSchemaId: '',
                status: '',
                sourceCode: ''
            }
        },
        methods: {
            //显示弹出框
            showDialog() {
                this.$http.post('/impexpscheme/page', {page: false}).then(({data}) => {
                    if (data && data.success) {
                        //只展示导入的
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
                if (['xml', 'xlsx', 'xls', 'dbf', 'accdb'].indexOf(FileExt.toLowerCase()) === -1) {
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
                this.$message.success('请在导入记录中查看导入详情!')
                this.dialogShow = false
            },
            submitUpload() {
                this.$refs.uploadFile.submit();
                this.progress = true
                this.timeout = setInterval(() => {
                    if (this.percent <= 99) {
                        const a = Math.round(Math.random() * 5 + 2)
                        this.percent = a + this.percent >= 100 ? 99 : a + this.percent
                    }
                }, 1000);
            },
            $init() {
                this.status = this.register.handoverStatus;
            },
        },
    }
</script>
