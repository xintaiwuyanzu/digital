<template>
  <section>
    <nac-info title="扫 描">
      <el-input style="width:300px;margin-right: 5px" placeholder="请输入档号" v-model="archiveCode"
                @keyup.enter.native="loadOne"
                clearable>
        <el-button slot="append" icon="el-icon-search" @click="loadOne"></el-button>
      </el-input>
      <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交</el-button>
      <return-button v-if="id!==''" :id.sync=id :query="this.$route.query._query"
                     :types="this.rtype" @toDetail="loadOne"></return-button>

      <el-button type="primary" v-on:click="onSet" icon="el-icon-printer">扫描仪</el-button>
      <el-button type="primary" v-on:click="setScan" icon="el-icon-setting">设置</el-button>
      <el-button type="primary" v-on:click="scan" @keyup.space.native-type="scan" icon="el-icon-folder-checked"
                 :loading="scanLoad">
        扫描
      </el-button>
      <el-button type="primary" v-on:click="insertScan" icon="el-icon-folder-delete" :loading="scanLoad">插 扫
      </el-button>
      <el-button type="primary" v-on:click="insertScan" icon="el-icon-folder-add" :loading="scanLoad">补 扫
      </el-button>
      <el-button type="primary" v-on:click="toSweep" icon="el-icon-folder" :loading="scanLoad">替 扫</el-button>
      <el-button type="danger" v-on:click="deleteAllImg" icon="el-icon-delete" :loading="scanLoad">删除全部</el-button>
      <el-button type="primary" v-if="id!==''" @click="back" style="text-align: center" icon="el-icon-save">返回</el-button>
    </nac-info>
    <div class="index_main" v-loading="loading" style="background:#F0F8FF"
         element-loading-text="拼命加载中...">
      <el-row style="height: 100%">
        <Split style="height: 100%" :gutterSize="6">
          <SplitArea :size="10">
            <e-vue-contextmenu ref="Menudisplay" id="contextStyle">
              <div>
                <ul>
                  <li class="menu__item" @click="insertScanRight">插扫</li>
                  <li class="menu__item" @click="insertScanRight">补扫</li>
                  <li class="menu__item" @click="toSweepRight">替扫</li>
                  <li class="menu__item" @click="deleteImg">删除</li>
                </ul>
              </div>
            </e-vue-contextmenu>
            <div class="demo-image__lazy" v-for="(data,index) in UploadFiles" :key="index"
                 @click="see(data.filePath,data.id,index) "
                 :class="{isActive:index==isActive}"
                 style="width:100% ; z-index: inherit;position: relative;margin:0 auto ;" align="center">
              <img :key="data.id" :src="data.thumbnailPath+'?temp='+Math.random()"
                   @click="see(data.filePath,data.id,index) "
                   @contextmenu.prevent="!scanLoad?rightClick($event,data.id):''"
                   style="width: 100%"/>
              <el-tag @click.native="see(data.filePath,data.id,index) " type="info">{{ data.srcName }}
              </el-tag>
            </div>

          </SplitArea>
          <SplitArea :size="midImgSize">
            <div align="center" v-if="url1!=''">
              <el-image :src="url1" style="width: 40%; height: 60%" :preview-src-list="[url1]"></el-image>
            </div>
          </SplitArea>
          <SplitArea :size="rightContentSize">
            <el-row class="gallery">
              <div style="margin: 0px 0 0 10px">
                <volumes-detail-disabled :form-definition-id="ajFormId" :fond-id="ajFondId"
                                         :form="volumesForm" ref="volumesForm"/>
              </div>
            </el-row>
          </SplitArea>
        </Split>
        <span class="detailBtn">
                     <el-button type="primary" @click="detail">查<br/>看<br/>详<br/>情</el-button>
                </span>
      </el-row>
    </div>
    <el-dialog :visible.sync="edit" title="扫描仪选择" width="320px" :close-on-click-modal="false">
      <el-form :model="form" ref="form" label-width="70px" style="color: #00397f;margin-top: 10px">
        <el-form-item prop="SMY" label="扫描仪"
                      :rules="[{ required: true, message: '请选择扫描仪', trigger: 'change'}]">
          <el-select v-model="form.SMY" placeholder="请选择扫描仪">
            <el-option
                v-for="item in options"
                :key="item.id"
                :label="item.label"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="edit=false">取 消</el-button>
        <el-button type="primary" @click="confirmScan" v-loading="loading">确 认</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import indexMixin from '@/util/indexMixin'
