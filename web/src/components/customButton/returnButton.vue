<template>
  <span>
        <el-button type="primary" @click="dialogFormVisible = true">回退</el-button>

       <el-dialog title="任务退回" :visible.sync="dialogFormVisible">
            <el-form :model="form">
                <el-form-item label="环节选择：" :label-width="formLabelWidth">
                    <el-select v-model="form.region" placeholder="请选择环节名称">
                        <el-option v-for="item in linklist" :key="item.index" :label="item.label"
                                   :value="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="退回原因：" :label-width="formLabelWidth">
                    <el-input v-model="form.names" :rows="2" type="textarea"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button type="primary" @click="submit()">确 定</el-button>
            </div>
        </el-dialog>
    </span>
</template>

<script>
export default {
  name: "returnButton",
  props: ['id', 'query', 'types', 'formDefinitionId'],
  data() {
    return {
      dialogFormVisible: false,
      form: {
        names: '',
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
      formLabelWidth: '120px',


    }
  },
  created() {
    //旧版
    // this.startData()
    this.returnFlowPath()
    //this.status = this.register.handoverStatus;
  },
  methods: {
    startData() {//流程控制，不能选自己流程后面的。实现回退功能
      let admin = this.linklist.findIndex(item => {//需要查询到数组的编号，或者该流程的type.
        if (item.value == this.types) {
          return true
        }
      })
      const list = this.linklist.filter(item => item.index < admin)
      this.linklist = list
    },
    //退回
    async submit() {
      this.$post('/register/updateStatus', Object.assign(this.query, {
        type: this.form.region.value,
        id: this.id,
        remarks:this.form.names
      })).then(({data}) => {
        if (data.success) {
          this.$emit('update:id', "");
          //debugger
          this.$emit('toDetail','');
          //this.$parent.loadOne("up");
          this.$message.success('退回成功，退回'+this.form.region.label)
          this.dialogFormVisible = false
        } else {
          this.$message.error(data.message)
          this.dialogFormVisible = false
        }
      })
    },
    returnFlowPath() {
      //拿当前的type去后台查询，返回下一个的type.
      this.$http.post('/fonddata/flowPath', {
        fid: this.query.formDefinitionId,
        type: this.types,
        state: 2
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
        }
      })
    }
  },
}
</script>

