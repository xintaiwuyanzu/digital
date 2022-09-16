<template>
    <section>
        <el-dialog title="原文上传" :visible.sync="imports" :modal-append-to-body='false' :append-to-body="true">
            <el-upload style="text-align: center;margin-top: 50px"
                       class="upload-demo"
                       ref="uploadFiles"
                       action="api/files/upload"
                       :before-upload="beforeUpload"
                       :on-success="Push"
                       :on-error="error"
                       accept="*"
                       multiple
                       :limit="1"
                       :headers="myHeader"
                       :data="{
                          refId:this.formDataId,
                          refType:this.refType
                       }"
                       :on-exceed="handleExceed"
                       :auto-upload="false"
                       :file-list="fileList">
                <el-button slot="trigger" size="medium" type="primary" icon="el-icon-search">选取原文</el-button>
                <el-button style="margin-left: 10px;" size="medium" type="success" @click="submitUpload">
                    <i class="el-icon-upload el-icon--right"/>上传原文
                </el-button>
                <div slot="tip" class="el-upload__tip">原文无格式限制</div>
            </el-upload>
        </el-dialog>
    </section>

</template>

<script>
  import fromMixin from '@/util/formMixin'

  export default {
        data() {
            return {
                imports: false,
                fileList: [],
                myHeader: {
                    $token: sessionStorage.getItem('$token')
                },
            }
        },
        methods: {
            getConfigScheme() {
                if (!this.formDataId) {
                    this.$message.error("请选择一项信息!")
                    return
                } else {
                    this.imports = true
                    this.fileList = []
                }
            },
            cancel() {
                this.imports = false
            },
            Push(val) {
                if (val.success) {
                    this.$message.success("导入成功!")
                    this.imports = false
                    this.drawer = false
                    this.$emit('func')
                } else {
                    this.$message.error(val.message)
                }
            },
            error() {
                this.$message.error("导入失败!")
                return
            },
            submitUpload() {
                this.$refs.uploadFiles.submit();
            },
            handleExceed(files, fileList) {
                this.$message.warning(`当前限制选择 1 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
            },
            beforeUpload(file) {
                file.name.replace(/.+\./, "");
            }
        },
        props: {
            formDataId: String,
            refType: {default: 'default'},
            groupCode: {default: 'default'}
        },
        mixins: [fromMixin]
    }
</script>
