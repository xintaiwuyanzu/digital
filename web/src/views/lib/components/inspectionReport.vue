<template>
  <section>
    <el-button type="primary" size="mini" @click="report"> 质检报告</el-button>
    <el-dialog
        title="提示"
        :visible.sync="dialogVisible"
        width="30%"
       >
      <el-table
          :data="reportData"
          border
          style="width: 100%">
        <el-table-column
            prop="status"
            label="检测状态"
            align="center">
        </el-table-column>
        <el-table-column
            prop="num"
            label="检测数量"
            align="center">
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">取 消</el-button>
<!--    <el-button type="primary" @click="searchStatusFalse">查询检测未通过对象</el-button>-->
  </span>
    </el-dialog>
  </section>
</template>

<script>
import abstractComponent from "./abstractComponent";
import search from "@/views/lib/components/search";
import abstractFormIndex from "@/views/lib/archiveFormIndex/abstractFormIndex";
export default {
  extends: abstractComponent,abstractFormIndex,
  data() {
    return {
      reportData:[],
      dialogVisible:false,
      page: {size: 15, index: 0, total: 0},
      data:[],
      parse:[],
      query:{}
    }
  },

  methods: {
   // async report(){
   //    this.query = this.eventBus.getQueryByQueryType('query')
   //   this.parse = JSON.parse(this.query._QUERY);
   //   let flowPath = this.parse[0].value
   //    //查出当前批次下在点击检测报告时的档号对象
   //   const {data} = await  this.$post("/jpgQueue/getRegisterIdByCount",{registerId:this.fond.registerId,flowPath:flowPath})
   //   if (data&&data.success){
   //     this.reportData = []
   //     for (let key in data.data){
   //       this.reportData.push({status:key,num:data.data[key]})
   //     }
   //     this.dialogVisible = true
   //   }else {
   //     this.$message.error(data.message)
   //   }
   //  },
   //  async searchStatusFalse(){
   //    //默认查询 value = 0为未通过 1为通过
   //    let arr = {key: "quality_state", value: "1",type: "0"}
   //    //添加查询条件 先转json在push后转str回去
   //    this.parse.push(arr);
   //    this.query._QUERY= JSON.stringify(this.parse)
   //    this.eventBus.$emit("loadData")
   //  //   const {data} = await this.$post('/manage/formData/formDataPage',
   //  //       Object.assign(
   //  //           //默认参数
   //  //           this.query,
   //  //           //分页参数
   //  //           this.page),
   //  //       {timeout: 20000})
   //  //   if (data&&data.success) {
   //  //     this.data = data.data.data
   //  //     this.page.total = data.data.total
   //  //     this.page.size = data.data.size
   //  //     this.page.index = data.data.start / data.data.size + 1
   //  //     //重置查询的条件
   //  //     // this.eventBus.$emit("click");
   //  //     //把查询出来的内容映射到表格页面内
   //  //     // this.eventBus.$emit("loadData")
   //  //   } else {
   //  //     this.$message.error(data.message)
   //  //   }
   //    this.dialogVisible = false;
   //  }
  },
}
</script>

<style scoped>

</style>