import splitPane from 'vue-splitpane'
import DeviceClient from '@/util/scanning'
import volumesDetailDisabled from "@/views/lib/components/volumesDetailDisabled";
import returnButton from "@/components/customButton/returnButton";

let client = DeviceClient()
export default {
  name: "index",
  mixins: [indexMixin],

  // splitPane,
  components: {returnButton, volumesDetailDisabled},
  data() {
    return {
      scanLoad: false,
      isActive: '',
      id: this.$route.query.message.id,
      type: this.$route.query.message.type,
      dangHao: this.$route.query.message.dangHao,
      ajDangHao: this.$route.query.message.ajDangHao,
      ajFormId: this.$route.query.message.ajFormId,
      ajFondId: this.$route.query.message.ajFondId,
      message: this.$route.query.message,
      formId: this.$route.query.message.formId,
      code: this.$route.query.message.code,
      rtype: this.$route.query.message.rtype,
      ajRow: this.$route.query.ajRow,
      edit: false,
      leftId: '',
      rightId: '',
      url1: '',
      UploadFiles: {},
      archiveData: [],
      volumesData: [],
      volumesForm: {},
      form: {
        SMY: ''
      },
      options: [],
      page: {
        size: 15,
        index: 0,
        total: 0
      },
      index: this.$route.query.index,
      archiveCode: this.$route.query.message.dangHao,
      flowType: '',
      midImgSize: 89,
      rightContentSize: 0,
      contentSize: true,
      uploadTableLength: 0,
      flag: false,
      findCount: 0,
      obj:{}
    }
  },
  mounted() {
    this.getSMY()
    this.getScan();
  },
  created() {
    this.flowPath()
  },
  methods: {
    //查找能链接的扫描仪
    getSMY() {
      client.getScanner().then(d => {
        d.data.forEach(v => {
          let one = {}
          one.id = v.id
          one.label = v.lable
          this.options.push(one)
        })
        //现在是默认选第一个
        if (this.options.length < 1) {
          this.edit = true
        } else {
          this.edit = false
          this.form.SMY = this.options[0].id
        }
      }).catch(e => {
        alert(e + '连接失败！')
      })
        if (this.form.SMY == null || this.form.SMY == "") {
            this.$message.error("请选择扫描仪!")
            this.edit = true;
            return
        }
    },
    //获取扫描地址
    getScan() {
      this.$http.post('ocr/getScan').then(({data}) => {
        if (data.success) {
          this.scanurl = data.data
        } else {
          this.$message.error(data.message)
        }
        this.loading = false;
      })
    },
    //选择扫描仪
    onSet() {
      this.edit = true
    },
    //确认使用的扫描仪
    confirmScan() {
      if (this.$refs.form) {
        this.loading = true
        this.$refs.form.validate(valid => {
          if (valid) {
            this.edit = false
            this.loading = false
          } else {
            this.loading = false
          }
        })
      }
    },
    //扫描仪设置
    setScan() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.edit = true;
      } else {
        client.openUi(this.form.SMY).then(data => {
        });
      }
    },
    back(){
      this.$router.push({path:"/scanning"})
    },
    $init() {
      this.objInit()
      this.volumesForm = Object.assign({}, this.ajRow)
      this.findImg(this.formId, this.id, this.type, this.url1)
      this.scannerEnd()

      this.leftId = ''
      this.rightId = ''
    },
    objInit(){
      if (this.$route.query.message&&this.$route.query.message.ajFormId){
        this.obj.formDefinitionId = this.$route.query.message.ajFormId
      }else {
        this.obj.formDefinitionId = ''
      }
      this.obj.archivesId = this.id
      //状态 0，无标注 1，有标注
      this.obj.wssplitTaggingCondition = '1'
    },
    findImg(formId, id, type, url1) {
      this.loading = true
      this.$http.post('uploadfiles/findImgByIdAndType', {
        formDefinitionId: formId,
        id: id,
        type: type
      }).then(({data}) => {
        if (data.success) {
          if (data.data != null) {
            this.UploadFiles = data.data;
          }
          this.url1 = url1
        } else {
          this.$message.error(data.message)
        }
        this.loading = false;
      })
    },
    //点击档号刷新
    elTag() {
      this.url1 = ''
      this.findImg(this.formId, this.id, this.type, this.url1)
    },
    //点击左侧缩略图 查看大图
    see(url, id, index) {
      this.url1 = url + "?temp=" + Math.random()
      this.leftId = id
      this.isActive = index
    },
    //点击左侧缩略图 右键触发菜单
    rightClick(event, id) {
      this.rightId = id
      this.$refs.Menudisplay.showMenu(event)
    },
    //上一件，下一件
    async loadOne() {
      const query = this.$route.query._query
      const {data} = await this.$post('/manage/formData/formDataPage',
          Object.assign(
              //默认参数
              query,
              //分页参数
              {size: 1, index: 1, 'archival_code': this.archiveCode}),
          {timeout: 20000})
      if (data.success) {
        if (data.data.data.length > 0) {
          this.archiveData = data.data.data
            this.judge(this.archiveData[0])
          this.lurArchive(this.archiveData[0])
        } else {
          this.archiveCode = ''
          this.$message.error("查无此份档案")
        }
      } else {
        this.$message.error(data.message)
      }
    },
      //添加操作人
      async judge(archiveData){
          let obj = new Object();
          obj.archivesId = archiveData.id
          obj.formDefinitionId = this.$route.query.message.ajFormId
          await this.$http.post("/manage/formData/uniquenessJudge", obj).then(({data}) => {
              if (data.data.success) {
              } else {
                  this.$message.error(data.data.message);
              }
          })
      },
    //切换档案执行方法
    lurArchive(row) {
      this.id = row.id
      this.dangHao = row.archival_code
      if (this.form.SMY == '') {
        this.edit = true
      }
      this.url1 = '';
      this.findImg(this.formId, this.id, "1")
    },
    scannerImg(formId, id, SMY, imgId, url) {
      client.scanner(
          this.getConfig(),
          formId + "," + id, SMY, imgId, url);
    },
    //扫描
    async scan() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.type == '1') {
          this.scannerImg(this.formId, this.id, this.form.SMY, "0", this.scanurl + 'api/uploadfiles/addFaceImg');
        } else {
          this.scannerImg(this.formId, this.id, this.form.SMY, "0", this.scanurl + 'api/uploadfiles/addJnFaceImg');
        }
    },
    //插扫
    insertScan() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.leftId == '' || this.leftId == null) {
        this.$message.warning("请选择图片信息!")
        return
      }
      if (this.type == '1') {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.leftId, this.scanurl + 'api/uploadfiles/forPlug');
      } else {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.leftId, this.scanurl + 'api/uploadfiles/forJnPlug');
      }
      // this.leftId = '';
    },
    //补扫
    patchScan() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.leftId == '' || this.leftId == null) {
        this.$message.warning("请选择图片信息!")
        return
      }
      if (this.type == '1') {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.leftId, this.scanurl + 'api/uploadfiles/forSweeping');
      } else {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.leftId, this.scanurl + 'api/uploadfiles/forJnSweeping');
      }
      // this.leftId = '';
    },
    //替扫
    toSweep() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.leftId == '' || this.leftId == null) {
        this.$message.warning("请选择图片信息!")
        return
      }
      if (this.type == '1') {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.leftId, this.scanurl + 'api/uploadfiles/forSaul');
      } else {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.leftId, this.scanurl + 'api/uploadfiles/forJnSaul');
      }
      // this.leftId = '';
    },
