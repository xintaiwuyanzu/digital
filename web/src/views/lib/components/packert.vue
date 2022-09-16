<template>
    <section>
        <el-button type="success" size="mini" v-show="this.childrenIndex" @click="onDelete">数据包
        </el-button>
    </section>
</template>
<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        name: 'recycle',
        methods: {
            async onDelete() {
                try {
                    await this.$confirm('打包?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    const {data} = await this.$http.post("/packetsData/packet", {
                        formDefinitionId: this.formId,
                        formDataId: this.row.id
                    })
                    if (data.success) {
                        this.$message.success(data.data)
                    } else {
                        this.$message.success(data.message)
                    }
                    this.dialogShow = false
                } catch (e) {
                  
                }
            }
        }
    }
</script>
