<template>
  <section>
    <nac-info title="记录统计">
      <el-form :model="searchForm" ref="searchForm" inline>
        <el-form-item label="选择批次">
          <el-select v-model="searchForm.id" clearable placeholder="请选择批次">
            <el-option
                v-for="(item,index) in register"
                :key="item.index"
                :label="item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择时间">
          <el-col :span="24">
            <el-date-picker
                v-model="statisticsTime"
                type="datetimerange"
                align="right"
                unlink-panels
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份">
            </el-date-picker>
          </el-col>
        </el-form-item>
        <el-button type="primary" @click="loadData(searchForm)" size="mini">搜索</el-button>
        <el-button type="primary" @click="reset()" size="mini">重置</el-button>
        <el-button type="primary" @click="updateStatistics()" size="mini">更新统计</el-button>
      </el-form>
    </nac-info>
    <div class="index_main">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" :summary-method="getSummaries" show-summary>
        <el-table-column
            prop="batch_no"
            label="批次号"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="batch_name"
            label="批次名"
            align="center"
            header-align="center"
            sortable>
          <template v-slot="scope">
            <a link class="result_text"
               :href="detail(scope.row)">
              {{ scope.row.batch_name }}
            </a>
          </template>
        </el-table-column>
        <el-table-column
            prop="batch_createDate"
            label="创建时间"
            align="center"
            header-align="center"
            sortable>
          <template v-slot="scope">
            {{ getTime(scope.row.batch_createDate) }}
          </template>
        </el-table-column>
        <el-table-column
            prop="completeDate"
            label="完成时间"
            align="center"
            header-align="center"
            sortable>
          <template v-slot="scope">
            {{ getTime(scope.row.completeDate) }}
          </template>
        </el-table-column>
        <el-table-column
            prop="totalFen"
            label="总份数"
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
            prop="completeFenNum"
            label="完成份数"
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
            prop="totalPeople"
            label="参与人数"
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
      <el-pagination
          @current-change="index=>loadData({pageIndex:index-1,createDate:this.searchForm.createDate,
          updateDate:this.searchForm.updateDate,registerId:this.searchForm.id})"
          :current-page.sync="page.index"
          :page-size="page.size"
          layout="total, prev, pager, next,jumper"
          :total="page.total">
      </el-pagination>
    </div>
  </section>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      page: {index: 0, size: 15},
      loading: false,
      //存放搜索数据
      searchForm: {
        id: null,
        //起始时间
        createDate: 0,
        //截止时间
        updateDate: 0,
      },
      //存放表格数据
      tableData: [],
      //详情信息
      dialogFormVisible: false,
      peopleTableData: '',
      statisticsTime: [],
      register: {}
    }
  },
  methods: {
    $init() {
      this.registerData();
      this.loadData(this.searchForm);
    },
    //查看详情,可以将自己想传的数据传入进去
    detail(row) {
      return '#/statistics/check?row=' + JSON.stringify(row)
    },
    //合计
    getSummaries(param) {
      //param 是固定的对象，里面包含 columns与 data参数的对象 {columns: Array[4], data: Array[5]},包含了表格的所有的列与数据信息
      const {columns, data} = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总计';
          return;
        }
        const values = data.map(item => Number(item[column.property]));
        if (index > 3 && index < 9) {
          if (!values.every(value => isNaN(value))) {
            sums[index] = values.reduce((prev, curr) => {
              return prev + curr;
            }, 0);
            sums[index] += '';
          } else {
            sums[index] = 'N/A';
          }
        }
      })
      return sums
    },
    //更新统计按钮
    async updateStatistics() {
      this.loading = true
      this.$http.post('statistics/statisticsAllRegister').then(({data}) => {
        if (data && data.success) {
          this.$message.success("更新统计表成功")
        } else {
          this.$message.error(data.message)
        }
        this.loading = false
      })

    },
    //时间转换
    getTime(timestamp) {
      if (timestamp != 0 && timestamp != undefined) {
        return this.$moment(timestamp).format('YYYY-MM-DD HH:mm:ss')
      }
    },
    //搜索
    loadData(search) {
      if (this.statisticsTime && this.statisticsTime[0] != undefined) {
        //创建时间为开始，更新时间为结束
        search.createDate = new Date(this.statisticsTime[0]).getTime()
        search.updateDate = new Date(this.statisticsTime[1]).getTime()
      }
      if (this.statisticsTime == null) {
        search.createDate = 0
        search.updateDate = 0
      }
      this.$post('statistics/page', search).then(({data}) => {
        if (data.success) {
          this.tableData = data.data.data
          this.page.index = data.data.start / data.data.size + 1
          this.page.size = data.data.size
          this.page.total = data.data.total
        } else {
          this.$message.error(data.message)
        }
      })
    },
    //重置
    reset() {
      this.searchForm = {}
      this.statisticsTime = []
      this.loadData();
    },
    async registerData() {
      this.$http.post('/register/page').then(({data}) => {
        if (data && data.success) {
          this.register = data.data.data
        } else {
          this.$message.error(data.message)
        }
      })
    },

  }
}
</script>

<style scoped>
.result_text {
  color: #499afc;
  font-weight: bold;
  justify-content: left;
  font-size: 12px;
  white-space: nowrap;
  overflow-x: hidden;
  text-decoration: none
}
</style>