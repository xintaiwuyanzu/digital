<template>
  <el-form-item label="请选择系统">
    <el-select v-model="sysId" ref="sysSelect" @change="select">
      <el-option v-for="item in options" :key="item.id" :value="item.id" :label="item.sysName">
        <span style="float: left">{{ item.sysName }}</span>
        <span style="float: right; color: #8492a6; font-size: 13px">{{ item.sysDescription }}</span>
      </el-option>
    </el-select>
    <el-dialog :visible.sync="edit" :title="(form.id?'编辑':'添加')+'子系统'" width="80%">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px" inline>
        <el-form-item label="系统名称" prop="sysName" required>
          <el-input v-model="form.sysName"></el-input>
        </el-form-item>
        <el-form-item label="系统简称" prop="shortName">
          <el-input v-model="form.shortName"></el-input>
        </el-form-item>
        <el-form-item label="IP地址" prop="ip" required>
          <el-input v-model="form.ip"></el-input>
        </el-form-item>
        <br/>
        <el-form-item label="访问端口" prop="port">
          <el-input v-model="form.port"></el-input>
        </el-form-item>
        <el-form-item label="系统首页" prop="homeAddress">
          <el-input v-model="form.homeAddress"></el-input>
        </el-form-item>
        <el-form-item label="是否可用" prop="status">
          <el-select v-model="form.status" clearable placeholder="请选择是否可用">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label"
                       :value="item.value"/>
          </el-select>
        </el-form-item>
        <br/>
        <el-form-item label="排序" prop="order">
          <el-input v-model="form.order"></el-input>
        </el-form-item>
        <el-form-item label="系统描述" prop="sysDescription">
          <el-input type="textarea" v-model="form.sysDescription"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="saveForm" v-loading="loading">保 存</el-button>
      </div>
    </el-dialog>
  </el-form-item>
</template>
<script>
import fromMixin from '@dr/auto/lib/util/formMixin'

export default {
  mixins: [fromMixin],
  model: {prop: 'value', event: 'select'},
  data() {
    return {
      sysId: 'default',
      autoClose: true,
      options: [],
      path: '/subsys'
    }
  },
  methods: {
    select(v) {
      if (v) {
        this.$emit('select', v)
      }
    },
    search() {
      this.loadSyss()
    },
    loadSyss() {
      this.$http.post('/subsys/page?page=false')
          .then(({data}) => {
            if (data.success) {
              this.options = data.data
            }
          })
    }
  },
  mounted() {
    if (this.id) {
      this.sysId = this.id
    } else {
      this.$emit('select', this.sysId)
    }
    this.loadSyss()
  }
}
</script>
