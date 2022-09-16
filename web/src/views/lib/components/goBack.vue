<template>
    <section>
        <el-dropdown
                placement="bottom"
                trigger="click"
                @command="handleCommand">
            <el-button class="search-btn" v-show="!this.parentIndex && this.status != '2' " type="primary">退回<i
                    class="el-icon-arrow-down el-icon--right"/></el-button>
            <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="currentSelect.length>0" command="select">退回选中</el-dropdown-item>
                <el-dropdown-item command="all">退回全部</el-dropdown-item>
            </el-dropdown-menu>
        </el-dropdown>
        <el-dialog title="任务退回" :visible.sync="dialogFormVisible">
            <el-form :model="form">
                <el-form-item label="环节选择：" :label-width="formLabelWidth">
                    <el-select v-model="form.region" placeholder="请选择环节名称">
                        <el-option v-for="item in linklist" :key="item.index" :label="item.label"
                                   :value="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="退回原因：" :label-width="formLabelWidth">
                    <el-input v-model="form.name" :rows="2" type="textarea"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button type="primary" @click="onSubmit(form)">确 定</el-button>
            </div>
        </el-dialog>
    </section>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: 'goBack',
        data() {
            return {
                targetPerson: '',
                sendType: 'all',
                dialogFormVisible: false,
                form: {
                    name: '',
                    region: '',
                },
                linklist: [
                    {index: "0", label: "任务登记", value: 'RECEIVE'},
                    {index: "1", label: "图像扫描", value: 'SCANNING'},
                    {index: "2", label: "图像处理", value: 'PROCESSING'},
                    {index: "3", label: "图像质检", value: 'IMAGES'},
                    {index: "4", label: "手动拆件", value: 'WSSPLIT'},
                    {index: "5", label: "档案著录", value: 'VOLUMES'},
                    {index: "6", label: "初检", value: 'QUALITY'},
                    {index: "7", label: "复检", value: 'RECHECK'},
                    {index: "8", label: "数字化成果", value: 'OVER'},
                ],
                status: '',
                formLabelWidth: '120px'
            }
        },
        created(){
          this.type = this.eventBus.defaultForm.status_info
          //加载流程信息
          this.returnFlowPath()
        },
        methods: {
            $init() {
                this.status = this.register.handoverStatus;
            },
            handleCommand(command) {
                if (this.register.handoverStatus != '2') {
                    this.dialogFormVisible = true
                    this.sendType = command
                    const status = this.eventBus.defaultForm.status_info
                    let admin = this.linklist.findIndex(item => {
                        if (status == item.value) {
                            return true
                        }
                    })
                    const list = this.linklist.filter(item => item.index < admin)
                    //this.linklist = list
                } else {
                    this.$message.error("当前处于移交状态不能退回！")
                }
            },
            async onSubmit(form) {
                const query = this.eventBus.getQueryByQueryType(this.sendType)
                const type = form.region.value
                const remarks = form.name
                if (form.region.value != '') {
                    let ids = ''
                    for (let i = 0; i < this.currentSelect.length; i++) {
                        ids += this.currentSelect[i].id + ','
                    }
                    let status = form.region.value
                    //如果有父类 表示是卷内目录数据 需要联合提交
                    if (this.parentIndex) {
                        const {data} = await this.$post('/manage/formData/updateStatus', {
                            ids: ids,
                            status: status,
                            formDefinitionId: this.formId,
                            remarks:remarks,
                            ...query
                        })
                        if (data.success) {
                            this.eventBus.$emit("loadData")

                        } else {
                            this.$message.error(data.message)
                        }
                    } else {
                        const {data} = await this.$post('/register/lhUpdateType', {
                            type: type,
                            remarks:remarks,
                            ...query
                        })
                        if (data.success) {
                            this.eventBus.$emit("loadData")
                            if (this.childrenIndex) {
                                this.childrenIndex.$emit("loadData")
                            }
                            this.$message.success('正在退回！退回到'+form.region.label)
                          //暂时先设置延时刷新。
                          let timer = setTimeout(() => {
                            this.eventBus.$emit("loadData")
                          }, 1000);
                          this.$once('hook:beforeDestroy', () => { clearInterval(timer); })
                        } else {
                            this.$message.error(data.message)
                        }
                    }
                    this.dialogFormVisible = false
                } else {
                    this.$message.success("请选择需要分配的环节！")
                }
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
                this.loading = true
                const status = this.backStatus(this.eventBus.defaultForm.status_info)
                let ids = ''
                for (let i = 0; i < this.currentSelect.length; i++) {
                    ids += this.currentSelect[i].id + ','
                }
                //如果有子类 表示的案卷目录 需要联合提交
                //如果没有子类 表示是卷内目录数据直接提交即可
                if (this.childrenIndex) {
                    const {data} = await this.$post('/register/lhUpdateStatus', {
                        ids: ids,
                        status: status,
                        formDefinitionId: this.formId,
                        childFormId: this.childrenIndex.formId,
                        registerId: this.category.id,
                    })
                    if (data.success) {
                        this.eventBus.$emit("loadData")
                        if (this.childrenIndex) {
                            this.childrenIndex.$emit("loadData")
                        }
                        this.$message.success('提交成功，请在操作页面查看结果！')
                    } else {
                        this.$message.error(data.message)
                    }
                } else {
                    const {data} = await this.$post('/manage/formData/updateStatus', {
                        ids: ids,
                        status: status,
                        formDefinitionId: this.formId,
                    })
                    if (data.success) {
                        this.eventBus.$emit("loadData")
                        this.$message.success('提交成功，请在操作页面查看结果！')
                    } else {
                        this.$message.error(data.message)
                    }
                }
                this.loading = false
                this.dialogShow = false
            },
            returnFlowPath(){
              const query = this.eventBus.getQueryByQueryType(this.sendType)
              //拿当前的type去后台查询，返回下一个的type.
              this.$http.post('/fonddata/flowPath', {
                fid: query.formDefinitionId,
                type:this.type,
                state:2
              }).then(({data}) => {
                if (data && data.success) {
                  let list = data.data
                  //退回功能，将当前type给后台，后台吧前面的都返回回来。
                  const asList = this.linklist.filter(item => {
                    if (list.includes(item.value)) {
                      return true
                    }
                  })
                  this.linklist = asList
                }})

            }
        }
    }
</script>
