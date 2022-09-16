<template>
    <section>
        <el-link type="primary" @click="uniquenessJudge" v-show="!this.parentIndex">档案著录</el-link>
    </section>
</template>

<script>
import abstractColumnComponent from "./abstractColumnComponent";

export default {
    extends: abstractColumnComponent,
    name: 'volumeEntry',
    methods: {
      messageInit(formData){
        let type = 1;
        if (this.parentIndex) {
          type = 2
          this.message.formId = this.childrenIndex.formId;
          this.message.ajFormId = this.formId;
        } else {
          this.message.formId = this.formId;
          this.message.ajFormId = this.formId;
        }
        this.message.rtype = this.row.status_info//
        if (formData){
          this.message.id = formData.id;
        }else {
          this.message.id = this.row.id;
        }
        this.message.FondId = this.fond.id;
        this.message.ajFormId = this.formId;
        this.message.ajFondId = this.fond.id;
        this.message.registerId = this.fond.registerId;
        if (formData){
          this.message.dangHao = formData.archival_code;
        }else {
          this.message.dangHao = this.row.archival_code;
        }
        this.message.type = type;
        this.message.code = this.category.code;
      },
      async defaultJump(){
        const _query = this.eventBus.getQueryByQueryType('query')
        const t = this.IfStatus(this.eventBus.defaultForm.status_info)
        const {data} =await this.$post("/manage/formData/defaultJump",_query)
        if (data.data.data.length>0){
          this.messageInit(data.data.data[0])
          this.$message.success("该档案已存在修改人员,给您跳转到无人档案");
          let obj = new Object()
          obj.formDefinitionId = this.formId
          obj.archivesId = data.data.data[0].id
          await this.$http.post("/manage/formData/uniquenessJudge", obj)
          await this.$router.push({
            path: '/volumeslist/inEntry',
            query: {
              message: this.message,
              ajRow: data.data.data[0],
              type: t,
              index: 1, _query,
              date:new Date().getTime()
            }
          })
        }else {
          this.$message.error("当前已经没有无人操作过的档案了")
          //重新加载页面
          this.eventBus.$emit("loadData")
        }
      },
        volumeEntry() {
            this.messageInit()
            const nowPage = (parseInt(this.eventBus.page.index) - 1) * parseInt(this.eventBus.page.size)
            const _query = this.eventBus.getQueryByQueryType('query')
            const t = this.IfStatus(this.eventBus.defaultForm.status_info)
            this.$router.push({
                path: '../../volumeslist/inEntry',
                query: {
                    message: this.message,
                    ajRow: this.row,
                    type: t,
                    index: parseInt(this.row.$index) + nowPage + 1,
                    _query,
                    date:new Date().getTime()
                }
            })
        },
        //存放参数
        obj(){
            let obj = new Object();
            obj.formDefinitionId = this.formId
            obj.archivesId = this.row.id
            return obj
        },
        uniquenessJudge(){
            const obj = this.obj()
            this.$http.post("/manage/formData/uniquenessJudge", obj).then(({data}) => {
                if (data.data.success) {
                    //成功后跳转到加工页面
                    this.volumeEntry()
                } else {
                  this.defaultJump()
                }
            })
        },

    }
}
</script>
