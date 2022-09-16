<template>
    <section>
      <nac-info title="权限管理">
            <config-form ref="form" @search="loadData"/>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table :data="data" border height="100%" @selection-change="handleTableSelect">
                    <el-table-column label="排序" type="index" align="center"/>
                    <el-table-column label="权限名称" header-align="center" align="center" show-overflow-tooltip>
                        <template slot-scope="scope">
                            <el-button type="text" size="small"
                                       @click="$router.push({path:'/system/permission/edit',query:{id:scope.row.id}})">
                                {{ scope.row.name }}
                            </el-button>
                        </template>
                    </el-table-column>
                    <el-table-column prop="description" label="权限描述" align="center" header-align="center"
                                     show-overflow-tooltip/>
                    <el-table-column label="权限类型" align="center" header-align="center">
                        <template slot-scope="scope">
                            {{ getProvideLabel(scope.row.type) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80" header-align="center" align="center" fixed="right">
                        <template slot-scope="scope">
                            <el-button type="text" size="small" @click="remove(scope.row.id)" v-if="!scope.row.sys">删 除
                            </el-button>
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
  import abstractResource from "./abstractResource";
  import indexMixin from '@dr/auto/lib/util/indexMixin'

  export default {
        extends: abstractResource,
        data() {
            return {
                path: '/sysPermission/'
            }
        },
        components: {ConfigForm},
        mixins: [indexMixin],
        methods: {
            $init() {
                this.loadResourceProvider()
                this.loadData()
            }
        }
    }
</script>
