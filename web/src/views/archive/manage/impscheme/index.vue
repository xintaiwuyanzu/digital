<template>
    <section>
        <nac-info title="导入方案">
            <el-form :model="searchForm" ref="form" inline>
                <el-form-item label="方案名称" prop="name">
                    <el-input v-model="searchForm.name" placeholder="请输入方案名称" style="width: 200px" clearable>
                    </el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="$init" size="mini">搜 索</el-button>
                    <el-button type="primary" size="mini" @click="add">添 加</el-button>
                    <el-button type="info" v-if="this.$route.query.batch_no" size="mini" @click="back">返 回
                    </el-button>
                </el-form-item>
            </el-form>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table ref="multipleTable" :data="schemeData" border height="100%">
                    <el-table-column label="序号" fixed align="center" width="50">
                        <template slot-scope="scope">
                            {{ (page.index - 1) * page.size + scope.$index + 1 }}
                        </template>
                    </el-table-column>
                    <el-table-column label="方案名称" prop="name" show-overflow-tooltip align="center"
                                     header-align="center">
                        <template slot-scope="scope">
                            {{ scope.row.name }}
                        </template>
                    </el-table-column>
                    <el-table-column label="方案类型" prop="schemeType" show-overflow-tooltip align="center"
                                     header-align="center">
                        <template slot-scope="scope">
                            {{ scope.row.schemeType|dict('impexp.schemeType') }}
                        </template>
                    </el-table-column>
                    <el-table-column label="文件类型" prop="fileType" show-overflow-tooltip align="center"
                                     header-align="center">
                        <template slot-scope="scope">
                            {{ scope.row.fileType|dict('impexp.fileType') }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" align="center" header-align="center" fixed="right" width="240">
                        <template slot-scope="scope">
                            <el-button size="mini" @click="edit(scope.row)" type="primary">编 辑</el-button>
                            <el-button size="mini" @click="configTest(scope.row)" type="success">配 置</el-button>
                            <el-button size="mini" @click="remove(scope.row.id)" type="danger">删 除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <el-pagination
                    @current-change="index=>loadData(index)"
                    :current-page.sync="page.index"
                    :page-size="page.size"
                    layout="total, prev, pager, next,jumper"
                    :total="page.total">
            </el-pagination>
            <el-dialog
                    :title="schemeTitle"
                    width="30%"
                    :close-on-click-modal="false"
                    :destroy-on-close=true
                    :visible.sync="dialog">
                <el-form :model="scheme" :rules="rules" ref="form" label-width="100px">
                    <el-form-item label="方案名称" prop="name" required>
                        <el-input v-model="scheme.name" placeholder="请输入方案名称"
                                  required clearable></el-input>
                    </el-form-item>
                    <el-form-item label="方案编码" prop="code" required>
                        <el-input v-model="scheme.code" placeholder="请输入方案编码"
                                  required clearable></el-input>
                    </el-form-item>
                    <el-form-item label="起始年度" prop="startYear" required>
                        <el-date-picker
                                v-model="scheme.startYear"
                                type="year"
                                format="yyyy"
                                value-format="yyyy"
                                placeholder="起始年度"
                                :clearable="true">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="终止年度" prop="endYear" required>
                        <el-date-picker
                                v-model="scheme.endYear"
                                type="year"
                                format="yyyy"
                                value-format="yyyy"
                                placeholder="终止年度"
                                :clearable="true">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="方案类型" prop="schemeType">
                        <select-dict v-model="scheme.schemeType" type="impexp.schemeType"/>
                    </el-form-item>
                    <!--<el-form-item label="文件类型" prop="fileType">
                      <select-dict v-model="scheme.fileType" style="width: 75%" type="impexp.fileType"/>
                    </el-form-item>-->
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button type="info" @click="dialog = false" class="btn-cancel">取 消</el-button>
                    <el-button type="primary" @click="saveScheme" v-loading="loading"
                               class="btn-submit">提 交
                    </el-button>
                </div>
            </el-dialog>
        </div>
    </section>
</template>

<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        mixins: [indexMixin],
        data() {
            return {
                schemeTitle: "",
                searchForm: {
                    schemeType: ''
                },
                page: {
                    index: 1,
                    size: 15,
                    total: 0
                },
                dialog: false,
                categoryId: "",
                categoryCode: "",
                scheme: {},
                applyTime: [],
                schemeData: [],
                dict: ['impexp.schemeType'],
                rules: {
                    code: [
                        {required: true, message: '请输入方案编码', trigger: 'change'},
                        {required: true, message: '请输入方案编码', trigger: 'blur'}
                    ],
                    name: [
                        {required: true, message: '请输入方案名称', trigger: 'change'},
                        {required: true, message: '请输入方案名称', trigger: 'blur'}
                    ],
                    startYear: [
                        {required: true, message: '请选择开始时间', trigger: 'change'},
                        {required: true, message: '请选择开始时间', trigger: 'blur'}
                    ],
                    endYear: [
                        {required: true, message: '请选择结束时间', trigger: 'change'},
                        {required: true, message: '请选择结束时间', trigger: 'blur'}
                    ],
                    schemeType: [
                        {required: true, message: '请选择方案类型', trigger: 'change'},
                        {required: true, message: '请选择方案类型', trigger: 'blur'}
                    ],
                    fileType: [
                        {required: true, message: '请选择文件类型', trigger: 'change'},
                        {required: true, message: '请选择文件类型', trigger: 'blur'}
                    ],
                }
            }
        },
        methods: {
            $init() {
                this.loadData(1)
            },
            loadData(index) {
                const param = Object.assign({}, {
                    name: this.searchForm.name,
                    index: index,
                    size: this.page.size,
                });
                this.$http.post('/impexpscheme/page', param)
                    .then(({data}) => {
                        if (data.success) {
                            this.schemeData = data.data.data;
                            this.page.index = data.data.start / data.data.size + 1;
                            this.page.size = data.data.size;
                            this.page.total = data.data.total
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    })
            },
            add() {
                this.schemeTitle = '添加';
                this.optype = 'add';
                this.dialog = true;
                this.scheme = {}
            },
            edit(row) {
                this.schemeTitle = '编辑';
                this.optype = 'edit';
                this.scheme = Object.assign({}, row);
                this.scheme.startYear = '' + this.scheme.startYear
                this.scheme.endYear = '' + this.scheme.endYear
                this.dialog = true
            },
            saveScheme() {
                this.$refs.form.validate((valid) => {
                    if (valid) {
                        let path = '';
                        if (this.optype === 'add') {
                            path = '/impexpscheme/insert'
                        } else if (this.optype === 'edit') {
                            path = '/impexpscheme/update'
                        }
                        this.scheme.businessId = "impexpscheme";
                        this.loading = true;
                        this.$http.post(path, this.scheme)
                            .then(({data}) => {
                                if (data.success) {
                                    this.$message.success("保存成功！");
                                    this.loadData(this.page.index);
                                    this.dialog = false
                                } else {
                                    this.$message.error(data.message)
                                }
                                this.loading = false
                            })
                    }
                })
            },
            remove(id) {
                this.$confirm("确认删除？", '提示', {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning',
                    dangerouslyUseHTMLString: true
                }).then(() => {
                    this.$http.post('/impexpscheme/delete', {aIds: id})
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
            configTest(row) {
                this.$router.push({
                    path: '/archive/manage/impscheme/item',
                    query: {id: row.id, name: row.name}
                })
            },
            back() {
                this.$router.push({
                    path: '../../../home',
                })
            },
        }
    }
</script>
