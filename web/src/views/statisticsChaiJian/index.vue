<template>
  <section>
    <nac-info title="记录统计">
      <el-form :model="searchForm" ref="searchForm" inline>
        <el-form-item label="选择批次">
          <el-select v-model="searchForm.registerId" clearable placeholder="请选择批次">
            <el-option
                v-for="(item,index) in register"
                :key="item.index"
                :label="item.name"
                :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </nac-info>
    <div class="index_main">
      <el-table :data="flowData"
                style="width: 100%">
          <el-table-column
              prop="label"
              label="操作环节"
              align="center"
              header-align="center"
              sortable>
          </el-table-column>
          <el-table-column
              prop="totalNum"
              label="档案总份数"
              align="center"
              header-align="center"
              sortable>
          </el-table-column>
          <el-table-column
              prop="completeNum"
              label="已完成数量"
              align="center"
              header-align="center"
              sortable>
              <template slot-scope="scope">
                  <el-link type="success" v-on:click="cellclick(scope.row)"> {{scope.row.completeNum}} </el-link>
              </template>
          </el-table-column>
        <el-table-column
            prop="surPlusNum"
            label="环节剩余数量"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog title="详情信息" :visible.sync="dialogFormVisible"
               width="60%"
               center
               :before-close="handleClose">
      <el-form ref="peopleData" :model="peopleData" >
        <el-form-item label="选择时间">
          <el-col :span="24">
            <el-date-picker
                v-model="peopleData.statisticsTime"
                type="daterange"
                align="right"
                unlink-panels
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
            >
            </el-date-picker>
          </el-col>
        </el-form-item>
      </el-form>
      <el-table :data="peopleTableData" show-summary>
        <el-table-column property="name" label="操作人" width="200"></el-table-column>
        <el-table-column property="address" label="完成数量"></el-table-column>
      </el-table>
    </el-dialog>
  </section>
</template>

<script>
import indexMixin from '@dr/auto/lib/util/indexMixin'

export default {
  mixins: [indexMixin],
  name: "index",
  data() {
    return {
      statisticsTime: {},
      register: {},
      searchForm: {
        registerId:''
      },
      formDefinitionId: '',
      registerId: '',
      categoryId: '',
      pickerOptions: {
        shortcuts: [
          {
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 1);
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '前天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 2);
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          }]
      },
      startTime: 0,
      endTime: 0,
      linklist: [
        {index: "0", label: "任务登记",   value: 'RECEIVE'   ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "1", label: "图像扫描",   value: 'SCANNING'  ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "2", label: "图像处理",   value: 'PROCESSING',shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "3", label: "图像质检",   value: 'IMAGES'    ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "4", label: "手动拆件",   value: 'WSSPLIT'   ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "5", label: "档案著录",   value: 'VOLUMES'   ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "6", label: "初检",      value: 'QUALITY'   ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "7", label: "复检",      value: 'RECHECK'   ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
        {index: "8", label: "数字化成果", value: 'OVER'      ,shouldNum:"0",surPlusNum:"0",completeNum:"0",totalNum:"0"},
      ],
      tableData: [],
      flowData: [],
      dialogFormVisible:false,
      peopleData:{
        statisticsTime:''
      },
      peopleTableData:[],
      tables:{}
    }
  },
  methods: {
    $init() {
      this.registerData()
    },
    async registerData() {
      this.$http.post('/register/page').then(({data}) => {
        if (data && data.success) {
          this.register = data.data.data
          this.formDefinitionId = this.register[0].formDefinitionId
          this.searchForm.registerId = this.register[0].id
          this.byRegisterIdGetCategoryId(this.searchForm.registerId)
        } else {
          this.$message.error(data.message)
        }
      })
    },
    returnFlowCensus(formDefinitionId){
      //拿当前的type去后台查询，返回下一个的type.
      this.$http.post('/fonddata/flowCensus', {
        fid: formDefinitionId,
      }).then(({data}) => {
        if (data && data.success) {
          let list = data.data
          let datalist =[];
          for (let i=0;i<list.length;i++){
            for (let j=0;j<this.linklist.length;j++){
              if (list[i].value === this.linklist[j].value){
                datalist.push({
                  label:this.linklist[j].label,
                  shouldNum:list[i].shouldNum,
                  surPlusNum:list[i].surPlusNum,
                  completeNum:list[i].completeNum,
                  totalNum:list[i].totalNum,
                  value:list[i].value
                })
                continue
              }
            }
          }
          this.flowData = datalist
        }else {
          this.flowData = []
        }
      })
    },
    cellclick(row) {
      //详情dialog
      this.dialogFormVisible = true
      this.peopleData.statisticsTime = null
      this.peopleTableData = []
      this.tables = row
      this.flowPeopleData(row)
    },
    //获取操作人完成的数量
    flowPeopleData(row){
      //拿当前的type去后台查询，返回下一个的type.
      this.$http.post('/fonddata/flowPeopleData', {
        fid: this.formDefinitionId,
        info:row.value,
        startTime:this.startTime,
        endTime:this.endTime
      }).then(({data}) => {
        if (data && data.success) {
            this.peopleTableData = data.data
        }else {
          this.peopleTableData = []
        }
      })
    },
    //获取批次的选中环节
    async byRegisterIdGetCategoryId() {
      //获取批次的选中环节
      this.returnFlowCensus(this.formDefinitionId)
    },
    handleClose(){
      this.tables = {}
      this.dialogFormVisible =false
    }

  },
  watch: {
    async 'searchForm.registerId'(v) {
      this.register.forEach(i => {
        if (i.id === v) {
          this.formDefinitionId = i.formDefinitionId
          this.byRegisterIdGetCategoryId(v)
        }
      })
    },
    async 'peopleData.statisticsTime'(v) {
      if (v !== null) {
        this.startTime = new Date(v[0]).getTime()
        this.endTime = new Date(v[1]).getTime()
        this.flowPeopleData(this.tables)
      } else {
        this.startTime = 0
        this.endTime = 0
      }
    }
  }
}
</script>

<style scoped>

</style>
