<template>
  <section>
    <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
      <el-form-item label="编码：" prop="key">
        <el-input v-model="searchForm.key" placeholder="请输入字典编码" clearable/>
      </el-form-item>
      <el-form-item label="描述：" prop="description">
        <el-input v-model="searchForm.description" placeholder="请输入字典描述" clearable/>
      </el-form-item>
      <el-form-item label="是否可用：" prop="status">
        <el-select v-model="searchForm.status" clearable style="width: 60px" placeholder="">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search" size="mini">搜 索</el-button>
        <el-button @click="$refs.searchForm.resetFields()">重 置</el-button>
        <el-button type="primary" @click="editForm()" size="mini">添加</el-button>
      </el-form-item>
    </el-form>
    <el-dialog :visible.sync="edit" :title="(form.id?'编辑':'添加')+'字典'" width="30%">
      <el-form :model="form" :rules="rules" ref="form" label-width="90px">
        <el-form-item label="编码：" prop="key" >
          <el-input v-model="form.key" placeholder="请输入字典编码" clearable/>
        </el-form-item>
        <el-form-item label="值：" prop="value" >
          <el-input v-model="form.value" placeholder="请输入字典值" clearable/>
        </el-form-item>
        <el-form-item label="排序：" prop="order" >
          <el-input v-model="form.order" placeholder="请输入字典排序" clearable/>
        </el-form-item>
        <el-form-item label="是否可用：" prop="status">
          <el-switch v-model="form.status" active-text="是" inactive-text="否" :active-value="1"
                     :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="描述：" prop="description">
          <el-input type="textarea" v-model="form.description" :autosize="{ minRows: 3, maxRows: 6 }"
                    placeholder="请输入字典描述" clearable/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="saveForm" v-loading="loading">保 存</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import fromMixin from '@dr/auto/lib/util/formMixin'

export default {
  data() {
    return {
      searchForm: {},
      rules: {
        key: [
          {required: true,validator: this.validateKey, trigger: 'blur'}
        ],
        value: [
          {required: true,message: '值不能为空', trigger: 'blur'}
        ],
      },
      path: 'sysDict',
      autoClose: true,
      defaultForm: {
        key: '',
        value: '',
        order: '',
        status: 1,
        description: ''
      }
    }
  },
  methods: {
    validateKey(rule, key, cb) {
      if (key) {
        if (this.editFormData.id && this.editFormData.key === key) {
          cb()
        } else {
          this.$post('/sysDict/validate', {key})
              .then(({data}) => {
                if (data.success) {
                  cb()
                } else {
                  cb(new Error(data.message))
                }
              })
              .catch(e => cb(new Error(`校验失败，请稍后重试！\n${e}`)))
        }
      } else {
        cb(new Error('编码不能为空！'))
      }
    }
  },
  mixins: [fromMixin]
}
</script>
