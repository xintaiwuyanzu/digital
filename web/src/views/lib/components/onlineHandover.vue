<template>
    <section>
        <el-button type="primary" size="mini" v-show="!this.parentIndex && this.status != '2'  " @click="onDelete">
            移交
        </el-button>
    </section>
</template>
<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        name: 'onlineHandover',
        data() {
            return {
                sendType: 'all',
                status: '',
            }
        },
        methods: {
            $init() {
                this.status = this.register.handoverStatus;
            },
            async onDelete() {
                const query = this.eventBus.getQueryByQueryType(this.sendType)
                await this.$confirm('执行在线移交?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                const {data} = await this.$http.post("/packetsData/onlineHandover", {
                    registerId: this.category.registerId,
                    formDefinitionId: this.formId,
                    ...query
                })
                if (data.success) {
                    this.eventBus.$emit("loadData")
                    this.status = '2'
                    this.$message.success(data.data)
                } else {
                    this.$message.success(data.message)
                }
                this.dialogShow = false
            },

        }
    }
</script>