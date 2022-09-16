<template>
    <section>
        <nac-info title="JPG队列">
          <el-row style="padding-right: 50px">
            <el-col :span="20">
              <el-form :model="searchForm" ref="searchForm" inline>
                <el-form-item label="选择批次" >
                  <el-select v-model="searchForm.batchName" clearable placeholder="请选择批次" style="width: 150px">
                    <el-option
                        v-for="(item,index) in register"
                        :key="item.index"
                        :label="item.name"
                        :value="item.name"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="档号 :" prop="archiveCode">
                  <el-input v-model="searchForm.archiveCode" placeholder="请输入档号" style="width: 150px" clearable/>
                </el-form-item>
                <el-form-item label="拆分状态 :" prop="status">
                  <el-select v-model="searchForm.status" clearable placeholder="请选择状态" style="width: 150px">
                    <el-option label="等待" value="0"></el-option>
                    <el-option label="拆分中" value="1"></el-option>
                    <el-option label="拆分成功" value="2"></el-option>
<!--                    <el-option label="拆分失败" value="3"></el-option>-->
<!--                    <el-option label="暂停中" value="4"></el-option>-->
                  </el-select>
                </el-form-item>
              </el-form>
            </el-col>
            <el-col :span="20" style="margin-top: 10px">
              <el-button type="primary" @click="loadData(searchForm)" size="mini">搜索</el-button>
              <el-button type="primary" @click="reset()" size="mini">重置</el-button>
              <span style="size: 15px;padding-left: 10px">当前执行批次：</span>
              <span style="color: red">{{current}}</span>
            </el-col>
          </el-row>


        </nac-info>
        <div class="index_main" v-loading="loading">
            <el-table border height="100%" class="table-container"
                      ref="multipleTable" :data="data"
                      @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" align="center"/>
                <column-index :page="page"/>
                <el-table-column
                        prop="batchName"
                        label="批次号"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="archiveCode"
                        label="档号"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="fileName"
                        label="文件名称"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="fileSize"
                        label="文件大小"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="fileYs"
                        label="文件页数"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="personName"
                        label="创建人"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column prop="createDate" label="创建时间" header-align="center"
                                 show-overflow-tooltip
                                 width="150" align="center">
                    <template slot-scope="scope">
                        <span>{{ timestampToTime(scope.row.createDate) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="拆分状态" header-align="center"
                                 show-overflow-tooltip
                                 align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.status|dict('dlCode') }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                        prop="jpgExplain"
                        label="匹配结果"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
            </el-table>
            <el-pagination
                    @current-change="index=>loadData({pageIndex:index-1},this.searchForm)"
                    :current-page.sync="page.index"
                    :page-size="page.size"
                    layout="total, prev, pager, next,jumper"
                    :total="page.total">
            </el-pagination>
        </div>
    </section>
</template>

<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        mixins: [indexMixin],
        name: "index",
        data() {
            return {
                dict: ['dlCode'],
                page: {index: 0, size: 15},
                multipleSelection: [],
                searchForm: {},
                register: {},
                sendType: 'all',
                current:"无",
                interval:null
            }
        },
        destroyed() {
          //页面关闭时清除定时器
        if(this.interval){
          window.clearInterval(this.interval);
        }
      },
        methods: {
            $init() {
                this.loadData();
                this.registerData();
                this.currentData();
            },
            loadData(params) {
                this.loading = true
                this.$http.post('/jpgQueue/page', params).then(({data}) => {
                    if (data.success) {
                        this.data = data.data.data
                        this.page.index = data.data.start / data.data.size + 1
                        this.page.size = data.data.size
                        this.page.total = data.data.total
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            reset(){
              this.loadData();
              this.searchForm = {}
             },
            currentData(){
              this.selectFunction();
                this.interval = window.setInterval( ()=>{
                  if (this.rowFunction.length>0){
                    this.selectFunction()
                }else {
                    this.$once('hook:beforeDestroy', () => {
                      clearInterval(this.interval);
                    })
                  }
           },10000);
            },
            selectFunction() {
            this.$http.post('/jpgQueue/selectFunction').then(({data}) => {
              if (data&&data.success) {
                this.rowFunction = data.data.data
                if (this.rowFunction.length>0&&this.rowFunction[0]&&this.rowFunction[0].batchName){
                  this.current =  this.rowFunction[0].batchName
                }else {
                  this.current = "无"
                }
              } else {
                this.$message.error(data.message)
                this.current = "无"
              }
            })
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
            handleSelectionChange(val) {
                this.multipleSelection = val
            },
            //时间转换
            timestampToTime(timestamp) {
                if (timestamp != 0 && timestamp != undefined) {
                    return this.$moment(timestamp).format('YYYY-MM-DD HH:mm:ss')
                }
            },
        },
    }
</script>

<style scoped>
</style>