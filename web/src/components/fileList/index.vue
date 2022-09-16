<template>
    <section>
        <div class="index_main">
            <el-dialog :title="title" :visible.sync="drawer" align="center" @close="$parent.fileListDialog = false"
                       append-to-body>
                <upload-form ref="upload" @func="getConfigScheme" :formDataId="formDataId"
                             :ref-type="refType"/>
                <el-button type="primary" size="mini" @click="getConfig" style="margin-bottom: 10px" v-if="upload">
                    {{ uploadText }}
                </el-button>
                <div class="table-container" style="height: 60vh">
                    <list-content ref="loadData" v-on:toPrint="toPrint" :ref-id="formDataId" :ref-type="refType"
                                  :group-code="groupCode"
                                  :useType="useType" :deleter="deleter" :print="print" :transform="transform"/>
                </div>
            </el-dialog>
        </div>
    </section>
</template>

<script>
  import UploadForm from './upload'
  import listContent from './content'
  import indexMixin from '@/util/indexMixin'

  export default {
        mixins: [indexMixin],
        components: {UploadForm, listContent},
        data() {
            return {
                drawer: true
            }
        },
        props: {
            formDataId: {type: String},
            refType: {default: 'default'},
            groupCode: {default: 'default'},
            //是否显示上传按钮
            upload: {default: true},
            //是否显示打印按钮
            print: {default: false},
            //是否显示删除按钮
            deleter: {default: true},
            //是否显示转换按钮
            transform: {default: true},
            useType: {type: Boolean},
            //dialog的标题
            title: {default: '原文列表'},
            //上传按钮的标题
            uploadText: {default: '上传原文'}
        },
        methods: {
            getConfig() {
                setTimeout(() => {   //设置延迟执行
                    this.$refs.upload.getConfigScheme()
                }, 1000)
            },
            getConfigScheme() {
                if (!this.formDataId) {
                    this.$message.error("请选择一项信息!")
                    return
                } else {
                    this.$refs.loadData.loadData()
                    this.fileList = []
                }
            },
            toPrint(v) {
                this.$emit("toPrint", v)
            }
        }
    }
</script>






