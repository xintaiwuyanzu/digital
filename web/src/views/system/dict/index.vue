<template>
    <section>
        <nac-info title="字典管理">
            <config-form ref="form" @search="loadData"/>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table :data="data" border height="100%" @selection-change="handleTableSelect">
                    <el-table-column label="排序" type="index" align="center"/>
                    <el-table-column prop="key" label="编码" header-align="center" align="center" show-overflow-tooltip/>
                    <el-table-column prop="value" label="值" header-align="center" align="center" show-overflow-tooltip/>
                    <el-table-column prop="description" label="描述" width="300" align="center" header-align="center"
                                     show-overflow-tooltip/>
                    <el-table-column prop="order" label="排序" width="80" header-align="center" align="center"/>
                    <el-table-column prop="status" label="可用" width="50" align="center" header-align="center">
                        <template slot-scope="scope">
                            {{ scope.row.status|dict({1: "是", 0: "否"}) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" header-align="center" align="center" fixed="right">
                        <template slot-scope="scope">
                            <el-button type="text" size="small" @click="editForm(scope.row)">编 辑</el-button>
                            <el-button type="text" size="small" @click="remove(scope.row.id)">删 除</el-button>
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
        </div>
    </section>
</template>

<script>
  import ConfigForm from './form'
  import indexMixin from '@dr/auto/lib/util/indexMixin'

  export default {
        data() {
            return {
                path: 'sysDict',
                dict: ['state']
            }
        },
        components: {ConfigForm},
        mixins: [indexMixin],
        methods: {
            $init() {
                this.loadData(this.$refs.form.getSearchForm())
            },
            apiPath() {
                return '/sysDict/'
            },
        }
    }
</script>

<style scoped>

</style>
