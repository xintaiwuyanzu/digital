<template>
  <section>
    <el-tooltip content="个人信息" placement="bottom">
      <section class="top_icon" @click="getChange()">
        <icon icon="user"/>
      </section>
    </el-tooltip>
    <change-form ref="changeform"/>
  </section>
</template>
<script>
import changeForm from "./changeForm";

export default {
  components: {changeForm},
  methods: {
    getChange() {
      this.$http.post('/login/info')
          .then(({data}) => {
            if (data.success) {

              this.$refs.changeform.editForm(data.data)
            } else {
              this.$message.error(data.message)
            }
          })
    }
  }
}
</script>
