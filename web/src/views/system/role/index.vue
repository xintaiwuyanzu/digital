<template>
    <section>
        <nac-info title="角色管理">
            <el-button type="primary" @click="$router.push('/system/role/edit')">添加角色
            </el-button>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <div class="table-container">
                <el-table :data="data" height="100%" :border="true">
                    <el-table-column label="序号" align="center" width="60">
                        <template slot-scope="scope">
                            {{ (page.index - 1) * page.size + scope.$index + 1 }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="name" label="角色名" min-width="100">
                        <template slot-scope="scope">
                            <el-button type="text"
                                       @click="$router.push({path:'/system/role/edit',query:{id:scope.row.id}})">
                                {{ scope.row.name }}
                            </el-button>
                        </template>
                    </el-table-column>
                    <el-table-column prop="description" label="角色描述" show-overflow-tooltip/>
                    <el-table-column prop="code" label="角色编码" width="150" show-overflow-tooltip/>
                    <el-table-column label="操作" width="120" align="center">
                        <template slot-scope="scope">
                            <el-button type="text" @click="showDialog(scope.row.id)">用户绑定</el-button>
                            <el-button type="text" @click="remove(scope.row.id)" v-if="!scope.row.sys">删 除</el-button>
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
        <el-dialog title="用户绑定" :visible.sync="dialogShow" width="80%">
            <el-transfer filterable :filter-method="filterMethod" :titles="['所有用户','选中用户']"
                         filter-placeholder="请输入用户名称搜索"
                         v-model="rolePersons" :data="persons">
            </el-transfer>
            <span slot="footer" class="dialog-footer">
          <el-button @click="dialogShow = false">取 消</el-button>
          <el-button type="primary" @click="bindRoleUser">确 定</el-button>
        </span>
        </el-dialog>
    </section>
</template>
<script>
  import indexMixin from '@dr/auto/lib/util/indexMixin'

  export default {
        data() {
            return {
                path: 'sysrole',
                dialogShow: false,
                rolePersons: [],
                selectRoleId: '',
                persons: []
            }
        },
        methods: {
            $init() {
                this.loadData()
            },
            filterMethod(query, item) {
                return item.label.indexOf(query) > -1;
            },
            async showDialog(id) {
                if (this.persons.length === 0) {
                    const p = await this.$post('/person/page', {page: false})
                    this.persons = p.data.data.map(p => ({key: p.id, label: p.userName}))
                }
                const rp = await this.$post('/sysrole/roleUser', {id})
                this.rolePersons = rp.data.data
                this.selectRoleId = id
                this.dialogShow = true
            },
            async bindRoleUser() {
                await this.$post('/sysrole/bindRoleUser', {
                    id: this.selectRoleId,
                    personIds: this.rolePersons.join(',')
                })
                this.dialogShow = false
                this.$message.success('绑定成功！')
            }
        },
        mixins: [indexMixin]
    }
</script>
