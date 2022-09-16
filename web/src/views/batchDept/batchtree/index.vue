<template>
    <section>
        <nac-info back title="批次分类管理"/>
        <div class="index_main category_index">
            <el-row style="overflow: hidden">
                <el-col :span="5">
                    <el-card shadow="hover">
                        <div slot="header">
                            <strong>批次分类</strong>
                        </div>
                        <fond-tree :fondId="fondId" @check="check" ref="fondTree" style="height: 100%"/>
                    </el-card>
                </el-col>
                <el-col :span="19">
                    <el-card shadow="hover">
                        <div slot="header">
                            <strong>分类详情</strong>
                        </div>
                        <el-form :model="form" ref="form" label-width="120px" inline v-loading="loading">
                            <el-form-item :label="type+'名称'" prop="name"
                                          :rules="[
                                      { required: true, message: ''+type+''+'名称不能为空'}
                                    ]">
                                <el-input v-model="form.name" :placeholder="'请输入'+type+'名称'" clearable/>
                            </el-form-item>
                            <el-form-item :label="type+'编码'" prop="code"
                                          :rules="[
                                      { required: true, message: ''+type+''+'编码不能为空'}
                                    ]">
                                <el-input v-model="form.code" :placeholder="'请输入'+type+'编码'" clearable/>
                            </el-form-item>
                            <el-form-item :label="type+'类型'" prop="categoryType" :required="type === '门类'"
                                          :rules="[
                                      { required: true, message: ''+type+''+'类型不能为空'}
                                    ]">
                                <select-dict v-model="form.categoryType" clearable type="archive.ML"
                                             :placeholder="'请输入'+type+'类型'"/>
                            </el-form-item>
                            <el-form-item label="档案类型" prop="archiveType" :required="type === '门类'"
                                          :rules="[
                                      { required: true, message: ''+type+''+'档案类型不能为空'}
                                    ]">
                                <select-dict v-model="form.archiveType" type="archiveTypes" placeholder="请选择档案类型"/>
                            </el-form-item>
                            <el-form-item label="顺序号" prop="order">
                                <el-input v-model="form.order" placeholder="请输入顺序号" clearable/>
                            </el-form-item>
                            <el-form-item label="描述" prop="represent">
                                <el-input type="textarea" v-model="form.description" placeholder="请输入描述" clearable/>
                            </el-form-item>
                            <br>
                            <el-form-item label=" ">
                                <el-button type="primary" @click="save" v-if="parentId">保存</el-button>
                                <el-button type="primary" @click="form={}" v-if="parentId">重置</el-button>
                                <el-button type="primary" @click="form={}" v-if="(form.id&&'template'!==fondId)">添加同级
                                </el-button>
                                <el-button type="primary" @click="addChild" v-if="form.id">添加下级</el-button>
                                <el-button type="danger" @click="remove" v-if="form.id">删除</el-button>
                            </el-form-item>
                            <br>
                        </el-form>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </section>
</template>
<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'
    import CategoryTree from "@/components/categoryTree";

    export default {
        components: {CategoryTree},
        props: {id: {type: String, required: false}},
        mixins: [indexMixin],
        data() {
            let fondId = this.$route.query.id
            if (!fondId) {
                fondId = this.id
            }
            return {
                dict: ['archives.ML'],
                fondId,
                parentId: fondId,
                type: '分类',
                form: {}
            }
        },
        methods: {
            //删除数据
            async remove() {
                try {
                    await this.$confirm('删除门类会删除与之相关的所有数据，确定要删除吗？', '提示')
                    this.loading = true
                    const {data} = await this.$post(`/category/delete`, {id: this.form.id, deleteChildren: true})
                    if (data.success) {
                        this.$message.success('删除成功！')
                        this.form = {}
                        //重新加载全宗数据
                        await this.$refs.fondTree.loadCategory()
                    } else {
                        this.$message.error(data.message)
                    }
                } catch {
                    this.loading = false
                }
                this.loading = false
            },
            check(v) {
                this.parentId = v.parentId
                this.detail(v.id)
            },
            //根据Id加载门类数据
            async detail(id) {
                this.loading = true
                const {data} = await this.$post(`/category/detail`, {id})
                this.form = data.data
                this.loading = false;
            },
            //添加下级按钮
            addChild() {
                this.parentId = this.form.id
                this.form = {}
            },
            //保存数据
            async save() {
                this.loading = true
                try {
                    const valid = await this.$refs.form.validate()
                    if (valid) {
                        //默认参数
                        const defaultParams = {
                            registerId: this.fondId,
                            businessId: this.fondId,
                            parentId: this.parentId
                        }
                        const {data} = await this.$post(
                            `/category/${this.form.id ? 'update' : 'insert'}`,
                            Object.assign(defaultParams, this.form)
                        )
                        if (data.success) {
                            this.form = data.data
                            this.$message.success('保存成功！')
                        } else {
                            this.$message.error(data.message)
                        }
                        //重新加载全宗数据
                        await this.$refs.fondTree.loadCategory()
                    }
                    this.loading = false
                } catch {
                    this.loading = false
                }
            },
            async $init() {
                if (!this.fondId) {
                    this.$message.error('全宗Id不能为空')
                    this.$router.back()
                }
            }
        }
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
</style>
