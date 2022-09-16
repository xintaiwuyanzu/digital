<template>
    <section>
        <nac-info :title="(id?'编辑':'添加')+'权限'" back>
            <el-button type="primary" @click="save">保存</el-button>
        </nac-info>
        <div class="index_main">
            <el-form :model="form" :rules="rules" label-width="120px" ref="form">
                <el-form-item label="权限名称" prop="name" required>
                    <el-input v-model="form.name" clearable/>
                </el-form-item>
                <el-form-item label="权限描述" prop="description">
                    <el-input v-model="form.description" clearable/>
                </el-form-item>
                <el-form-item label="权限类型" prop="type" required>
                    <el-select v-model="form.type" :disabled="!!id" clearable @change="()=>form.groupId=''">
                        <el-option v-for="p in providers" :key="p.type" :label="p.name" :value="p.type"/>
                    </el-select>
                </el-form-item>
                <el-form-item label="权限分组" prop="groupId" required v-if="groups.length>1">
                    <el-select v-model="form.groupId" clearable :disabled="!!id">
                        <el-option v-for="p in groups" :key="p.id" :label="p.batch_name" :value="p.id"/>
                    </el-select>
                </el-form-item>
                <el-form-item prop="code" required>
                    <span slot="label">
                             权限编码
                       <el-tooltip content="选择权限" placement="right">
                          <el-button icon="el-icon-search" type="success" circle size="mini" @click="showSearchDialog"/>
                       </el-tooltip>
                    </span>
                    <el-dialog :visible.sync="dialogShow" title="选择权限" width="80%">
                        <resource-tree :resources="resources" :parts="parts" ref="tree" :code="form.code"/>
                        <span slot="footer" class="dialog-footer">
                               <el-button @click="dialogShow = false">取 消</el-button>
                               <el-button type="primary" @click="readCode">确 定</el-button>
                        </span>
                    </el-dialog>
                    <el-input type="textarea" v-model="form.code" clearable/>
                </el-form-item>
            </el-form>
        </div>
    </section>
</template>
<script>
    import abstractResource from "../abstractResource";
    import resourceTree from './resourceTree'

    export default {
        data() {
            return {
                id: this.$route.query.id,
                self: false,
                form: {
                    type: '',
                    groupId: '',
                    code: ''
                },
                dialogShow: false,
                rules: {
                    name: [
                        {required: true, message: '请填写权限名称', trigger: 'blur'},
                        {required: true, message: '请填写权限名称', trigger: 'change'}
                    ],
                    type: [
                        {required: true, message: '请选择权限类型', trigger: 'blur'}
                    ],
                    code: [
                        {required: true, message: '请选择权限类型', trigger: 'blur'}
                    ]
                }
            }
        },
        computed: {
            resourceType() {
                return this.form.type
            }
        },
        methods: {
            save() {
                this.$refs.form.validate((success) => {
                    if (success) {
                        this.$post(`/sysPermission/${this.id ? 'update' : 'insert'}`, this.form)
                            .then(() => {
                                this.$message.success('保存成功')
                                this.$router.back()
                            })
                    }
                })
            },
            $init() {
                this.loadResourceProvider()
                if (this.id) {
                    this.$post('/sysPermission/detail', {id: this.id})
                        .then(({data}) => {
                            this.form = data.data
                        })
                }
            },
            readCode() {
                this.form.code = this.$refs.tree.getCode()
                this.dialogShow = false
            },
            async doShow() {
                await this.loadResource(this.form.type, this.form.groupId)
                if (this.resources.length > 0) {
                    this.$refs.tree?.$init(this.form.code)
                    this.dialogShow = true
                } else {
                    this.$message.warning('未找到指定权限和分组的资源')
                }
            },
            showSearchDialog() {
                if (!this.form.type) {
                    this.$message.warning("请选择权限类型")
                } else if (this.groups.length > 1) {
                    if (this.form.groupId) {
                        if (this.form.groupId === '*') {
                            this.$message.warning("所有分组不能选择权限")
                        } else {
                            this.doShow()
                        }
                    } else {
                        this.$message.warning("请选择权限分组")
                    }
                } else {
                    this.doShow()
                }
            }
        },
        extends: abstractResource,
        components: {resourceTree}
    }
</script>