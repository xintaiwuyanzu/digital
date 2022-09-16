<template>
  <div>
    <el-dialog :visible.sync="edit" :title="'个人信息'" width="30%" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px" style="color: #00397f">
        <el-row style="max-width: content-box">
          <el-form-item label="用户姓名：">
            <span>{{ form.userName }}</span>
            <!--<el-button style="border: none">→去修改</el-button>-->
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item label="用户编号：">
            <span>{{ form.userCode }}</span>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item label="手机号：">
            <span>{{ form.mobile }}</span>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item label="身份证号：">
            <span>{{ form.idNo }}</span>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item label="邮   箱：">
            <span>{{ form.email }}</span>
          </el-form-item>
        </el-row>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel" type="primary">关闭</el-button>
        <el-button type="primary" @click="changePwd()" v-loading="loading">修改密码</el-button>
      </div>
    </el-dialog>
    <change-pwd-form :userId="form.id" ref="pwd"></change-pwd-form>
  </div>
</template>
<script>
import indexMixin from '@/util/indexMixin'
import fromMixin from '@/util/formMixin'
import changePwdForm from "@/components/headerRight/changePwdForm"
export default {
  mixins: [indexMixin, fromMixin],
  components: {changePwdForm},
  data() {
    return {
      editPwd: false,
      libIds: [],
      form: {},
      dict: ['loc.type'],
      libName: '',
      rules: {
        oldPwd: [{required: true, message: '请输入旧密码', trigger: 'blur'}],
        newPwd: [{required: true, message: '请输入新密码', trigger: 'blur'}],
        confirmPwd: [{required: true, message: '请输入确认密码', trigger: 'blur'}],
      }
    }
  },
  methods: {
    changePwd() {
      this.$refs.pwd.edit = true
    },
    saveForm() {
      if (this.$refs.form) {
        this.loading = true
        this.$refs.form.validate(valid => {
          if (valid) {
            let path = '/gestion/update'
            this.$http.post(path, this.form).then(({data}) => {
              if (data && data.success) {
                this.form = data.data
                this.$message.success('保存成功！')
              } else {
                this.$message.error(data.message)
              }
              this.loading = false
            })
          } else {
            this.loading = false
          }
        })
      }
    }
  },
}
</script>
