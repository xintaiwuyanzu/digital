<template>
    <section>
        <el-dropdown
                placement="bottom"
                trigger="click"
                @command="handleCommand">
            <el-button class="search-btn" type="primary" v-show="!this.parentIndex">OFD转换
                <i class="el-icon-arrow-down el-icon--right"/></el-button>
            <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="currentSelect.length>0" command="select">转换选中</el-dropdown-item>
                <el-dropdown-item command="all">转换所有</el-dropdown-item>
                <el-dropdown-item command="query">转换查询</el-dropdown-item>
            </el-dropdown-menu>
        </el-dropdown>
    </section>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: 'submitting',
        data() {
            return {
                targetPerson: '',
                sendType: 'all'
            }
        },
        methods: {
            handleCommand(command) {
                this.sendType = command
                this.doSend()
            },
            /**
             * 显示提报dialog
             */
            async showSend() {
                this.dialogShow = true
            },
            /**
             *
             * 执行转换操作
             * @returns {Promise<void>}
             */
            async doSend() {
                const query = this.eventBus.getQueryByQueryType(this.sendType)
                const type = this.IfStatus(this.eventBus.defaultForm.status_info)
                this.loading = true
                const {data} = await this.$post('/uploadfiles/fileSplitAndMergeAll', {
                    type: type,
                    formDefinitionId: this.formId,
                    registerId: this.fond.registerId,
                    ...query
                })
                if (data.success) {
                    this.eventBus.$emit("loadData")
                    this.$message.success('转换成功，请在操作页查看结果！')
                } else {
                    this.$message.error(data.message)
                }
                this.loading = false
                this.dialogShow = false
            }
        }
    }
</script>
