<template>
    <el-button type="primary" @click="taggingAdd">{{title}}</el-button>
</template>

<script>
export default {
    name: "index",
    props: ['id', 'query', 'types', 'formDefinitionId', 'obj'],
    data() {
        return {
            sonObj: {},
            title:"标注"
        }
    },
    methods: {
      $init() {
        this.taggingTitle()
      },
     async taggingTitle() {
        if (this.obj.formDefinitionId){
          await this.$http.post("/wsSplit/taggingType", this.obj).then(({data}) => {
            if (!data){
              this.title = "标注"
            }else {
              this.title = "取消标注"
            }
          })
        }

      },
      taggingAdd() {
        /*添加&&修改*/
        if (this.obj.formDefinitionId){
          if ( this.title == "标注"){
            this.obj.wssplitTaggingCondition = 1;
            this.$http.post("/wsSplit/splitWsTaggingType", this.obj).then(({data}) => {
              if (data&&data.success) {
                this.$message.success("操作成功")
                this.taggingTitle()
              } else {
                this.$message.error(data.message);
              }
            })
          }else {
            this.obj.wssplitTaggingCondition = 0;
            this.$http.post("/wsSplit/splitWsTaggingType", this.obj).then(({data}) => {
              if (data&&data.success) {
                this.$message.success("操作成功")
                this.taggingTitle()
              } else {
                this.$message.error(data.message);
              }
            })
          }
        }



      }
    }
}
</script>

<style scoped>

</style>