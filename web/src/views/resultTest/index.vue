<template>
  <section>
    <nac-info title="成果检测">
      <span>当前批次：</span>
<!--      <el-input v-model="registerId" style="width: 100px;" disabled/>-->
      <el-select v-model="registerId" placeholder="请选择批次" :disabled="registerDisabled">
        <el-option
            v-for="(item,index) in register"
            :key="item.index"
            :label="item.name"
            :value="item.id"
        ></el-option>
      </el-select>
      <el-button type="primary" size="mini" v-show="registerId!=''" @click="xmlGenerateFile()">生成xml</el-button>
      <el-button type="primary" size="mini" v-show="registerId!=''" @click="startResult()"  :v-loading="loading"
                 :disabled="resultDisabled">{{completeFen>0?'重新质检':'开始质检'}}</el-button>
      <el-button type="primary" size="mini" v-show="registerId!=''" @click="flush(registerId)">
        刷新
      </el-button>
      <el-button type="primary" @click="testReport()" v-show="registerId!=''" size="mini">检测规则设置</el-button>
    </nac-info>
    <div class="index_main">
      <div>
        <!--        用来隐藏进度条百分百的时候的v-if="show"-->
        <el-row style="padding: 40px">
          <el-col :span="4" style="text-align: center;font-size: 15px;line-height:25px;margin-top: 4px">
            <span>当前检测进度:</span>
            <!--            <span style="color: red">目录-页数检测</span>-->
          </el-col>
          <el-col :span="18">
            <el-progress :text-inside="true" :stroke-width="30" :percentage=percentage
                         :color="customColors"></el-progress>
          </el-col>
          <el-col :span="2" style="text-align: center;font-size: 15px;line-height:25px;margin-top: 4px">
            <span style="font-size: 20px">{{ completeFen }}/{{ totalFen }}份</span>
          </el-col>
        </el-row>
        <el-table :data="statisticsData" style="width: 100%"
        >
          <el-table-column
              prop=""
              label="原文|图像检测未通过项"
              align="center"
              header-align="center"
          >
            <el-table-column
                prop="yuanWenArchiveCodeTest"
                label="档号检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="yuanWenFormatTest"
                label="格式检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="yuanWenDpiTest"
                label="dpi检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="yuanWenFilePowerTest"
                label="分辨率检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="yuanWenFileNameTest"
                label="命名规范检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="yuanWenFileYsTest"
                label="页码检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="yuanWenRgbTest"
                label="色彩检测" align="center">
            </el-table-column>
          </el-table-column>
        </el-table>
        <el-table :data="statisticsData" style="width: 100%"
        >
          <el-table-column
              prop=""
              label="元数据检测未通过项"
              align="center"
              header-align="center"
          >
            <el-table-column
                prop="metadataRequireTest"
                label="元数据必填项检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="metadataRepeatabilityTest"
                label="元数据唯一性检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="metadataTypeTest"
                label="元数据类型检测" align="center">
            </el-table-column>
            <el-table-column
                prop="metadataLengthTest"
                label="元数据长度检测" align="center">
            </el-table-column>
            <el-table-column
                prop="metadataValRangeTest"
                label="元数据值范围检测" align="center">
            </el-table-column>
            <el-table-column
                prop="metadataValContentTest"
                label="元数据值域检测" align="center">
            </el-table-column>
            <el-table-column
                prop="metadataDisByteTest"
                label="元数据禁用词检测" align="center">
            </el-table-column>
            <el-table-column
                prop="metadataComplexTest"
                label="元数据复杂检测"
                align="center">
            </el-table-column>
          </el-table-column>
        </el-table>
        <el-table :data="statisticsData" style="width: 100%"
        >
          <el-table-column
              prop=""
              label="原文元数据对比检测未通过项"
              align="center"
              header-align="center"
          >
            <el-table-column
                prop="comparisonArchiveCodeTest"
                label="档号对比检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="comparisonFondCodeTest"
                label="全宗对比检测"
                align="center">
            </el-table-column>
            <el-table-column
                prop="comparisonFileYsTest"
                label="页数对比检测" align="center">
            </el-table-column>
          </el-table-column>
        </el-table>
        <el-table :data="problemData" style="width: 100%"
                  :span-method="arraySpanMethod"
        >
          <el-table-column
              prop=""
              label="档案问题详情"
              align="center"
              header-align="center"
          >
            <el-table-column
                prop="archival_code"
                label="档号"
                align="center">
            </el-table-column>
            <el-table-column
                label="档号问题"
                align="center">
              <el-table-column
                  prop="resultType"
                  label="检测类型"
                  align="center">

              </el-table-column>
              <el-table-column
                  prop="resultElement"
                  label="检测对象"
                  align="center">

              </el-table-column>
              <el-table-column
                  prop="resultMessage"
                  label="检测未通过原因"
                  align="center">

              </el-table-column>
            </el-table-column>
          </el-table-column>
        </el-table>
        <el-button type="primary" @click="exportExcel()" v-show="registerId!=''" size="mini">导出报告</el-button>
        <el-button type="primary" @click="exitAll()" v-show="registerId!=''" size="mini">退回全部</el-button>
        <el-button type="primary" @click="resultAdopt()" v-show="registerId!=''" size="mini">查看通过</el-button>
      </div>
    </div>
    <el-dialog title="字段检测配置" :visible.sync="testReportVisible">
      <el-tabs v-model="activeName">
        <el-tab-pane label="原文检测规则" name="first">
          <el-table :data="yuanWenRuleDate" max-height="250">
            <el-table-column property="fieldName" label="字段名" align="center  "
                             header-align="center"></el-table-column>
            <el-table-column property="rules" label="校验规则" align="center"
                             header-align="center"></el-table-column>
            <el-table-column property="preset" label="预设值" align="center  "
                             header-align="center">
              <template slot-scope="scope">
                <el-select v-if="scope.row.preset!='/'" @change="updatePreset(scope.row)" v-model="scope.row.preset"
                           style="padding-left:40px;text-align: center;width: 60%;">
                  <el-option label="200" value="200"></el-option>
                  <el-option label="300" value="300"></el-option>
                  <el-option label="600" value="600"></el-option>
                </el-select>
                <span v-else>{{ scope.row.preset }}</span>
              </template>

            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="元数据常规性检测规则" name="second">
          <el-table :data="metadataRuleDate" max-height="250">
            <el-table-column property="fieldName" label="字段名" align="center  "
                             header-align="center"></el-table-column>
            <el-table-column property="characterType" label="字符类型" align="center"
                             header-align="center">
              <template slot-scope="scope">
                {{ characterType(scope.row.characterType) }}
              </template>
            </el-table-column>
            <el-table-column property="rules" label="校验规则" width="300" align="center"
                             header-align="center"></el-table-column>
            <el-table-column property="isRequired" label="是否必填" align="center"
                             header-align="center">
              <template slot-scope="scope">
                <el-checkbox v-model="scope.row.required" disabled/>
              </template>
            </el-table-column>
            <el-table-column property="isRepeatability" label="是否可重复" align="center"
                             header-align="center">
              <template slot-scope="scope">
                <el-checkbox v-model="scope.row.repeatability" disabled/>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="元数据复杂性检测规则" name="third">
          <el-table :data="metadataTestDate" max-height="250">
            <el-table-column property="fieldName" label="字段名" align="center  "
                             header-align="center"></el-table-column>
            <el-table-column property="rules" label="校验规则" width="500" align="center"
                             header-align="center"></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="原文元数据对比检测规则" name="four">
          <el-table :data="comparisonRuleDate" max-height="250">
            <el-table-column property="fieldName" label="字段名" align="center  "
                             header-align="center"></el-table-column>
            <el-table-column property="rules" label="校验规则" align="center"
                             header-align="center"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-button type="primary" size="mini" v-show="registerId!=''" @click="resetResultTest()">初始化校验规则</el-button>
    </el-dialog>
  </section>
