<template>
    <section>
        <el-link type="success" @click="onFileList">
            <div v-html="'原文'"></div>
        </el-link>
        <file-list v-on:uploadYuanwen="updateStatusYuanwen" refType="archive" :formDataId="row.id"
                   style="margin-top: 5px"
                   width="50%" v-if="fileListDialog"/>
    </section>
</template>

<script>
  import abstractColumnComponent from "./abstractColumnComponent";

  export default {
        extends: abstractColumnComponent,
        name: 'yuanwen',
        data() {
            return {
                fileListDialog: false
            }
        },
        mounted() {
            this.$on('uploadYuanwen', this.updateStatusYuanwen)
        },
        methods: {
            onFileList() {
                this.fileListDialog = true
            },
            updateStatusYuanwen() {
                let param = {'categoryId': this.category.id, 'id': this.row.id, 'formId': this.formId}
                this.$http.post('/manage/formData/updateHaveYuanwen', param).then(
                    ({data}) => {
                        this.loading = false
                        if (data.success) {
                            this.row.yw_have = data.data
                        } else {
                            this.$message.error("更新原文信息失败")
                        }
                    })
            }
        }
    }
</script>
