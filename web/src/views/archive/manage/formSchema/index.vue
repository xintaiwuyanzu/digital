<template>
    <section>
        <nac-info title="表单方案">
            <el-button type="primary" v-if="current.id" size="mini" @click="()=>{show=true;id=''}">添 加</el-button>
            <el-button type="danger" v-if="multipleSelection.length>0" size="mini" @click="remove">删 除</el-button>
        </nac-info>
        <div style="display: flex;flex-direction: row;height: 100%;overflow: auto;padding: 5px">
            <fond-tree autoSelect @check="check" style="overflow:scroll"/>
            <div class="index_main" style="padding: 0px" v-loading="loading">
                <div class="table-container">
                    <el-table ref="multipleTable"
                              :data="data"
                              border
                              height="100%"
                              @selection-change="handleSelectionChange"
                              empty-text="请选择左侧全宗门类">
                        <column-index :page="page"/>
                        <el-table-column type="selection" width="50" align="center"/>
                        <el-table-column label="方案名称" prop="name" width="100" show-overflow-tooltip align="center"/>
                        <el-table-column label="项目表单" prop="proFormName" show-overflow-tooltip align="center"
                                         v-if="pro"/>
                        <el-table-column label="案卷表单" prop="arcFormName" show-overflow-tooltip align="center"/>
                        <el-table-column label="文件表单" prop="fileFormName" show-overflow-tooltip align="center"/>
                        <el-table-column label="起始年度" prop="startYear" width="80" show-overflow-tooltip align="center"/>
                        <el-table-column label="终止年度" prop="endYear" width="80" align="center"/>
                        <el-table-column label="操作" align="center" header-align="center" width="180">
                            <template slot-scope="scope">
                                <el-button size="mini" type="text" @click="()=>edit(scope.row)">编 辑</el-button>
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
        <form-dialog :category="current" :show="show" :id="id"/>
    </section>
</template>
<script>
  import indexMixin from "@dr/auto/lib/util/indexMixin";
  import formDialog from './form'

  export default {
        mixins: [indexMixin],
        components: {formDialog},
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
                show: false,
                id: '',
                multipleSelection: []
            }
        },
        methods: {
            edit(r) {
                this.show = true
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
                const {data} = await this.$http.post("/manage/categoryconfig/update", row)
                if (data.success) {
                    this.$message.success("设置默认成功");
                    await this.loadData()
                } else {
                    this.$message.error(data.message)
                }
                this.loading = false
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
                    const {data} = await this.$http.post('/manage/categoryconfig/page', param)
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
                        const {data} = await this.$http.post('/manage/categoryconfig/delete', {ids: ids})
                        if (data.success) {
                            this.$message.success("删除成功");
                            await this.loadData()
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    }
                } catch {
                    this.loading = false
                }
            },
            handleSelectionChange(val) {
                this.multipleSelection = val;
            }
        }
    }
</script>
