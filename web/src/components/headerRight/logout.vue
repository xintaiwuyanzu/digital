<template>
  <el-tooltip content="退出登录" placement="bottom">
    <section class="top_icon" @click="logout()">
      <icon icon="delete"/>
    </section>
  </el-tooltip>
</template>

<script>
import loginUtil from '@dr/framework/src/components/login/util'

export default {
  methods: {
    logout() {
      this.$confirm("确定要退出登录吗？", '提 醒', {type: 'waring'})
          .then(() => {
            loginUtil.cleanToken()
            this.$message.success('退出登录成功！')
            this.$router.push('/login')
          })
    }
  }
}
</script>
