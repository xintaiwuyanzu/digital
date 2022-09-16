<template>
  <section>
    <el-button type="primary" size="mini" v-show="this.category.registerId!=''"
               @click="resultTest">
      成果检测
    </el-button>
  </section>
</template>
<script>
import abstractColumnComponent from "./abstractColumnComponent";
export default {
  extends: abstractColumnComponent,
  name: 'resultTest',
  data() {
    return {
    }
  },
  methods: {
    resultTest() {
      console.log(this.category.registerId)
      this.$router.push({
        path:'resultTest',
        query:{
          registerId:this.category.registerId,
          formDefinitionId:this.formId
        }
      })
    },

  }
}
</script>