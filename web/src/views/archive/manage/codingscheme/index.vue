<template>
    <section>
        <nac-info title="档号生成规则">
            <el-button type="primary" v-if="current.id" size="mini" @click="showDialog">添 加</el-button>
            <el-button type="danger" v-if="multipleSelection.length>0" size="mini" @click="remove">删 除</el-button>
        </nac-info>
        <div style="display: flex;flex-direction: row;height: 100%;overflow: auto;padding: 5px">
            <fond-tree autoSelect @check="check" style="overflow:scroll"/>
            <div class="index_main" style="padding: 0px" v-loading="loading">
                <div class="table-container">
                    <el-table ref="multipleTable"
                              :data="data"
                              border
                              type="selection"
                              height="100%"
                              show-overflow-tooltip="true"
                              @selection-change="handleSelectionChange">
                        <el-table-column
                                type="selection"
                                width="55">
                        </el-table-column>
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
                        <el-table-column label="起始年度" prop="startYear" show-overflow-tooltip align="center"
                                         header-align="center">
                            <template slot-scope="scope">
                                {{ scope.row.startYear }}
                            </template>
                        </el-table-column>
                        <el-table-column label="终止年度" prop="endYear" show-overflow-tooltip align="center"
                                         header-align="center">
                            <template slot-scope="scope">
                                {{ scope.row.endYear }}
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" align="center" header-align="center" fixed="right" width="250">
                            <template slot-scope="scope">
                                <el-button size="mini" @click="edit(scope.row)" type="primary">编 辑</el-button>
                                <el-button size="mini" @click="configItem(scope.row)" type="warning">配 置</el-button>
                                <el-button size="mini" @click="setDefault(scope.row)"
                                           :type="scope.row.default?'info':'success'"
                                           :disabled="scope.row.default">设为默认
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                <page :page="page" @change="pageIndex=>loadData({pageIndex})"/>
            </div>
        </div>
        <form-dialog :category="current" v-if="isShowDialog" :isShowDialog="isShowDialog" :id="id"
                     :businessId="this.current.id" @func="loadData"/>
        <sort-dialog :pitch-on="pitchOn"
                     :field="field"
                     ref="sort"
                     :show-scheme-id="showSchemeId">
        </sort-dialog>
    </section>
</template>
<script>
    import indexMixin from "@dr/auto/lib/util/indexMixin";
    import formDialog from './form'
    import sortDialog from './sort'

    export default {
        mixins: [indexMixin],
        components: {formDialog, sortDialog},
        watch: {
            current() {
                this.loadData()
            }
        },
        computed: {
            pro() {
                if (this.current && this.current.data) {
                    return this.current.data.archiveType === "2"
                }
                return false
            }
        },
        data() {
            return {
                form: {},
                current: {},
                id: '',
                multipleSelection: [],
                pitchOn: [],
                field: [],
                showSchemeId: "",
                isShowDialog: false,
                dialogVisible: false
            }
        },
        methods: {
            edit(r) {
                this.isShowDialog = true
                this.id = r.id
            },
            check(v) {
                this.current = v.data
            },
            /**
             * 设置默认配置
             * @param row
             * @returns {Promise<void>}
             */
            async setDefault(row) {
                this.loading = true;
                row.default = true;
                if (!row.startYear) {
                    row.startYear = 0
                }
                if (!row.endYear) {
                    row.endYear = 0
                }
                const {data} = await this.$http.post("/codingscheme/update", row)
                if (data.success) {
                    this.$message.success("设置默认成功");
                    await this.loadData()
                } else {
                    this.$message.error(data.message)
                }
                this.loading = false
            },
            configItem(row) {
                this.showSchemeId = row.id
                this.loading = true
                const param = Object.assign({}, {businessId: this.showSchemeId})
                this.$http.post('/codingscheme/schemeitem/page', param).then(({data}) => {
                    if (data.success) {
                        this.pitchOn = data.data.data
                        this.$http.post("/codingscheme/getFieldCheck", {fond: true}).then(({data}) => {
                            if (data.success) {
                                this.field = data.data
                                if (this.pitchOn && this.pitchOn.length > 0) {
                                    for (const x of this.pitchOn) {
                                        for (let i = 0; i < this.field.length; i++) {
                                            if (x.code === this.field[i].code) {
                                                this.field.splice(i, 1)
                                            }
                                        }
                                    }
                                } else {
                                    this.pitchOn = []
                                }
                            } else {
                                this.$message.error(data.message)
                            }
                            this.dialogVisible = true
                            this.loading = false
                        })
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            /**
             * 加载数据
             * @param pa
             * @returns {Promise<void>}
             */
            async loadData(pa) {
                if (this.current && this.current.id) {
                    this.loading = true
                    const param = Object.assign({businessId: this.current.id}, this.page, pa);
                    const {data} = await this.$http.post('/codingscheme/page', param)
                    if (data.success) {
                        this.data = data.data.data;
                        for (const datum of this.data) {
                            if (datum.startYear === 0) {
                                datum.startYear = null
                            }
                            if (datum.endYear === 0) {
                                datum.endYear = null
                            }
                        }
                        this.page.index = data.data.start / data.data.size + 1;
                        this.page.size = data.data.size;
                        this.page.total = data.data.total
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                } else {
                    this.data = []
                    this.page = {size: 15, index: 0, total: 0}
                }
            },
            async remove() {
                if (this.multipleSelection.length <= 0) {
                    this.$message.warning("请选择至少一项删除");
                    return
                }
                let ids = this.multipleSelection.map(s => s.id).join(',');
                try {
                    const v = await this.$confirm("确认删除？", '提示', {
                        confirmButtonText: '确认',
                        cancelButtonText: '取消',
                        type: 'warning',
                        dangerouslyUseHTMLString: true
                    })
                    if (v) {
                        this.loading = true
                        const {data} = await this.$http.post('/codingscheme/delete', {aIds: ids})
                        if (data.success) {
                            this.$message.success("删除成功");
                            await this.loadData()
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    }
                } catch {
                }
            },
            handleSelectionChange(val) {
                this.multipleSelection = val;
            },
            showDialog() {
                this.isShowDialog = true
                this.id = ''
            }
        }
    }
</script>
