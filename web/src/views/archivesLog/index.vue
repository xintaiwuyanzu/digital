<template>
    <section class="archiveLibIndex">
        <nac-info title="流转记录">
        </nac-info>
        <div class="index_main category_index">
            <el-row style="overflow: hidden">
                <el-col :span="4">
                    <el-card shadow="hover">
                        <fond-tree :autoSelect="true" @check="check" @change="change" ref="fondTree"
                                   style="height: 100%"/>
                    </el-card>
                </el-col>
                <el-col :span="20">
                    <el-card shadow="hover" v-if="isShow">
                        <nac-info title="流转记录信息">
                            <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
                                <el-form-item>
                                    <archiveFrom ref="form"
                                                 :archiveType="archveType"
                                                 :archiveCode="''"
                                                 :registerId="registerId"
                                                 @loadData="loadData"
                                                 @func="loadData">
                                    </archiveFrom>
                                </el-form-item>
                            </el-form>
                        </nac-info>
                        <div class="table-container">
                            <el-table v-show="archveType=='2'"
                                      :data="archiveData"
                                      stripe
                                      border
                                      v-loading="loading"
                                      element-loading-text="拼命加载中..."
                                      height="74vh"
                                      style="width: 100%"
                                      highlight-current-row
                                      @selection-change="handleSelectionChange">
                                <el-table-column
                                        type="selection"
                                        width="45">
                                </el-table-column>
                                <el-table-column label="排序" type="index" align="center"/>
                                <el-table-column prop="dangHao" label="档号" header-align="center" show-overflow-tooltip
                                                 width="300" sortable align="center"/>

                                <el-table-column prop="logDescription" label="日志描述" header-align="center"
                                                 show-overflow-tooltip
                                                 align="center"/>
                                <el-table-column prop="operatorName" label="操作人" header-align="center"
                                                 show-overflow-tooltip
                                                 sortable width="150" align="center"/>
                                <el-table-column prop="operatorDate" label="操作时间" header-align="center"
                                                 show-overflow-tooltip
                                                 width="150" align="center">
                                    <template slot-scope="scope">
                                        <span>{{ timestampToTime(scope.row.operatorDate) }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="caoZuoHuanJie" label="操作环节" header-align="center"
                                                 show-overflow-tooltip
                                                 sortable align="center">
                                    <template slot-scope="scope">
                                        <span>{{ scope.row.caoZuoHuanJie|dict('taskTrace.activityCode') }}</span>
                                    </template>
                                </el-table-column>
                            </el-table>
                            <el-table v-show="archveType!='2'"
                                      :data="archiveData"
                                      stripe
                                      border
                                      v-loading="loading"
                                      element-loading-text="拼命加载中..."
                                      height="74vh"
                                      style="width: 100%"
                                      @selection-change="handleSelectionChange"
                                      highlight-current-row>
                                <el-table-column
                                        type="selection"
                                        width="45">
                                </el-table-column>
                                <el-table-column label="排序" type="index" align="center"/>
                                <el-table-column prop="dangHao" label="档 号" header-align="center" show-overflow-tooltip
                                                 align="center"/>
                                <el-table-column prop="anJuanTiMing" label="题 名" header-align="center"
                                                 show-overflow-tooltip
                                                 align="center"/>
                                <el-table-column prop="logDescription" label="日志描述" header-align="center"
                                                 show-overflow-tooltip
                                                 width="300"
                                                 align="center"/>
                                <el-table-column prop="operatorName" label="操作人" header-align="center"
                                                 show-overflow-tooltip
                                                 width="150" align="center"/>
                                <el-table-column prop="operatorDate" label="操作时间" header-align="center"
                                                 show-overflow-tooltip
                                                 width="150" align="center">
                                    <template slot-scope="scope">
                                        <span>{{ timestampToTime(scope.row.operatorDate) }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="caoZuoHuanJie" label="操作环节" header-align="center"
                                                 show-overflow-tooltip
                                                 align="center">
                                    <template slot-scope="scope">
                                        <span>{{ scope.row.caoZuoHuanJie|dict('hjcode') }}</span>
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
                    </el-card>
                    <el-card shadow="hover" v-if="!isShow">
                        <span>请选择左侧批次门类树</span>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </section>
</template>
<script>
  import indexMixin from '@/util/indexMixin'
  import archiveFrom from "./form";

  export default {
        components: {archiveFrom},
        mixins: [indexMixin],
        data() {
            return {
                dict: ['hjcode'],
                archiveData: [],
                searchForm: {},
                isShow: false,
                archveType: "",
                registerId: '',
                page: {
                    size: 15,
                    index: 0,
                    total: 0
                },
                multipleSelection: [],
            }
        },
        methods: {
            handleSelectionChange(val) {
                this.$refs.form.currentSelect = val
                this.multipleSelection = val;
            },
            timestampToTime(timestamp) {
                if (timestamp != 0 && timestamp != undefined) {
                    return this.$moment(timestamp).format('YYYY-MM-DD HH:mm:ss')
                }
            },
            /**
             * 左侧点击事件
             *
             * @param v
             */
            check(v) {
                this.isShow = true
                this.archveType = v.data.archiveType
                this.registerId = v.data.id;
                this.loadData()
            },
            change() {
                this.isShow = false
            },
            /**
             * 加载案卷信息
             *
             * @param dangHao
             * @param anJuanTiMing
             */
            loadData(params, dangHao, anJuanTiMing, boxNumber) {
                this.loading = true;
                let pageIndex = ''
                if (params != undefined) {
                    pageIndex = params.pageIndex
                }
                let params1 = Object.assign({}, {
                    registerId: this.registerId,
                    anJuanTiMing: anJuanTiMing,
                    dangHao: dangHao,
                    boxNumber: boxNumber,
                    pageIndex: pageIndex,
                })
                this.$http.post('archivesLog/page', params1).then(({data}) => {
                    if (data.success) {
                        console.log(this.archiveData)
                        this.archiveData = data.data.data
                        this.page.index = data.data.start / data.data.size + 1
                        this.page.size = data.data.size
                        this.page.total = data.data.total
                    }
                    this.loading = false
                })
            },
        },
    }
</script>
<style lang="scss" scoped>
    .category_index {
        .el-row {
            flex: 1;

            .el-col {
                height: 100%;
                display: flex;

                .el-card {
                    flex: 1;
                    overflow: auto;
                }
            }
        }
    }

    .index_m {
        display: flex;
        height: 100%;

        .table-container {
            flex: 1;

            td.outDateStyle {
                color: #ff4d51;
            }

            td.editColumn {
                > div {
                    display: flex;

                    > section {
                        margin-left: 6px;
                    }
                }
            }
        }
    }
</style>
