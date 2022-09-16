<template>
  <section>
    <el-form :model="searchForm" ref="searchForm" inline class="searchForm">
      <el-form-item label="题 名" prop="anJuanTiMing" v-if="archiveType!='2'">
        <el-input v-model="searchForm.anJuanTiMing" prefix-icon="el-icon-search" placeholder="请输入"
                  clearable/>
      </el-form-item>
      <el-form-item label="档 号" prop="dangHao">
        <el-input v-model="searchForm.dangHao" prefix-icon="el-icon-search" placeholder="请输入档号"
                  clearable/>
      </el-form-item>
      <el-form-item label="盒 号" prop="boxNumber" v-if="archiveType=='2'">
        <el-input v-model="searchForm.boxNumber" prefix-icon="el-icon-search" placeholder="请输入盒号"
                  clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchF" size="small">搜 索</el-button>
      </el-form-item>
      <el-form-item>
        <el-dropdown
            placement="bottom"
            trigger="click"
            @command="removeAll">
          <el-button class="search-btn" type="primary">删除<i class="el-icon-arrow-down el-icon--right"/></el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-if="currentSelect.length>0" command="select">删除选中</el-dropdown-item>
            <el-dropdown-item command="all">删除所有</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-form-item>
    </el-form>
  </section>
</template>

<script>
import fromMixin from '@/util/formMixin'

export default {
  name: "form",
  mixins: [fromMixin],
  data() {
    return {
      searchForm: {
        dangHao: '',
        dangAnTiMing: '',
        boxNumber: '',
      },
      currentSelect: []
    }
  },
  props: {
    archiveType: String,
    registerId: String
  },
  methods: {
    /**
     * 查询
     */
    searchF() {
      this.$emit('func', '', this.searchForm.dangHao, this.searchForm.anJuanTiMing, this.boxNumber)
    },
    /**
     * 删除
     */
    async removeAll(command) {
      let isAll = false
      let id = ''
      if (command == 'all') {
        isAll = true
      } else {
        id = this.currentSelect.map(s => s.id).join(',');
      }
      const {data} = await this.$http.post('/archivesLog/removeAll', {
        id: id,
        isAll: isAll,
        registerId: this.registerId
      })
      if (data.success) {
        this.$message.success("操作成功")
        this.$emit('loadData')
      } else {
        this.$message.error(data.message)
      }
    }
  }
}
</script>

<style scoped>

</style>