<template>
    <section>
        <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
            <el-form-item label="权限名称：" prop="name">
                <el-input v-model="searchForm.name" placeholder="请输入权限名称" clearable/>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="search">搜 索</el-button>
                <el-button @click="$refs.searchForm.resetFields()">重 置</el-button>
                <el-button type="primary" @click="$router.push('/system/permission/edit')">添加</el-button>
            </el-form-item>
        </el-form>
    </section>
</template>

<script>
  import fromMixin from '@dr/auto/lib/util/formMixin'

  export default {
        data() {
            return {
                path: '/sysPermission/',
                searchForm: {name: ''},
            }
        },
        mixins: [fromMixin]
    }
</script>