// //旧扫描：延迟加载 返回数据，等待 扫描完成
//     getConfig() {
//      this.scanLoad = true
//       // let timeout = setTimeout(() => {
//       //     //设置延迟执行
//       //     this.afterScanner()
//       // }, 8000);
//     },
    //新扫描，1秒查数据库一次，发现增加刷新数据
    getConfig() {
      this.scanLoad = true
      this.flag = false
      let scannerEnd = setInterval(() => {
        this.scannerEnd()
        if (this.flag) {
          this.flag = false
          clearInterval(scannerEnd);
          this.afterScanner()
          this.findCount = 0
        } else if (this.findCount > 10) {
          clearInterval(scannerEnd);
          this.$message.error("扫描超时")
          this.scanLoad = false
          this.findCount = 0
        }
        this.findCount++
        //设置1秒执行一次
      }, 1000);

    },
    //查询数据库是否新增扫描图像
    scannerEnd() {
      this.$http.post('uploadfiles/findImgCountById', {
        id: this.id
      }).then(({data}) => {
        if (data && data.success) {
          this.uploadTableLength = data.data

        } else {
          this.$message.error(data.message)
        }
      })
    },
    //查询当前扫描的这张图片展示大图显示
    afterScanner() {
      this.$http.post('uploadfiles/findImgById', {
        id: this.id
      }).then(({data}) => {
        if (data && data.success) {
          console.log(data.data.length > 0,data.data[data.data.length - 1])
          if (data.data.length > 0 && data.data[data.data.length - 1].filePath != null && data.data[data.data.length - 1].filePath != undefined) {
            this.url1 = data.data[data.data.length - 1].filePath + "?temp=" + Math.random();
            this.findImg(this.formId, this.id, this.type, this.url1)
          } else {
            this.$message.error("图片路径找不到")
            this.findImg(this.formId, this.id, this.type, this.url1)
          }
        } else {
          this.$message.error(data.message)
        }
        this.loading = false;
        this.scanLoad = false
      })
    },
    //删除全部
    deleteAllImg() {
      this.$confirm('确定删除全部？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post(`/uploadfiles/deleteAllByBussinessId`, {id: this.id}).then(({data}) => {
          if (data.success) {
            this.$message.success("删除成功!");
            this.findImg(this.formId, this.id, this.type)
          } else {
            this.$message.error(data.message)
          }
        })
      })
    },
    //删除单张图片
    deleteImg() {
      this.$confirm('确定删除？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post(`/uploadfiles/deleteByFile`, {id: this.rightId}).then(({data}) => {
          if (data.success) {
            this.$refs.Menudisplay.hideMenu()
            this.findImg(this.formId, this.id, this.type)
            this.$message.success("删除成功!");
          } else {
            this.$message.error(data.message)
          }
        })
      })
    },
    //提交
    async submit() {
      this.$post('/register/updateStatus', Object.assign(this.$route.query._query, {
        type: this.flowType,
        id: this.id,
      })).then(({data}) => {
        if (data.success) {
          this.id = ''
          this.$message.success('提交成功，请在操作页查看结果！')
        } else {
          this.$message.error(data.message)
        }
      })
    },

    /**选择功能**/
    //TODO 插扫
    insertScanRight() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.rightId == '' || this.rightId == null) {
        this.$message.warning("请选择左侧图片信息!")
        return
      }
      if (this.type == '1') {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.rightId, this.scanurl + 'api/uploadfiles/forPlug');
      } else {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.rightId, this.scanurl + 'api/uploadfiles/forJnPlug');
      }
      // this.rightId = '';
    },
    //todo 补扫
    patchScanRight() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.rightId == '' || this.rightId == null) {
        this.$message.warning("请选择左侧图片信息!")
        return
      }
      if (this.type == '1') {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.rightId, this.scanurl + 'api/uploadfiles/forSweeping');
      } else {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.rightId, this.scanurl + 'api/uploadfiles/forJnSweeping');
      }
      // this.rightId = '';
    },
    //todo 替扫batch
    toSweepRight() {
      if (this.form.SMY == null || this.form.SMY == "") {
        this.$message.error("请选择扫描仪!")
        this.edit = true;
        return
      }
      if (this.rightId == '' || this.rightId == null) {
        this.$message.warning("请选择左侧图片信息!")
        return
      }
      if (this.type == '1') {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.rightId, this.scanurl + 'api/uploadfiles/forSaul');
      } else {
        this.scannerImg(this.formId, this.id, this.form.SMY, this.rightId, this.scanurl + 'api/uploadfiles/forJnSaul');
      }
      // this.rightId = '';
    },
    //查看详情默认开启
    detail() {
      this.contentSize = !this.contentSize
      if (this.contentSize) {
        this.rightContentSize = 0
        this.midImgSize = 89
      } else {
        this.rightContentSize = 30
        this.midImgSize = 60
      }
    },
    flowPath() {
      this.$http.post('/fonddata/flowPath', {
        fid: this.formId,
        type: this.rtype,
        state: 1
      }).then(({data}) => {
        if (data && data.success) {
          //type给提交，用于提交，
          this.flowType = data.data.flowBatchName
        }
      })
      //退回功能，将当前type给后台，后台吧前面的都返回回来。
    }
  },
  watch: {
    uploadTableLength(newValue,old) {
      if (old<newValue&&newValue > 0) {
        this.flag = true
      }
    }
  }
}
</script>
<style scoped>
.active {
  background-color: #77a9fd;
  color: white;
}

.isActive {
  background-color: #77a9fd;
  color: white;
}

.detailBtn {
  height: 50%;
  display: block;
  bottom: 3px;
  right: 3px;
  position: fixed
}
</style>
