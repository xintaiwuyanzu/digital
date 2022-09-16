<template>
  <section>
    <div style="float: right">
      <el-dropdown
          placement="bottom"
          trigger="click"
          @command="handleCommand">
        <el-button class="search-btn" v-show="!this.parentIndex && this.status != '2' " type="primary">任务下发<i
            class="el-icon-arrow-down el-icon--right"/></el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-if="currentSelect.length>0" command="select">分配选中</el-dropdown-item>
          <el-dropdown-item command="all">分配全部</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
    <el-dialog title="任务下发" :visible.sync="dialogFormVisible">
      <el-form :model="form">
        <el-form-item label="环节选择" :label-width="formLabelWidth">
          <el-select v-model="form.region" placeholder="请选择环节名称">
            <el-option v-for="item in linklist" :key="item.index" :label="item.label"
                       :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" :label-width="formLabelWidth">
          <el-input v-model="form.name" :rows="2" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="onSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import abstractComponent from "./abstractComponent";

export default {
  extends: abstractComponent,
  name: "fenFA",
  data() {
    return {
      dialogFormVisible: false,
      sendType: 'all',
      form: {
        name: '',
        region: '',
      },
      formLabelWidth: '120px',
      status: '',
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
    };
  },
  methods: {
    $init() {
      this.status = this.register.handoverStatus;
      this.returnFlowPath()
    },
    handleCommand(command) {
      this.dialogFormVisible = true
      this.sendType = command
    },
    async onSubmit() {
      const query = this.eventBus.getQueryByQueryType(this.sendType)
      const type = this.form.region
      if (this.form.region != '') {
        let ids = ''
        for (let i = 0; i < this.currentSelect.length; i++) {
          ids += this.currentSelect[i].id + ','
        }
        let status = this.form.region
        //如果有父类 表示是卷内目录数据 需要联合提交
        if (this.parentIndex) {
          const {data} = await this.$post('/manage/formData/updateStatus', {
            ids: ids,
            status: status,
            formDefinitionId: this.formId,
            ...query
          })
          if (data.success) {
            this.eventBus.$emit("loadData")
            this.$message.success('提交成功，请在操作页面查看结果！')
          } else {
            this.$message.error(data.message)
          }
        } else {
          const {data} = await this.$post('/register/lhUpdateType', {
            type: type,
            ...query
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
        }
        this.dialogFormVisible = false
      } else {
        this.$message.success("请选择需要分配的环节！")
      }
    },
    returnFlowPath(){
      const query = this.eventBus.getQueryByQueryType(this.sendType)
      //拿当前的type去后台查询，返回下一个的type.
      this.$http.post('/fonddata/flowPathAll', {
        fid: query.formDefinitionId,
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
  },
}
</script>

