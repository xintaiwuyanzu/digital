<template>
    <section>
        <el-dropdown
                placement="bottom"
                trigger="click"
                @command="packertAll">
            <el-button class="search-btn" v-show="!this.parentIndex " type="primary">打包<i
                    class="el-icon-arrow-down el-icon--right"/></el-button>
            <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="currentSelect.length > 0" command="select">打包选中</el-dropdown-item>
                <el-dropdown-item command="all">打包所有</el-dropdown-item>
                <el-dropdown-item command="query">打包查询</el-dropdown-item>
            </el-dropdown-menu>
        </el-dropdown>
    </section>
</template>
<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        name: 'recycle',
        methods: {
            async packertAll(command) {
                let formDataId = ''
                if (command == 'select') {
                    formDataId = this.currentSelect.map(item => item.id).join(",")
                }
                const query = this.eventBus.getQueryByQueryType(command)
                try {
                    await this.$confirm('确定封包吗?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    const {data} = await this.$http.post("/packetsData/packetAll", {
                        formDefinitionId: this.formId,
                        formDataId: formDataId,
                        command: command,
                        registerId: this.registerId,
                        ...query
                    })
                    if (data.success) {
                        this.$message.success("正在打包")
                    } else {
                        this.$message.success(data.message)
                    }
                    this.dialogShow = false
                } catch (e) {

                }
            },
        }
    }
</script>
