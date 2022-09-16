<template>
    <section>
        <el-dropdown placement="bottom" trigger="click" @command="handleCommand">
            <el-button class="search-btn" v-show="!this.parentIndex" type="primary">提交<i
                    class="el-icon-arrow-down el-icon--right"/></el-button>
            <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="currentSelect.length>0" command="select">提交选中</el-dropdown-item>
                <el-dropdown-item command="all">提交所有</el-dropdown-item>
                <el-dropdown-item command="query">提交查询</el-dropdown-item>
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
                sendType: 'all',
                type: '',
                flowType: '',
                flowStringName:'',
            }
        },
        created() {
            this.type = this.eventBus.defaultForm.status_info
            //加载流程信息
            this.flowPath()
        },
        methods: {
            handleCommand(command) {
                //提交退回钱刷新，防止修改后数据没有更新
                const check =  this.eventBus.$emit("loadData")
                this.sendType = command
                const query = this.eventBus.getQueryByQueryType(this.sendType)
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
             * 执行提交操作
             * @returns {Promise<void>}
             */
            async doSend() {
                const query = this.eventBus.getQueryByQueryType(this.sendType)
                //如果有父类 表示是卷内目录数据 需要联合提交
                if (this.parentIndex) {
                    const {data} = await this.$post('/register/updateStatus', {
                        type: this.flowType,
                        ...query
                    })
                    if (data.success) {
                        this.$message.success('提交成功，请在操作页查看结果！')
                        this.eventBus.$emit("loadData")
                    } else {
                        this.$message.error(data.message)
                    }
                } else {
                    const {data} = await this.$post('/register/lhUpdateType', {
                        type: this.flowType,
                        ...query
                    })
                    if (data.success) {
                        this.$message.success('正在提交到'+"  "+this.flowStringName+"!")
                        if (this.childrenIndex) {
                            this.childrenIndex.$emit("loadData")
                        }
                        //暂时先设置延时刷新。
                        let timer = setTimeout(() => {
                            this.eventBus.$emit("loadData")
                        }, 1000);
                        this.$once('hook:beforeDestroy', () => {
                            clearInterval(timer);
                        })
                    } else {
                        this.$message.error(data.message)
                    }
                }
                this.dialogShow = false
            },
            flowPath() {
                const query = this.eventBus.getQueryByQueryType(this.sendType)
                //拿当前的type去后台查询，返回下一个的type.
                this.$http.post('/fonddata/flowPath', {
                    fid: query.formDefinitionId,
                    type: this.type,
                    state: 1
                }).then(({data}) => {
                    if (data && data.success) {
                        //type给提交，用于提交，
                        this.flowType = data.data.flowBatchName
                        this.flowStringName = data.data.flowStringName
                    }
                })
                //退回功能，将当前type给后台，后台吧前面的都返回回来。
            }
        }
    }
</script>
