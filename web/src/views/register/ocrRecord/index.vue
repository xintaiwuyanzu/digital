<template>
    <section>
        <nac-info title="OCR识别记录">
            <el-form :model="searchForm" ref="searchForm" inline>
                <el-form-item label="档号" prop="archiveCode">
                    <el-input v-model="searchForm.archiveCode" placeholder="请输入档号" style="width: 150px" clearable/>
                </el-form-item>
                <el-form-item label="识别状态">
                    <el-select v-model="searchForm.status" clearable placeholder="请选择状态" style="width: 150px">
                        <el-option label="成功" value="0"></el-option>
                        <el-option label="失败" value="1"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="选择时间">
                    <el-date-picker
                            v-model="searchTime"
                            type="datetimerange"
                            :picker-options="pickerOptions"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期"
                            align="right">
                    </el-date-picker>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData(searchForm)" size="mini">搜索</el-button>
                    <el-button type="primary" @click="reset" size="mini">重置</el-button>
                    <el-button type="danger" size="mini" @click="remove">删 除</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <el-table border height="100%" class="table-container" ref="multipleTable" :data="data"
                      @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" align="center"/>
                <column-index :page="page"/>
                <el-table-column
                        prop="archiveCode"
                        label="档号"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="fileName"
                        label="图像名称"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="fileKb"
                        label="图像大小(kb)"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="startTime"
                        label="开始时间"
                        :formatter="formatterTime"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="endTime"
                        label="结束时间"
                        :formatter="formatterTime"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="filePath"
                        label="存放地址"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="name"
                        label="识别方式"
                        align="center"
                        header-align="center"
                        sortable>
                </el-table-column>
                <el-table-column
                        prop="message"
                        label="返回信息"
                        align="center"
                        header-align="center"
                        sortable>
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
            <el-pagination
                    @current-change="index=>loadData({pageIndex:index-1,archiveCode:this.searchForm.archiveCode,status:this.searchForm.status})"
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
                page: {index: 0, size: 15},
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
                searchForm: {},
                multipleSelection: [],
                searchTime: [],
                detailVisible: false,
                detail: {},
                editorValue: "",
                dataTxt: [],
                jpgFilePath: '',
            }
        },
        methods: {
            $init() {
                this.loadData();
            },
            viewDetail(row) {
                this.detail = row
                this.$http.post('/processing/findTxtByArchiveId', {
                    path: row.filePath,
                    archiveCode: row.archiveCode,
                    fileName: row.fileName,
                }).then(({data}) => {
                    if (data.success) {
                        this.dataTxt = data.data.listContent
                        this.jpgFilePath = data.data.filePath.substring(data.data.filePath.indexOf("filePath"))
                        this.detailVisible = true
                    } else {
                        this.$message.error(data.message)
                    }
                })
            },
            formatterTime(row, column, cellValue, index) {
                return this.$moment(this.$moment(parseInt(cellValue))).format('YYYY-MM-DD HH:mm:ss.SSS')
            },
            loadData(search) {
                if (this.searchTime && this.searchTime[0] != undefined) {
                    search.startTime = new Date(this.searchTime[0]).getTime()
                    search.endTime = new Date(this.searchTime[1]).getTime()
                }
                if (this.searchTime == null) {
                    search.startTime = 0
                    search.endTime = 0
                }
                this.loading = true
                this.$http.post('/ocrRecord/page', search).then(({data}) => {
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
            reset() {
                this.searchForm = {}
                this.searchTime = []
                this.loadData();
            },
            handleSelectionChange(val) {
                this.multipleSelection = val
            },
            remove() {
                if (this.multipleSelection.length === 0) {
                    this.$message.error("请至少选择一条数据")
                    return
                }
                let ids = ''
                for (let i = 0; i < this.multipleSelection.length; i++) {
                    ids = ids + this.multipleSelection[i].id + ","
                }
                const param = Object.assign({}, {ids: ids})
                this.$confirm("确认删除？", '提示', {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning',
                    dangerouslyUseHTMLString: true
                }).then(() => {
                    this.$http.post('/ocrRecord/deleteByIds', param).then(({data}) => {
                        if (data.success) {
                            this.$message({
                                message: '操作成功！',
                                type: 'success'
                            });
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loadData()
                    })
                }).catch(() => {
                });
            },
        },
    }
</script>

<style lang="scss">
    .editor {
        position: fixed;
        width: 100%;
        margin: 0 auto;
        //position: relative;
        z-index: 0;

        .toolbar {
            border: 1px solid #ccc;
        }

        .text {
            border: 1px solid #ccc;
            min-height: 200px;
        }
    }
</style>
