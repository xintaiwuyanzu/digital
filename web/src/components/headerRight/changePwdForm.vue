<template>
  <el-dialog :visible.sync="edit" :title="'修改密码'" width="40%" :close-on-click-modal="false">
    <el-form :model="form" :rules="rules" ref="form" label-width="100px">
      <el-form-item label="旧密码：" prop="oldPwd">
        <el-input v-model="form.oldPwd" placeholder="请输入旧密码" show-password/>
      </el-form-item>
      <el-form-item label="新密码：" prop="newPwd">
        <el-input v-model="form.newPwd" placeholder="请输入新密码" show-password/>
      </el-form-item>
      <el-form-item label="确认密码：" prop="confirmPwd">
        <el-input v-model="form.confirmPwd" placeholder="请再次输入新密码" show-password/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="cancel">取 消</el-button>
      <el-button type="primary" @click="changePwd()" v-loading="loading">确 定</el-button>
    </div>
  </el-dialog>
</template>
<script>
import fromMixin from '@/util/formMixin'
import loginUtil from '@dr/framework/src/components/login/util'
import {SM4} from "gm-crypto";

const sm4Key = '617263686976655f66757a686f756c63'
export default {
  data() {
    return {
      form: {
        oldPwd: '',
        newPwd: '',
        confirmPwd: '',
      },
      rules: {
        oldPwd: [{required: true, message: '请输入旧密码', trigger: 'change'}],
        newPwd: [{required: true, message: '请输入新密码', trigger: 'change'}],
        confirmPwd: [{required: true, message: '请输入确认密码', trigger: 'change'}],
      },
    }
  },
  props: {
    userId: {type: String}
  },
  methods: {
    changePwd() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.newPwd === this.form.confirmPwd) {
            this.$http.post('/ssoLogin/changePassword', {
              adminId: this.securityPassword(this.userId),
              oldPwd: this.securityPassword(this.form.oldPwd),
              newPwd: this.securityPassword(this.form.newPwd)
            })
                .then(({data}) => {
                  if (data.success) {
                    this.form = data.data
                    this.$message({
                      showClose: true,
                      message: '修改成功，请重新登录。',
                      type: 'success'
                    });
                    this.logout()
                  } else {
                    this.$message.error("旧密码输入错误")
                  }
                })
          } else {
            this.$message.error("新密码两次输入不一致！")
          }
        }
      })

    },
    logout() {
      loginUtil.cleanToken()
      this.$router.push('/login')
    },
    /**
     * 加密密码
     * @param password
     * @returns {string}
     */
    securityPassword(password) {
      return SM4.encrypt(password, sm4Key, {
        mode: SM4.constants.ECB,
        inputEncoding: 'utf-8',
        outputEncoding: 'hex'
      })
    }
  },
  mixins: [fromMixin]
}
</script>
