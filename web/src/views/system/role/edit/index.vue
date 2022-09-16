<template>
  <section>
    <nac-info :title="(id?'编辑':'添加')+'角色'" back>
      <el-button type="primary" @click="save">保存</el-button>
    </nac-info>
    <div class="index_main">
      <el-form :model="form" :rules="rules" label-width="120px" ref="form">
        <el-form-item label="角色名称" prop="name" required>
          <el-input v-model="form.name" clearable/>
        </el-form-item>
        <el-form-item label="角色编码" prop="code" required>
          <el-input v-model="form.code" clearable/>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" clearable/>
        </el-form-item>
        <el-form-item label="角色权限">
          <el-transfer filterable :filter-method="filterMethod"
                       :titles="['所有权限','选中权限']" filter-placeholder="请输入权限名称搜索"
                       v-model="rolePermissions" :data="permissions">
          </el-transfer>
        </el-form-item>
      </el-form>
    </div>
  </section>
</template>
<script>
export default {
  data() {
    return {
      id: this.$route.query.id,
      form: {
        name: '',
        description: '',
        code: ''
      },
      rolePermissions: [],
      permissions: [],
      rules:{
        name: [
          {required: true,message: '请填写角色名称', trigger: 'blur'},
          {required: true,message: '请填写角色名称', trigger: 'change'}
        ],
        code: [
          {required: true,message: '请填写角色编码', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    save() {
      this.$refs.form.validate((success) => {
        if (success) {
          const params = Object.assign({permissions: this.rolePermissions.join(',')}, this.form)
          this.$post(`/sysrole/${this.id ? 'update' : 'insert'}`, params)
              .then((data) => {
                if(data.data.code=='200'){
                  this.$message.success('保存成功')
                  this.$router.back()
                }else{
                  this.$message.error(data.data.message)
                  this.$router.back()
                }
              })
        }
      })
    },
    filterMethod(query, item) {
      return item.label.indexOf(query) > -1;
    },
    async $init() {
      if (this.id) {
        //查询角色基本信息
        const {data} = await this.$post('/sysrole/detail', {id: this.id})
        this.form = data.data
        //查询角色的所有权限
        const rp = await this.$post('/sysrole/rolePermission', {id: this.id})
        this.rolePermissions = rp.data.data
      }
      //查询所有的权限
      const rs = await this.$post('/sysPermission/page', {page: false})
      this.permissions = rs.data.data.map(p => ({key: p.id, label: p.name}))
    }
  }
}
</script>
