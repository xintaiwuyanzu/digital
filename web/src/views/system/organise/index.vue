<template>
    <section>
        <nac-info title="机构管理">
            <row>
                <el-row>
                    <el-col :span="12">
                        <el-button type="primary" icon="el-icon-plus" @click="organSyn">组织机构同步</el-button>
                    </el-col>
                    <el-col :span="12">
                        <el-form inline>
                            <el-form-item>
                                <el-input placeholder="输入关键字进行过滤" v-model="filterText" clearable>
                                    <i slot="prefix" class="el-input__icon el-icon-search"/>
                                </el-input>
                            </el-form-item>
                        </el-form>
                    </el-col>
                </el-row>
            </row>
        </nac-info>
        <div class="index_main">
            <el-tree class="sysMenuTree"
                     :data="menuData"
                     :props="treeDefaultProps"
                     v-loading="treaLoading"
                     default-expand-all :filter-node-method="filterNode"
                     ref="menuTree">
                <div style="flex: 1;margin: 2px;" slot-scope="{ node, data }">
                    <el-tag style="float: left;font-size: 16px" :type="data.data.status==='1'?'':'danger'">
                        {{ data.data.organiseName }}
                    </el-tag>
                    <span class="buttons">
                  <el-button v-if="data.level===0" type="primary" size="mini" @click="editMenu">
                    添加同级机构
                  </el-button>
                  <el-button v-if="!data.data.leaf" type="primary" size="mini" @click="editMenu({parentId: data.id})">
                    添加子机构
                  </el-button>
                        <!--<el-button v-if="data.level!=0" type="primary" size="mini" @click="house(data)">
                         房屋分配
                       </el-button>-->
                  <el-button type="primary" size="mini" @click="editMenu(data.data)">
                    编辑
                  </el-button>
                  <el-button :type="data.data.status==='1'?'warning':'primary'" size="mini" @click="toggle(data)">
                    {{ data.data.status === '1' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" size="mini" v-if="node.childNodes.length===0" @click="remove(data)">
                    删除
                  </el-button>
                </span>
                </div>
            </el-tree>
        </div>
        <organise-form ref="orgForm"/>
    </section>
</template>
<script>
    import organiseForm from './form'

    export default {
        components: {organiseForm},
        data() {
            return {
                filterText: '',
                treaLoading: false,
                menuData: [],
            }
        },
        watch: {
            filterText(val) {
                this.$refs.menuTree.filter(val)
            }
        },
        methods: {
            $init() {
                this.loadData()
            },
            handleClick(tab) {
                this.activeName = tab.name
            },
            filterNode(value, data) {
                if (!value) return true
                return data.label.indexOf(value) !== -1
            },
            loadData() {
                this.treaLoading = true
                this.$http.post('/organise/organiseTree', {all: true, parentId: this.sysId})
                    .then(({data}) => {
                        if (data.success) {
                            this.menuData = data.data ? data.data : []
                        }
                        this.treaLoading = false
                    })
            },
            editMenu(menu) {
                let formData = {
                    sysId: this.sysId,
                    parentId: this.sysId,
                    status: '1',
                    leaf: false,
                    order: 1
                }
                if (menu) {
                    formData = Object.assign(formData, menu)
                }
                this.$refs.orgForm.editForm(formData)
            },
            house(data) {
                this.$router.push({path: this.$route.path + '/room', query: {organiseId: data.id, label: data.label}})
            },
            remove(data) {
                let orgId = data.id
                this.$confirm(`删除机构将会删除该机构及所有下属机构和人员，以及相关的登录账号。确认要删除机构【${data.label}】吗？`, '提示', {type: 'waring'})
                    .then(() => {
                        this.treaLoading = true
                        this.$http.post('/person/page', {
                            defaultOrganiseId: data.id,
                            page: false
                        }).then(({data}) => {
                            if (data && data.success) {
                                if (data.data.length > 0) {
                                    this.$message.error('当前部门下存在用户，暂不能删除！')
                                    this.treaLoading = false
                                } else {
                                    this.$http.post('/organise/delete', {id: orgId})
                                        .then(({data}) => {
                                            if (data.success) {
                                                this.$message.success('删除成功！')
                                            } else {
                                                this.$message.error(data.message)
                                            }
                                            this.loadData()
                                        })
                                }
                            } else {
                                this.$message.error(data.message)
                            }
                        })
                    })
            },
            toggle(data) {
                this.treaLoading = true
                let status = data.data.status === '1' ? '0' : '1'
                this.$http.post('/organise/update', Object.assign({}, data.data, {status}))
                    .then(({data}) => {
                        if (data.success) {
                            this.$message.success('操作成功！')
                        }
                        this.loadData()
                    })
            },
            //组织机构同步
            async organSyn() {
                try {
                    await this.$confirm('此操作将更新组织机构和人员信息,是否继续?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    const data = await this.$post('/bsp/syncBspData')
                    if (data.data.success) {
                        this.$message.success('正在同步数据中，请稍后进行查看‥‥')
                    } else {
                        this.$message.success(data.data.message)
                    }
                } catch {
                }
            }
        },
        mounted() {
            this.loadData()
        }
    }
</script>
<style lang="scss">
    .sysMenuTree {
        height: 100%;
        overflow: auto;
        padding: 5px;

        .el-tree-node__content {
            height: auto;

            .buttons {
                float: right;
            }
        }
    }
</style>
