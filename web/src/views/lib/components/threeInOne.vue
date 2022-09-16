<template>
    <span>
        <el-button type="primary" size="mini" @click="hooktDialog=true"> 数据清洗</el-button>
        <el-dialog :visible.sync="hooktDialog" title="匹配原文" width="80%">
        <el-form :model="form" ref="form" label-width="150px">
          <el-form-item label="来源:" prop="fileLocations" required>
            <el-radio-group v-model="form.fileLocations">
              <el-radio label="CLIENT">客户端上传</el-radio>
              <el-radio label="SERVER">服务器</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="上传批次记录:" prop="clientBatchId" v-if="form.fileLocations==='CLIENT'" required>
            <select-async v-model="form.clientBatchId" placeholder="选择客户端上传批次记录" style="width: 60%" clearable
                          url="/dataBatch/page" :params="dataBatchParams" labelKey="batchName"/>
          </el-form-item>
          <el-form-item label="原文位置:" prop="filePath" v-else-if="form.fileLocations==='SERVER'" required>
            <el-autocomplete v-model="form.filePath" clearable placeholder="请输入原文位置" style="width: 60%"
                             :fetch-suggestions="querySearch"/>
          </el-form-item>
<!--          <el-form-item label="是否删除上传文件" prop="isDeleteFile">
            <el-tooltip :content="'' + form.isDeleteFile" placement="top">
              <el-switch
                  v-model="form.isDeleteFile"
                  active-color="#13ce66"
                  inactive-color="#ff4949">
              </el-switch>
            </el-tooltip>
          </el-form-item>-->
          <el-form-item label="挂接方式:" prop="coverOrAdd" required>
            <select-async v-model="form.coverOrAdd" placeholder="请选择挂接方式" style="width: 60%" :mapper="hookOptions"
                          valueKey="value"/>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="cancel()">取消</el-button>
          <el-button type="primary" @click="matchText()">开始挂接</el-button>
        </div>
      </el-dialog>
    </span>
</template>

<script>
import abstractComponent from "./abstractComponent";

export default {
  extends: abstractComponent,
  name: "threeInOne",
  data() {
    return {
      hooktDialog: false,
      loading: false,
      form: {
        fileLocations: 'CLIENT',
        clientBatchId: '',
        filePath: '',
        isDeleteFile: false,
        coverOrAdd: '',
      },
      dataBatchParams: {page: false, batchType: 0},
      hookOptions: [{
        value: 'COVER',
        label: '清除现有原文并挂接'
      }, {
        value: 'ADD',
        label: '追加'
      }],
      formDefinitionId:'',
    }
  },

  created() {
    const _query = this.eventBus.getQueryByQueryType('query')
    this.formDefinitionId = _query.formDefinitionId
  },
  methods: {
    hook() {
      const _query = this.eventBus.getQueryByQueryType('query')
      this.$confirm('此操作将生成对应tif文件的jpg文件,' +
          '对应jpg的txt文本和对应档案的文件结构。,是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const data = this.$post('threeInOne/dataCleaning',
            Object.assign(
                _query,
                {registerId: this.fond.registerId}),
            {timeout: 20000})
        data.then(data => {
          if (data.data.success) {
            this.eventBus.$emit("loadData")
            this.$message.success('原文正在拆分中，请稍后查看！')
          } else {
            this.$message.success(data.data.message)
          }
        })
      }).catch(() => {
      });
    },
    querySearch(queryString, cb) {
      cb(JSON.parse(localStorage.getItem('filePath')))
    },
    cancel() {
      this.hooktDialog = false;
      this.$refs.form.resetFields();
    },
    test(){
      this.$refs.form.validate((valid) => {
        if (valid) {
          alert('submit!');
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    matchText(){
      this.$refs.form.validate((valid) => {
        if (valid) {
          const data =  this.$post('/jpgQueue/getMatchText', {
                fileLocations:this.form.fileLocations,
                clientBatchId:this.form.clientBatchId,
                filePath:this.form.filePath,
                formDefinitionId:this.formDefinitionId,
                /*isDeleteFile:this.form.isDeleteFile,*/
          })
          data.then(data => {
            if(data.data.success){
              this.$message({
                showClose: true,
                message: data.data.data,
                type: 'success'
              });
              this.hooktDialog = false;
              this.$refs.form.resetFields();
            }else {
              this.$message({
                showClose: true,
                message: data.data.message,
                type: 'error'
              });
            }
          })
        } else {
          this.$message({
            showClose: true,
            message: "请选择挂接原文",
            type: 'error'
          });
        }
      });
      }
    /*init() {
        const _query = this.eventBus.getQueryByQueryType('query')
        const data = this.$post('threeInOne/dataCleaning',
            Object.assign(
                _query,
                {registerId: this.fond.registerId}),
            {timeout: 20000})
        if (data.data.success) {
            this.$message.success('原文正在拆分中，请稍后查看！')
        } else {
            this.$message.success(data.data.message)
        }
    }*/
  },
  //暂时先写个定时 启动的怕 ocr崩溃
  /*mounted() {
      this.timer = setInterval(this.init, 300000);
  },*/
}
</script>
