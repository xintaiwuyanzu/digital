<template>
    <section>
        <nac-info title="打包记录">
            <el-form :inline="true" :model="sarchForm" class="demo-form-inline">
                <el-form-item label="档号">
                    <el-input v-model="sarchForm.archiveCode" clearable placeholder="档号"></el-input>
                </el-form-item>
                <el-form-item label="题名">
                    <el-input v-model="sarchForm.title" clearable placeholder="题名"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" v-if="multipleSelection.length > 0" @click="remove">删除</el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table :data="data"
                          @selection-change="handleSelectionChange"
                          height="100%"
                          :border="true">
                    <el-table-column
                            type="selection"
                            width="55">
                    </el-table-column>
                    <el-table-column label="序号" align="center" width="60">
                        <template slot-scope="scope">
                            {{ (page.index - 1) * page.size + scope.$index + 1 }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="archiveCode" label="档号" show-overflow-tooltip/>
                    <el-table-column prop="title" label="题名" min-width="100"/>
                    <el-table-column prop="message" label="封包信息" show-overflow-tooltip/>
                    <el-table-column prop="createDate" label="封包时间" show-overflow-tooltip>
                        <template slot-scope="scope">
                            {{ getTime(scope.row.createDate) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" align="center">
                        <template slot-scope="scope">
                            <el-button type="text" @click="downloadZip(scope.row)">下 载</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <el-pagination
                    @current-change="index=>loadData({pageIndex:index-1})"
                    :current-page.sync="page.index"
                    :page-size="page.size"
                    layout="total, prev, pager, next,jumper"
                    :total="page.total">
            </el-pagination>
        </div>archivesLog
    </section>
</template>
<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        data() {
            return {
                sarchForm: {},
                dialog: false,
                rolePersons: [],
                selectRoleId: '',
                persons: [],
                scheme: {},
                multipleSelection: [],
                rules: {
                    fieldval: [
                        {required: true, message: '请输入字段值', trigger: 'change'},
                        {required: true, message: '请输入字段值', trigger: 'blur'}
                    ],
                    field: [
                        {required: true, message: '请输入字段名', trigger: 'change'},
                        {required: true, message: '请输入字段名', trigger: 'blur'}
                    ],
                }
            }
        },
        methods: {
            $init() {
                this.loadData()
            },
            loadData(param) {
                this.$http.post('/packetRecord/page', Object.assign(this.sarchForm, param))
                    .then(({data}) => {
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
            handleSelectionChange(val) {
                this.multipleSelection = val;
            },
            downloadZip(row) {
                window.open('download' + '/' + row.fondCode + '/' + row.archiveCode + '.zip')
            },
            remove() {
                this.$confirm("确认删除？", '提示', {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning',
                    dangerouslyUseHTMLString: true
                }).then(() => {
                    let id = this.multipleSelection.map(item => item.id).join(',')
                    this.$http.post('/packetRecord/removePacket', {id: id})
                        .then(({data}) => {
                            if (data.success) {
                                this.$message.success("删除成功");
                                this.loadData()
                            } else {
                                this.$message.error(data.message)
                            }
                            this.loading = false
                        })
                })
            },
            getTime(time) {
                return this.$moment(this.$moment(parseInt(time))).format('YYYY-MM-DD HH:mm:ss')
            },
        },
        mixins: [indexMixin]
    }
</script>
