<template>
  <section>
    <nac-info title="模板管理">
      <el-button type="primary" @click="add">添加字段
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
          <el-table-column prop="field" label="字段名" min-width="100">
            <template slot-scope="scope">
              <el-button type="text" @click="edit(scope.row)">
                {{ scope.row.field }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="fieldval" label="字段值" show-overflow-tooltip >
            <template slot-scope="scope">
                {{ scope.row.fieldval }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip/>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="edit(scope.row)">编 辑</el-button>
              <el-button type="text" @click="remove(scope.row.id)">删 除</el-button>
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
    <el-dialog
        :title="schemeTitle"
        width="30%"
        :close-on-click-modal="false"
        :destroy-on-close=true
        :visible.sync="dialog">
      <el-form :model="scheme" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="字段名" prop="field" required>
          <el-input v-model="scheme.field" placeholder="请输入方案名称"
                    required clearable></el-input>
        </el-form-item>
        <el-form-item label="字段值" prop="fieldval" required>
          <el-input v-model="scheme.fieldval"  @input="upper" placeholder="请输入方案编码"
                    required clearable></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="scheme.remark" placeholder="请输入方案名称"
                     clearable></el-input>
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="info" @click="dialog = false" class="btn-cancel">取 消</el-button>
        <el-button type="primary" @click="saveScheme" v-loading="loading"
                   class="btn-submit">保 存
        </el-button>
      </div>
    </el-dialog>
  </section>
</template>
<script>
import indexMixin from '@dr/auto/lib/util/indexMixin'

export default {
  data() {
    return {
      schemeTitle: "",
      path: 'template',
      dialog: false,
      rolePersons: [],
      selectRoleId: '',
      persons: [],
      scheme: {},
      rules: {
        fieldval: [
          {required: true, message: '请输入字段值', trigger: 'change'},
          {required: true, message: '请输入字段值', trigger: 'blur'}
        ],
        field: [
          {required: true, message: '请输入字段名', trigger: 'change'},
          {required: true, message: '请输入字段名', trigger: 'blur'}
        ],
      }
    }
  },

  filters: {
    upper: function (value) {
      if (!value) return '';
      value = value.toString();
      return value.toUpperCase();
    }
  },
  methods: {
    $init() {
      this.loadData()
    },
    upper(){
      this.scheme.fieldval = this.scheme.fieldval.toUpperCase()
    },
    loadData (params, useSearchForm) {
      // this.loading = true
      if (useSearchForm && this.$refs.form && this.$refs.form.getSearchForm) {
        params = this.$refs.form.getSearchForm(params)
      }
      this.$http.post('/template/page', params).then(({data}) => {
        if (data && data.success) {
          this.data = data.data.data
          this.page.index = data.data.start / data.data.size + 1
          this.page.size = data.data.size
          this.page.total = data.data.total
          if(this.$refs.form){
            this.$refs.form.searchForm = Object.assign(this.$refs.form.searchForm,{pageIndex:this.page.index-1})
          }
        }
        this.loading = false
      })
    },
    add() {
      this.schemeTitle = '添加';
      this.optype = 'add';
      this.dialog = true;
      this.scheme = {}
    },
    edit(row) {
      this.schemeTitle = '编辑';
      this.optype = 'edit';
      this.scheme = Object.assign({}, row);
      this.dialog = true
    },
    saveScheme() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          let path = '';
          if (this.optype === 'add') {
            path = '/template/insert'
          } else if (this.optype === 'edit') {
            path = '/template/update'
          }
          this.loading = true;

          this.$http.post(path, this.scheme)
              .then(({data}) => {
                if (data.success) {
                  this.$message.success("保存成功！");
                  this.loadData(this.page.index);
                  this.dialog = false
                } else {
                  this.$message.error(data.message)
                }
                this.loading = false
              })
        }
      })
    },
    remove(id) {
      this.$confirm("确认删除？", '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }).then(() => {
        this.$http.post('/template/delete', {id:id})
            .then(({data}) => {
              if (data.success) {
                this.$message.success("删除成功");
                this.loadData()
              } else {
                this.$message.error(data.message)
              }
              this.loading = false
            })
      })
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
      await this.$post('/sysrole/bindRoleUser', {id: this.selectRoleId, personIds: this.rolePersons.join(',')})
      this.dialogShow = false
      this.$message.success('绑定成功！')
    }
  },
  mixins: [indexMixin]
}
</script>
