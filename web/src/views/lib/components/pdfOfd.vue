<template>
    <span>
        <el-button type="primary"  @click="hook">pdf转ofd</el-button>
        <el-dialog
                title="PDF转换OFD"
                :visible.sync="dialogVisible"
                width="40%">
            <div>
                <el-upload
                        class="inline-block"
                        style="text-align: center;margin-top: 50px"
                        ref="upload"
                        accept=".pdf"
                        :multiple="false"
                        :limit="1"
                        action="api/ofd/pdfConvertOfd"
                        :before-upload="beforeAvatarUpload"
                        :on-change="handleChange"
                        :before-remove="beforeRemove"
                        :on-exceed="handleExceed"
                        :on-success="Push"
                        :auto-upload="false"
                        :file-list="fileList"
                     >
                    <el-button class='margin-change' size="small" type="primary">点击选择PDF文件<i
                            class="el-icon-upload el-icon--right"></i></el-button>
                    <el-button size="small" style="margin-left: 10px;" type="success" @click.stop="confirmUpload">确认上传<i
                            class="el-icon-check"></i></el-button>
                    <el-button size="small" type="primary" v-if="ofdDownload" @click.stop="ofdDownloadF" plain>点击下载<i
                            class="el-icon-download"></i></el-button>
                    <div slot="tip" class="el-upload__tip">只能上传pdf文件</div>
                </el-upload>
            </div>
        </el-dialog>
    </span>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: "threeInOne",
        data() {
            return {
                dialogVisible: false,
                fileList: [],
                ofdDownload: false,
                url: "",
            }
        },

        methods: {
            beforeRemove() {
                this.url = "";
                this.ofdDownload = false;
            },
            async ofdDownloadF() {
                window.open("api/ofd/ofdDownload?ofdPath=" + encodeURI(this.url));
            },
            Push(res, file, fileList) {
                console.log(file)
                console.log(res)
                console.log(res.success)
                if (res.success) {
                    this.url = res.data
                    this.ofdDownload = true;
                }
                this.dialogShow = false
            },
            confirmUpload() { //确认上传
                this.url = "";
                this.ofdDownload = false;
                this.$refs.upload.submit();
            },
            handleChange(file, fileList) {
                this.fileList = fileList;
            },
            beforeAvatarUpload(file) {
                var files = file.name.substring(file.name.lastIndexOf('.') + 1)
                const isPDF = files.toLowerCase() === "pdf";
                if (!isPDF) {
                    this.$message.error('上传文件只能是 PDF 格式!');
                }
                return isPDF;
            },
            handleExceed(files, fileList) {
                this.$message.warning(`当前限制选择 1 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
            },

            hook() {
                this.dialogVisible = true
            },
        },
    }
</script>

<style scoped>
    .dialogdiv {
        height: 60vh;
        overflow: auto;
    }

    .inline-block {
        display: inline-block;
        margin-right: 10px;
    }

    .margin-change {
        display: inline-block;
        margin-left: 10px;
    }

    .center-div {
        height: 60vh;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);

    }
</style>
