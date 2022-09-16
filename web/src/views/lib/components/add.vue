<template>
  <section>
    <el-button v-if="!this.parentIndex && this.status != '2' " type="primary" @click="showAdd">添加</el-button>
    <el-dialog width="80%" :title="title" :visible.sync="dialogShow" append-to-body>
      <archive-form :form-definition-id="formId" :fond-id="fondId" :category-id="categoryId" :form="form"
                    ref="form"/>
      <div slot="footer" class="dialog-footer">
        <el-button type="info" @click="dialogShow = false" class="btn-cancel">关 闭</el-button>
        <el-button type="primary" @click="save(false)" v-loading="loading1" class="btn-submit">保存并继续</el-button>
        <el-button type="primary" @click="save(true)" v-loading="loading2" class="btn-submit">保存并关闭</el-button>
      </div>
    </el-dialog>

  </section>
</template>
<script>
import abstractComponent from "./abstractComponent";
import archiveForm from "../archiveForm/index";

/**
 * 头部
 * 添加按钮
 */
export default {
  extends: abstractComponent,
  name: "add",
  computed: {
    title() {
      return `${this.form.id ? '编辑档案' : '添加档案'}`
    }
  },
  components: {archiveForm},
  data() {
    return {
      form: {},
      loading1: false,
      loading2: false,
      fondId: '',
      status: '',
      categoryId: '',
      innerVisible: false,
      defaultParams: {}
    }
  },
  methods: {
    $init() {
      this.status = this.register.handoverStatus;
      //准备参数
      this.defaultParams = {
        //这几个参数是必填的
        formDefinitionId: this.formId,
        //全宗Id
        fondId: this.fond.id,
        //分类Id
        categoryId: this.category.id,
        //状态
        status: this.eventBus.defaultForm.status_info
      }

    },
    createDefaultForm() {
      return {
        archivers_category_code: this.category.code,
        transition_state: '0',
        split_state: '0',
        distinguish_state: '0',
        disassembly_state: '0',
        packet_state: '0',
      }
    },
    //显示弹出框
    showAdd() {
      this.categoryId = this.category.id
      this.fondId = this.fond.id
      this.form = this.createDefaultForm()
      this.dialogShow = true
    },
    /**
     * 编辑方法
     */
    edit(row) {
      this.form = Object.assign({}, row)
      this.fondId = this.fond.id
      this.dialogShow = true
    },
    /**
     * 保存方法
     */
    async save(close) {
      const valid = await this.$refs.form.validate()
      if (valid) {
        if (!close) {
          this.loading1 = true
        } else {
          this.loading2 = true
        }

        const url = `/manage/formData/${this.form.id ? "updateFormData" : "insertFormData"}`
        const {data} = await this.$post(url, Object.assign(this.defaultParams, this.form, this.eventBus.defaultForm))
        if (data.success) {
          this.eventBus.$emit("loadData")
          this.$message.success("保存成功！")
          if (close) {
            this.dialogShow = false
          } else {
            this.createDefaultForm()
          }
        } else {
          this.$message.warning(data.message.replace("服务器错误：", ""))
        }
      } else {
        this.$message.error('请填写完整表单')
      }
      if (!close) {
        this.loading1 = false
      } else {
        this.loading2 = false
      }
    },
  },
  mounted() {
    //监听编辑事件
    this.eventBus.$on('edit', this.edit)
  },
  beforeDestroy() {
    this.eventBus.$off('edit', this.edit)
  }
}
</script>