</template>


<script>
export default {
  name: "index",
  data() {
    return {
      loading: false,
      activeName: 'first',
      //存放表格数据
      statisticsData: [],
      testReportVisible: false,
      addTextVisible: false,
      registerId: '',
      register: {},
      customColors: [
        {color: '#f56c6c', percentage: 20},
        {color: '#e6a23c', percentage: 40},
        {color: '#5cb87a', percentage: 60},
        {color: '#1989fa', percentage: 80},
        {color: '#6f7ad3', percentage: 100}
      ],
      problemData: [],
      percentage: 0,
      form: {},
      rules: {
        name: [{required: true, message: '字段名不能为空', trigger: 'blur'}],
        type: [{required: true, message: '字段类型不能为空', trigger: 'blur'}],
        length: [{required: true, message: '字段长度不能为空', trigger: 'blur'}],
      },
      isOver: false,
      //原文规则
      yuanWenRuleDate: [],
      //元数据规则
      metadataRuleDate: [],
      //对比规则
      comparisonRuleDate: [],
      //元数据约束性规则
      metadataTestDate: [],
      show: true,
      url: '',
      totalFen: 0,
      completeFen: 0,
      allMap:new Map,
      registerDisabled:true,
      resultDisabled:false,
    }
  },
  methods: {
    $init() {
      this.registerData()
    },
    editClick(row) {
      this.form = row
      this.addTextVisible = true
    },
    arraySpanMethod({row, column, rowIndex, columnIndex}) {
      const key = row.archival_code
      if (columnIndex === 0) {
        if (this.allMap.get(key) !== undefined) {
          const length = this.allMap.get(key)[1]
          const index = this.allMap.get(key)[0]
          if (rowIndex === index) {
            return [length, 1]
          } else {
            return [0, 0]
          }
        }
      }
    },
    exportExcel() {
      this.$post("/resultTest/resultExcel", {registerId: this.registerId})
          .then(({data}) => {
            if (data && data.success) {
              this.url = data.data
              console.log(this.url)
              window.open("api/ofd/ofdDownload?ofdPath=" + encodeURI(this.url));
            } else {
              this.$message.error(data.message)
            }
          })
    },

    async exitAll() {
      const {data} = await this.$post("/resultTest/resultUpdateType", {id: this.registerId})
      if (data && data.success) {
        this.percentage = data.data
      } else {
        this.$message.error(data.message)
      }
    },
    //进度条初始化
    async percentageInit(v) {
      const {data} = await this.$post("/resultTest/percentageInit", {registerId: v})
      if (data && data.success) {
        this.percentage = data.data.percent
        this.totalFen = data.data.totalFen
        this.completeFen = data.data.completeFen
      } else {
        this.$message.error(data.message)
      }
    },
    //各问题统计
    async resultStatisticsData(v) {
      const {data} = await this.$post("/resultTest/resultStatistics", {registerId: v})
      if (data && data.success) {
        this.statisticsData = data.data
      } else {
        this.$message.error(data.message)
      }
    },
    //档号返回质检信息
    async problemDataInit(v) {
      const {data} = await this.$post("/resultTest/getResultMessage", {registerId: v})
      if (data && data.success) {
        this.problemData = data.data
        this.spanMap()
      } else {
        this.$message.error(data.message)
      }
      this.resultDisabled = false
      this.loading = false
    },
    spanMap(){
      let map = new Map;
      this.problemData.forEach(i=>{
        let key = i.archival_code
        //判断map是否包含key
        if (map.has(key)){
          //有的话,将新增内容放入数组
          map.get(key).push(i)
        }else {
          //没有数组就创造数组放进去
          map.set(key,[i])
        }
      })
      let index = 0;
      //遍历map
      for (let key of map.keys()) {
        this.allMap.set(key,[index,map.get(key).length])
        index = index + map.get(key).length
      }
    },
    //重置配置规则
    async resetResultTest() {
      const {data} = await this.$post("/resultTest/resetResultTest", {registerId: this.registerId})
      if (data && data.success) {
        await this.detectionDateInit(this.registerId)
        this.$message.success("重置完成")
      } else {
        await this.detectionDateInit(this.registerId)
        this.$message.error(data.message)
      }
    },
    async flush(v) {
      //统计表 质检信息 进度条
      await this.percentageInit(v)
      await this.resultStatisticsData(v)
      await this.problemDataInit(v)
    },
    async startResult() {
      this.resultDisabled = true
      this.loading = true
      if (this.completeFen>0){
        this.$confirm('重新质检会删除所有旧数据,是否重新质检?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const {data} =  this.$post("/resultTest/startResult", {registerId: this.registerId})
          if (data && data.success) {
            this.$message.success("开始质检")
            this.flush(this.registerId)
          } else {
            this.flush(this.registerId)
          }
        })
      }else {
        const {data} = await this.$post("/resultTest/startResult", {registerId: this.registerId})
        if (data && data.success) {
          this.$message.success("开始质检")
          await this.flush(this.registerId)
        } else {
          await this.flush(this.registerId)
        }
      }
    },
    async xmlGenerateFile() {
      const {data} = await this.$post("/resultTest/xmlGenerateFile", {registerId: this.registerId})
      if (data && data.success) {
        this.$message.success("xml文件生成成功")
      } else {
        this.$message.error(data.message)
      }
    },
    addForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.addTextVisible = false
        }
      })

    },
    async testReport() {
      //查出当前批次下在点击检测报告时的档号对象
      await this.detectionDateInit(this.registerId);
      this.testReportVisible = true
    },
    //带检测状态为成功的跳转到成果检测环节
    resultAdopt(row) {
      this.$router.push({
        path: '/over',
        query: {
          status_info: 'over',
          registerId: this.registerId,
          quality_state: '1'
        }
      })
    },
    async registerData() {
      this.$http.post('/register/page').then(({data}) => {
        if (data && data.success) {
          this.register = data.data.data
          if (this.$route.query.registerId){
            this.registerId = this.$route.query.registerId
            this.registerDisabled = true
          }else {
            if (this.registerId == undefined || this.registerId == "") {
              this.registerId = this.register[0].id
              this.registerDisabled = false
            }
          }

        } else {
          this.$message.error(data.message)
        }
      })
    },
    //检验规则初始化
    async detectionDateInit(v) {
      const {data} = await this.$post("/resultTest/detectionDateInit", {registerId: v})
      if (data && data.success) {
        this.yuanWenRuleDate = []
        this.metadataRuleDate = []
        this.comparisonRuleDate = []
        this.metadataTestDate = []
        data.data.forEach(i => {
          if (i.resultType == "1") {
            this.yuanWenRuleDate.push(i)
          } else if (i.resultType == "2") {
            this.metadataRuleDate.push(i)
          } else if (i.resultType == "3") {
            this.comparisonRuleDate.push(i)
          } else if (i.resultType == "4") {
            this.metadataTestDate.push(i)
          }
        })
      } else {
        this.$message.error(data.message)
      }
    },
    async updatePreset(row) {
      const {data} = await this.$post("/resultTest/update", row)
      if (data && data.success) {
        this.$message.success("更新成功")
      } else {
        this.$message.error(data.message)
      }
    },
    characterType(v) {
      if (v == 1) {
        return "数字"
      } else if (v == 2) {
        return "时间"
      } else if (v == 3) {
        return "字符"
      } else {
        return "暂无"
      }
    },

  },
  watch: {
    registerId(v) {
      this.percentageInit(v)
      this.problemDataInit(v)
      this.resultStatisticsData(v)
    }
    ,
    percentage() {
      if (this.percentage >= 100) {
        this.show = false
      } else {
        this.show = true
      }
    }
  }
}

</script>

<style>
.result_text {
  color: #499afc;
  font-weight: bold;
  justify-content: center;
  font-size: 12px;
  white-space: nowrap;
  overflow-x: hidden;
  text-decoration: none;
  cursor: pointer
}

.result_success {
  line-height: 20px;
  color: #ff0000;
  font-weight: bold;
  justify-content: center;
  font-size: 20px;
  white-space: nowrap;
  overflow-x: hidden;
  text-decoration: none;
  cursor: pointer
}
</style>