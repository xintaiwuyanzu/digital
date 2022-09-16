<template>
  <el-tooltip content="门户" placement="bottom">
    <section class="top_icon" @click="goPorTalSystem()">
      <icon icon="home"/>
    </section>
  </el-tooltip>
</template>

<script>
export default {
  name: "portalSystem",
  methods: {
    async goPorTalSystem() {
      const {data} = await this.$http.post('/portal/getPortalSystem')
      if (data && data.success) {
        window.open(data.data, "_self")
      }
    }
  }
}
</script>

<style scoped>

</style>