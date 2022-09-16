<template>
  <section>
    <nac-info title="统计详情" back>
    </nac-info>
    <div class="index_main">
      <div>
        <el-form :inline="true" :model="registerDetail" class="demo-form-inline" label-width="100px">
          <el-row>
            <el-col :span="8">
              <div>
                <el-form-item label="批次号：" prop="batch_no">
                  <el-input v-model="registerDetail.batch_no" disabled
                            placeholder="请输入批次号"></el-input>
                </el-form-item>
                <el-form-item label="批次名：" prop="batch_name">
                  <el-input v-model="registerDetail.batch_name" disabled
                            placeholder="请输入批次名"></el-input>
                </el-form-item>
                <el-form-item label="原文格式">
                  <el-input v-model=" registerDetail.original_format" disabled></el-input>
                </el-form-item>

              </div>
            </el-col>
            <el-col :span="8">
              <div>
                <el-form-item label="元数据方案" prop="yuanSJ">
                  <el-input v-model="registerDetail.metadataName" disabled></el-input>
                </el-form-item>
                <el-form-item label="执行标准:" prop="yearId">
                  <el-input v-model="registerDetail.codeFormData" disabled
                            placeholder="请选择元数据方案"></el-input>
                </el-form-item>
                <el-form-item label="目标格式">
                  <el-input v-model="registerDetail.target_format" disabled></el-input>
                </el-form-item>
              </div>
            </el-col>
            <el-col :span="8">
              <div>
                <el-form-item label="门类:">
                  <el-input v-model="registerDetail.archivers_category_code_name" disabled
                            placeholder="请选择元数据方案"></el-input>
                </el-form-item>
                <el-form-item label="整理方式:">
                  <el-input v-model="registerDetail.arrangeFormData" disabled></el-input>
                </el-form-item>
                <el-form-item label="创建人">
                  <el-input v-model="registerDetail.receiver" disabled></el-input>
                </el-form-item>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <el-table :data="tableData" style="width: 100%"
                height="250"  v-loading="loading">
        <el-table-column
            prop="operationLink"
            label="环节名"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="handledBy"
            label="办理人"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
<!--        <el-table-column-->
<!--            prop="startDate"-->
<!--            label="开始时间"-->
<!--            align="center"-->
<!--            header-align="center"-->
<!--            sortable>-->
<!--          <template v-slot="scope">-->
<!--            {{ getTime(scope.row.startDate) }}-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column-->
<!--            prop="endDate"-->
<!--            label="结束时间"-->
<!--            align="center"-->
<!--            header-align="center"-->
<!--            sortable>-->
<!--          <template v-slot="scope">-->
<!--            {{ getTime(scope.row.endDate) }}-->
<!--          </template>-->
<!--        </el-table-column>-->
        <el-table-column
            prop="totalFen"
            label="总份数"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="completeFenNum"
            label="完成份数"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="surPlusFenNum"
            label="剩余份数"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="totalPage"
            label="总页数"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="completePageNum"
            label="完成页数"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="surPlusPageNum"
            label="剩余页数"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="status"
            label="完成状态"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      //存放表格数据
      tableData: [],
      loading: false,
      registerDetail: {},
      original_format_data: [{
        value: '1',
        label: 'PDF'
      }, {
        value: '2',
        label: 'TIF'
      },
        {
          value: '3',
          label: '纸质'
        }],
      target_format_data: [{
        value: '2',
        label: 'OFD'
      }],
    }
  },
  methods: {
    $init() {
      this.registerDetailInit();
    },
    registerDetailInit() {
      let p;
      if (typeof(this.$route.query.row)=='string'){
        p = JSON.parse(this.$route.query.row)
        p.id = p.registerId
      }else {
        p = this.$route.query.row
      }
      for (let i = 0; i < this.original_format_data.length; i++) {
        if (this.original_format_data[i].value === p.original_format) p.original_format = this.original_format_data[i].label
      }
      for (let i = 0; i < this.target_format_data.length; i++) {
        if (this.target_format_data[i].value == p.target_format) {
          p.target_format = this.target_format_data[i].label
        }
      }
      this.registerDetail = p
      this.tableDataInit();
    },
    async tableDataInit(){
      this.loading = true
      const {data} = await this.$http.post('/statisticsDetail/statisticsRegister',
          {formDefinitionId: this.registerDetail.formDefinitionId});
      if (data && data.success) {
        this.$message.success("表单详情更新成功")
      } else {
        this.$message.error(data.message)
      }
      let params = {
        registerId: this.registerDetail.id,
        page:false
      }
      const data1 = await this.$http.post('/statisticsDetail/page',params);
      if (data1&&data1.data.success){
        this.tableData = data1.data.data
      }
      else {
        this.$message.error(data.message)
      }
      this.loading = false
    },
    //时间转换
    getTime(timestamp) {
        if (timestamp!=="/"&&timestamp != 0 && timestamp != undefined) {
          return this.$moment(timestamp).format('YYYY-MM-DD HH:mm:ss')
        }
    },
  },
}
</script>

<style scoped>

</style>