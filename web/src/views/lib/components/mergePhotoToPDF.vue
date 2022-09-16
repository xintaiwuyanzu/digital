<template>
    <section>
        <el-dropdown
                placement="bottom"
                trigger="click"
                @command="handleCommand">
            <el-button class="search-btn" type="primary" v-show="!this.parentIndex">合并pdf<i
                    class="el-icon-arrow-down el-icon--right"/></el-button>
            <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="currentSelect.length>0" command="select">合并选中</el-dropdown-item>
                <el-dropdown-item command="all">合并所有</el-dropdown-item>
                <el-dropdown-item command="query">合并查询</el-dropdown-item>
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
                //如果有子类 表示的案卷目录 需要联合转换
                //如果没有子类 表示是卷内目录数据直接转换即可
                if (this.childrenIndex) {
                    const {data} = await this.$post('/uploadfiles/photoMergeToPDF', {
                        type: type,
                        formDefinitionId: this.formId,
                        registerId: this.category.id,
                        childFormId: this.childrenIndex.formId,
                        ...query
                    })
                    if (data.success) {
                        this.eventBus.$emit("loadData")
                        if (this.childrenIndex) {
                            this.childrenIndex.$emit("loadData")
                        }
                        this.$message.success(data.message)
                    } else {
                        this.$message.error(data.message)
                    }
                } else {
                    const {data} = await this.$post('/uploadfiles/photoMergeToPDF', {
                        type: type,
                        formDefinitionId: this.formId,
                        registerId: this.category.id,
                        ...query
                    })
                    if (data.success) {
                        this.eventBus.$emit("loadData")
                        this.$message.success('转换成功，请在操作页查看结果！')
                    } else {
                        this.$message.error(data.message)
                    }
                }
                this.loading = false
                this.dialogShow = false
            }
        }
    }
</script>