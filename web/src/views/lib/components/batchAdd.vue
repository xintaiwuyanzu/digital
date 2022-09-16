<template>
  <section>
    <el-button type="primary" @click="showAdd" class="btn-submit">批量添加</el-button>
    <el-dialog title="批量添加" :visible.sync="dialogFormVisible">
      <el-form :model="inform" ref="inform" :rules="informRules">
        <el-form-item label="全宗号" :label-width="formLabelWidth" prop="fonds_identifier">
          <el-input v-model="inform.fonds_identifier" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="起始档号" :label-width="formLabelWidth" prop="homeCode">
          <el-input v-model="inform.homeCode" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="终止档号" :label-width="formLabelWidth" prop="endCode">
          <el-input v-model="inform.endCode" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="info" @click="offShow" class="btn-cancel">关 闭</el-button>
        <el-button type="primary" @click="saveBatch('inform')" class="btn-submit">批量添加</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import abstractComponent from "./abstractComponent";

export default {
  extends: abstractComponent,
  name: "batchAdd",

  data() {
    return {
      dialogFormVisible: false,
      formLabelWidth: '20%',
      fonds_identifier: '',
      /*endArchivalCode: '',//结束档号.
      homeArchivalCode: '',//起始档号*/
      inform: {
        homeCode: '',
        endCode: '',
        fonds_identifier: "",
      },
      informRules: {
        fonds_identifier: [
          {required: true, message: '请输入全宗号', trigger: 'blur'},
          {pattern: /^(\-|\+)?\d+?$/, message: '请输入正确的数字', trigger: 'blur'}],
        homeCode: [
          {required: true, message: '请输入起始档号', trigger: 'blur'}],
        endCode: [
          {required: true, message: '请输入截至档号', trigger: 'blur'},]
      },
    }
  },
  methods: {
    $init() {
      this.status = this.register.handoverStatus;
    },
    //显示弹出框
    showAdd() {
      this.categoryId = this.category.id
      this.fondId = this.fond.id
      this.dialogFormVisible = true
    },
    offShow() {
      this.dialogFormVisible = false;
      this.$refs.inform.resetFields();
    },
    /**
     * 保存方法
     */
    saveBatch(formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          //校验
          let start = this.inform.homeCode
          let finish = this.inform.endCode
          start = start.substring(start.lastIndexOf("-") + 1)
          finish = finish.substring(finish.lastIndexOf("-") + 1)
          if (this.inform.homeCode.indexOf("-") === -1 || this.inform.endCode.indexOf("-") === -1) {
            this.$message({
              message: '请正确输入档号',
              type: 'warning'
            });
          } else if (start == "" || finish == "" || start == null || finish == null) {
            this.$message({
              message: '输入的档号格式有误，请检查',
              type: 'warning'
            });
          } else if (isNaN(start) || isNaN(finish)) {
            this.$message({
              message: '请正确输入档号格式',
              type: 'warning'
            });
          } else if (finish - start <= 0 || Number(finish) == 0 || Number(start) == 0) {
            this.$message({
              message: '起始档号不能大于截至档号,并且至少添加两条数据。如需添加一条数据请移步添加',
              type: 'warning'
            });
          } else {
            //准备参数
            const defaultParams = {
              //这几个参数是必填的
              formDefinitionId: this.formId,
              //全宗Id
              fondId: this.fond.id,
              //分类Id
              categoryId: this.category.id,
              //状态
              status: this.eventBus.defaultForm.status_info,
              fonds_identifier: this.inform.fonds_identifier,
              //截至档号
              endArchivalCode: this.inform.endCode,
              //起始档号
              homeArchivalCode: this.inform.homeCode,
              //门类代码
              archivers_category_code: this.category.code,
            }
            const url = `/manage/formData/insertFormBatchData`
            const data = await this.$post(url, Object.assign(defaultParams, this.eventBus.defaultForm))
            if (data.data.success) {
              this.eventBus.$emit("loadData")
              this.$message.success("保存成功！")
              this.dialogFormVisible = false
              this.dialogShow = false
            }else {
              this.$message({
                message: data.data.message.replace("服务器错误：",""),
                type: 'warning'
              });
            }
          }
        } else {
          console.log('error submit!!');
          return false;
        }
      })
    },
  }
}
</script>

<style scoped>

</style>
