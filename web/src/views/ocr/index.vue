<template>
    <section>
        <nac-info title="OCR队列">
            <el-form :model="searchForm" ref="searchForm" inline>
                <el-form-item label="选择批次">
                    <el-select v-model="searchForm.batchName" clearable placeholder="请选择批次">
                        <el-option
                            v-for="(item,index) in register"
                            :key="item.index"
                            :label="item.name"
                            :value="item.name"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="档号 :" prop="archiveCode">
                    <el-input v-model="searchForm.archiveCode" placeholder="请输入档号" clearable/>
                </el-form-item>
                <el-form-item label="转换状态 :" prop="status">
                    <el-select v-model="searchForm.status" clearable placeholder="请选择状态" style="width: 150px">
                        <el-option label="等待转换" value="0"></el-option>
                        <el-option label="转换中" value="1"></el-option>
                        <el-option label="转换成功" value="2"></el-option>
                        <el-option label="转换失败" value="3"></el-option>
                        <el-option label="暂停中" value="4"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData(searchForm)" size="mini">搜索</el-button>
                    <el-button type="primary" @click="startUp()" size="mini">一键启动</el-button>
                    <el-button type="primary" @click="suspend()" size="mini">一键暂停</el-button>
                    <el-dropdown placement="bottom" trigger="click" @command="handleCommand">
                        <el-button class="search-btn" type="primary">启动服务
                            <i class="el-icon-arrow-down el-icon--right"/></el-button>
                        <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item v-if="multipleSelection.length>0" command="select">启动选中</el-dropdown-item>
                            <el-dropdown-item command="query">启动查询</el-dropdown-item>
                            <el-dropdown-item command="all">暂停重启</el-dropdown-item>
                        </el-dropdown-menu>
                    </el-dropdown>
                    <el-button type="primary" @click="updatePriority()" size="mini">优先级处理</el-button>
                  <span style="size: 15px;margin-left: 50px;margin-right:20px;line-height: 30px">当前执行批次：</span>
                  <span style="color: red">{{current}}</span>
                </el-form-item>
            </el-form>
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
                    prop="filePath"
                    label="文件地址"
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
                <el-table-column prop="status" label="转换状态" header-align="center"
                                 show-overflow-tooltip
                                 align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.status|dict('dlCode') }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="查看详情"
                    align="center"
                    header-align="center"
                    sortable>
                    <template slot-scope="scope">
                        <el-button type="text" @click="viewDetail(scope.row)">查看</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                @current-change="index=>loadData({pageIndex:index-1,batchName:this.searchForm.batchName,archiveCode:this.searchForm.archiveCode,status:this.searchForm.status})"
                :current-page.sync="page.index"
                :page-size="page.size"
                layout="total, prev, pager, next,jumper"
                :total="page.total">
            </el-pagination>
        </div>
        <el-dialog title="设置优先级" :visible.sync="dialogPriority" center>
                    <el-table :data="dialogBatch">
                        <el-table-column property="batch_no" sortable label="批次号" width="200"></el-table-column>
                        <el-table-column property="batch_name" sortable label="批次名称" width="150"></el-table-column>
                        <el-table-column property="form_scheme" sortable label="档案门类"></el-table-column>
                        <el-table-column property="receiver" sortable label="创建人"></el-table-column>
                        <el-table-column property="priority" sortable label="档案优先级">
                            <template slot-scope="scope">
                                <span v-if="scope.row.priority==='3'">{{ "系统默认" }}</span>
                                <span v-else-if="scope.row.priority==='2'">{{ "一般" }}</span>
                                <span v-else-if="scope.row.priority==='1'">{{ "最高" }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column property="priority"  label="操作">
                            <template slot-scope="scope">
                                <span><el-link :type="typeDate(scope.row.priority,'1') " @click="updatePriorityData(scope.row,'1')">最高</el-link></span>
                                <el-divider direction="vertical"></el-divider>
                                <span><el-link :type="typeDate(scope.row.priority,'2')" @click="updatePriorityData(scope.row,'2')">一般</el-link></span>
                                <el-divider direction="vertical"></el-divider>
                                <span><el-link :type="typeDate(scope.row.priority,'3')" @click="updatePriorityData(scope.row,'3')">默认</el-link></span>
                            </template>
                        </el-table-column>
                    </el-table>
          <span slot="footer" class="dialog-footer"></span>
        </el-dialog>
        <el-dialog :title="detail.archiveCode" :visible.sync="detailVisible" width="70%">
            <div style="height: 600px">
                <el-row>
                    <el-col :span="12">
                        <el-scrollbar style="height: 600px">
                            <img :src="jpgFilePath" style="width: auto;height:400px;margin: 20px"/>
                        </el-scrollbar>
                    </el-col>
                    <el-col :span="12">
                        <div class="editor">
                            <div class="editor" style="height: 600px">
                                <el-scrollbar style="height: 600px">
                                    <div ref="editor" class="text" style="width: 500px;height:auto; margin: 20px">
                                        <div v-for="item in dataTxt">
                                            <span>{{item}}</span>
                                        </div>
                                    </div>
                                </el-scrollbar>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </div>
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
            dict: ['dlCode'],
            page: {index: 0, size: 15},
            multipleSelection: [],
            searchForm: {},
            register: {},
            sendType: 'all',
            //优先级dialog
            dialogPriority: false,
            dialogBatch: {},
            taskMap:new Map(),
            timer:null,
            rowFunctionTime:null,
            rowFunction:[],
            detail: {},
            dataTxt: [],
            jpgFilePath: '',
            detailVisible: false,
            current:"无"
        }
    },
    destroyed() {//页面关闭时清除定时器
        if(this.timer){
            window.clearInterval(this.timer);
        }
        if(this.rowFunctionTime){
            window.clearInterval(this.rowFunctionTime);
        }

    },
    methods: {
        $init() {
            this.loadData();
            this.registerData();
            this.dialogBatchDate();
            this.selectFunction();
        },
        // handleClick(tab, event) {
        //     this.selectFunction();
        // },
        selectFunction() {
            this.$http.post('/ocrQueue/selectFunction').then(({data}) => {
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
        loadData(params) {
            this.loading = true
            this.$http.post('/ocrQueue/page', params).then(({data}) => {
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
        //一键暂停
        async suspend() {
            this.loading = true
            this.$confirm('确定全部暂停转换吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                await this.$http.post('/ocrQueue/updateStatus').then(({data}) => {
                    if (data.success) {
                        this.$message.success("暂停服务成功!!!");
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            }).catch(() => {
                this.loading = false
            })
        },
        //启动转换服务
        handleCommand(command) {
            switch (command) {
                case "select":
                    return this.getCurrentSelect()
                case "query":
                    return this.getFormQuery()
                default:
                    return this.getAllQuery()
            }
        },
        //一键启动
        async startUp() {
            this.$confirm('确定开启转换吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                const {data} = await this.$http.post("/ocrQueue/implementOcr")
                if (data.success) {
                    this.$message.success("正在开启转换服务！")

                    //暂时先设置延时刷新。
                    let timer = setTimeout(() => {
                        this.selectFunction()
                    }, 1000);
                    this.$once('hook:beforeDestroy', () => {
                        clearInterval(timer);
                    })
                    this.rowFunctionTime = window.setInterval(() => {
                            //如果该任务还在进行中 则继续获取状态
                            if (this.rowFunction.length>0) {
                                this.selectFunction()
                        }
                    }, 5000);

                } else {
                    this.$message.success(data.message)
                }
            }).catch(() => {
            })
        },
        //选择启动
        async getCurrentSelect() {
            let ocrQueueId = this.multipleSelection.map(item => item.id).join(",")
            await this.$confirm('确定开启转换吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            const {data} = await this.$http.post("/ocrQueue/selectOcrStart", {
                ocrQueueId: ocrQueueId,
            })
            if (data.success) {
                this.$message.success("正在开启转换服务！")
            } else {
                this.$message.success(data.message)
            }
        },
        //启动查询
        async getFormQuery() {
            await this.$confirm('确定开启查询转换吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            const {data} = await this.$http.post("/ocrQueue/searchOcrStart", this.searchForm)
            if (data.success) {
                this.$message.success("正在开启转换服务！")
            } else {
                this.$message.success(data.message)
            }
        },
        //暂停重启
        async getAllQuery() {
            await this.$confirm('确定开启转换吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            const {data} = await this.$http.post("/ocrQueue/allOcrStart")
            if (data.success) {
                this.$message.success("正在开启转换服务！")
            } else {
                this.$message.success(data.message)
            }
        },
        updatePriority() {
            this.dialogPriority = true
        },
        async dialogBatchDate() {
            this.$http.post('/register/page').then(({data}) => {
                if (data && data.success) {
                    this.dialogBatch = data.data.data
                } else {
                    this.$message.error(data.message)
                }
            })
        },
        typeDate(priority,type){
            if (priority=='1'&&type=='1'){
                return "success"
            }else if(priority=='2'&&type=='2'){
                return "success"
            }else if(priority=='3'&&type=='3'){
                return "success"
            }else {
                return "info"
            }
        },
        updatePriorityData(row,type){
            this.$http.post('/ocrQueue/updatePriorityData',{
                fid:row.formDefinitionId,
                type:type,
                fromid:row.id
            }).then(({data}) => {
                if (data && data.success) {
                    //获取到任务id
                    let id = data.data;
                    this.taskMap.set('id',data.data.id);
                    this.$message.success(data.data.data)
                    this.start()
                    console.log(this.taskMap)
                } else {
                    this.$message.error(data.message)
                }
            })
        },
        start(){
            this.timer = setInterval(() => {
                this.taskMap.forEach((value, key, map) => {
                    //如果该任务还在进行中 则继续获取状态
                    if (value.status != 'FINISHED') {
                        this.$http.post('/ocrQueue/ps',{id:value
                        }).then(({data}) => {
                                console.log(data);
                                //更新
                                map.set(key, data);
                                this.dialogBatchDate()
                        })
                    }
                });
            }, 1000);
        },
        viewDetail(row) {
            this.detail = row
            this.$http.post('/processing/findTxtByArchiveId', {
                path: row.filePath,
                archiveCode: row.archiveCode,
                fileName: row.fileName,
            }).then(({data}) => {
                if (data.success) {
                    console.log(data.data)
                    this.dataTxt = data.data.listContent
                    this.jpgFilePath = data.data.filePath.substring(data.data.filePath.indexOf("filePath"))
                    this.detailVisible = true
                } else {
                    this.$message.error(data.message)
                }
            })
        },
    },

}
</script>

<style scoped>
</style>
