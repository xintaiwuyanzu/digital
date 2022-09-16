<template>
  <el-tooltip content="首页" placement="bottom" effect="light">
    <el-button circle @click="home()">
      <icon icon="home" style="width: 20px"/>
    </el-button>
  </el-tooltip>
</template>
<script>
export default {
  methods: {
    home() {
      this.$router.push('/home')
    }
  }
}
</script>
