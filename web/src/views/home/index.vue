<template>
  <section>
    <nac-info title="批次管理">
      <el-form :model="searchForm" ref="searchForm" inline>
        <el-form-item label="批次名称 :" prop="batch_name">
          <el-input v-model="searchForm.batch_name" placeholder="请输入批次名称" clearable/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData(searchForm)" size="mini">搜索</el-button>
          <el-button type="primary" size="mini" @click="showdialog">添加</el-button>
          <el-button type="danger" size="mini" @click="remove">删 除</el-button>
        </el-form-item>
      </el-form>
    </nac-info>
    <div class="index_main" v-loading="loading">
      <el-table border height="100%" class="table-container" ref="multipleTable" :data="data"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"/>
        <column-index :page="page"/>
        <el-table-column
            prop="batch_no"
            label="批次号"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="batch_name"
            label="批次名称"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="form_scheme"
            label="档案门类"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="archivers_category_code"
            label="门类代码"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column
            prop="receiver"
            label="创建人"
            align="center"
            header-align="center"
            sortable>
        </el-table-column>
        <el-table-column label="操作" align="center" width="400">
          <template slot-scope="scope">
            <el-button type="text" @click="configTest(scope.row)">导入方案</el-button>
            <!--                        <el-button type="text" v-if="scope.row.original_format !=3&&scope.row.original_format !='纸质'"-->
            <!--                                   @click="ads(scope.row)">jpg拆分-->
            <!--                        </el-button>-->
            <!--&lt;!&ndash;                      v-if="scope.row.original_format !=3"&ndash;&gt;-->
            <!--                        <el-button type="text" @click="txt(scope.row)">ocr识别</el-button>-->
            <el-button type="text" v-if="scope.row.archivers_category_code.indexOf('WS') != -1 "
                       @click="loadDaFenLei(scope.row)">拆件
            </el-button>
            <el-button type="text" @click="doSend(scope.row)">ofd转换</el-button>
            <el-button type="text" @click="doExcel(scope.row)">txt转Excel</el-button>
            <el-button type="text" @click="detailStatics(scope.row)">查 看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          @current-change="index=>loadData({pageIndex:index-1},this.searchForm)"
          :current-page.sync="page.index"
          :page-size="page.size"
          layout="total, prev, pager, next,jumper"
          :total="page.total">
      </el-pagination>
    </div>
    <el-dialog :title="title" :visible.sync="addDialogVisible" width="80%" inline="true"
               :close-on-click-modal="false" :before-close="cancel">
      <el-form :model="addForm" label-width="100px" ref="addForm" abel-width="160px" :rules="rules" v-loading="loading">
        <el-row>
          <el-col :span="8">
            <div>
              <el-form-item label="批次号：" prop="batch_no">
                <el-input v-model="addForm.batch_no" disabled
                          placeholder="请输入批次号"></el-input>
              </el-form-item>
              <el-form-item label="批次名：" prop="batch_name">
                <el-input v-model="addForm.batch_name" :disabled="inputDisabled"
                          placeholder="请输入批次名"></el-input>
              </el-form-item>
              <el-form-item label="原文格式:" prop="original_format">
                <el-select v-model="addForm.original_format" placeholder="请选择" style="width: 100%">
                  <el-option
                      v-for="item in original_format_data"
                      :label="item.label"
                      :key="item.value"
                      :value="item.value">
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
          </el-col>
          <el-col :span="8">
            <div>
              <el-form-item label="元数据方案" prop="yuanSJ">
                <el-select
                    :disabled="inputDisabled"
                    v-model="addForm.yuanSJ"
                    style="width: 100%"
                    @change="selectMetadata(addForm.yuanSJ)"
                    value-key="id"
                    placeholder="请选择">
                  <el-option
                      v-for="item in metadataList"
                      :key="item.id"
                      :label="item.metadataName"
                      :value="item">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="执行标准:" prop="yearId">
                <el-input v-model="addForm.codeFormData" disabled placeholder="请选择元数据方案"></el-input>
              </el-form-item>
              <el-form-item label="目标格式:" prop="target_format">
                <el-select v-model="addForm.target_format" placeholder="请选择" style="width: 100%">
                  <el-option
                      v-for="item in target_format_data"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value">
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
          </el-col>
          <el-col :span="8">
            <div>
              <el-form-item label="门类:">
                <el-input v-model="addForm.archivers_category_code_name" disabled
                          placeholder="请选择元数据方案"></el-input>
              </el-form-item>
              <el-form-item label="整理方式:">
                <el-input v-model="addForm.arrangeFormData" disabled placeholder="请选择元数据方案"></el-input>
              </el-form-item>
              <el-form-item label="创建人" prop="userName">
                <el-input v-model="addForm.userName" placeholder="请输入创建人"></el-input>
              </el-form-item>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div>
              <el-form-item label="设置加工环节">
                <el-transfer :titles="['所有环节','选中环节']" v-model="value" :data="segmentData">
                </el-transfer>
              </el-form-item>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
              <el-button type="primary" @click="cancel">取 消</el-button>
              <el-button type="success" @click="saveDetermine" v-if="this.vIfd"
              >保 存</el-button>
        <!-- <el-button type="success" @click="save" v-else v-loading="loading">下一步</el-button>-->
            </span>
    </el-dialog>
  </section>
</template>
<script>
import indexMixin from '@dr/auto/lib/util/indexMixin'

export default {
  mixins: [indexMixin],
  name: "index",
  data() {
    return {
      page: {index: 0, size: 15},
      title: '新增批次',
      titlePerson: '选择人员',
      titlePermission: '角色授权',
      searchForm: {},
      addForm: {
        yuanSJ: '',
        arrange: '',
        archivers_category_code_name: '',
        arrangeFormData: '',
        code: '',
        codeFormData: '',
        userName: '',
        original_format:'',
        target_format: '',
      },
      rules: {
        batch_name: [
          {required: true, message: '批次名不能为空！', trigger: 'blur'}
        ],
        fonds_identifier: [
          {required: true, message: '全宗不能为空！', trigger: 'blur'}
        ],
        yuanSJ: [
          {required: true, message: '元数据不能为空！', trigger: 'blur'}
        ],
        segment: [
          {required: true, message: '流程不能为空！', trigger: 'blur'}
        ],
        original_format: [
          {required: true, message: '原文格式不能为空！', trigger: 'blur'}
        ],
        target_format: [
          {required: true, message: '目标格式不能为空！', trigger: 'blur'}
        ],
        userName: [
          {required: true, message: '创建人不能为空！', trigger: 'blur'}
        ],
      },
      opType: 'add',
      multipleSelection: [],
      addDialogVisible: false,
      fondData: [],
      cateGoryData: [],
      CategoryBspDict: [],
      options: [{
        value: '1',
        label: '案件'
      }, {
        value: '2',
        label: '案卷'
      }],
      fond: {},
      name: '',
      code: '',
      personData: [],
      valuePerson: [],
      permissionData: [],
      valuePermission: [],
      personIds: '',
      roleIds: [],
      rolePersons: [],
      selectPersonId: '',
      persons: [],
      permission: '',
      personName: '',
      dialogShow: false,
      personDialogVisible: false,
      permissionDialogVisible: false,
      tableData: {},
      registerDetail: {},
      labelPosition: 'right',
      optionsMachining: [],
      //元数据
      metadataList: [],
      vIfd: false,
      inputDisabled: false,
      categoryId: "",
      segmentData: [
        {key: 0, label: "任务登记", value: 'RECEIVE', disabled: true},
        {key: 1, label: "原文拆jpg", value: 'YUANWENTOJPG', disabled: true},
        {key: 4, label: "图像扫描", value: 'SCANNING', disabled: true},
        {key: 7, label: "ocr识别", value: 'OCR'},
        {key: 10, label: "自动拆件", value: 'CHAIJIAN', disabled: true},
        {key: 13, label: "图像处理", value: 'PROCESSING'},
        {key: 16, label: "图像质检", value: 'IMAGES'},
        {key: 19, label: "手动拆件", value: 'WSSPLIT'},
        {key: 21, label: "档案著录", value: 'VOLUMES'},
        // {key: 6, label: "初检", value: 'QUALITY'},
        // {key: 7, label: "复检", value: 'RECHECK'},
        {key: 24, label: "ofd转换", value: 'OFD',disabled: true},
        {key: 27, label: "数字化成果", value: 'OVER', disabled: true},
        {key: 30, label: "成果检验", value: 'OVERCHECK'},
        {key: 33, label: "打包入库", value: 'ZIPPACKET'},

      ],
      original_format_data: [{
        value: '1',
        label: 'PDF'
      }, {
        value: '2',
        label: 'TIF'
      },
        {
          value: '3',
          label: '纸质'
        }],
      target_format_data: [{
        value: '2',
        label: 'OFD'
      }],
      value: [0,27,33],
      userName: '',
      detailConfig: false,
      formDefinition: {},
      fieldData: {},
      itemData: [],
    }
  },
  mounted() {
    this.loadData()
  },
  created() {
    this.user()
  },
  methods: {
    $init() {
      this.getMetadata()
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    //根据元数据对案卷，方案，门类选择。
    selectMetadata(item) {
      //门类
      /*this.selectGetCategoryName(item.code)*/
      this.addForm.archivers_category_code_name = item.archivers_category_code_name


      //方案
      /*this.selectScheme(item.standard)*/
      this.addForm.codeFormData = item.codeFormData
      //案卷
      this.addForm.arrangeFormData = item.arrangeFormData
    },
    //获取元数据 2.1
    async getMetadata() {
      const {data} = await this.$http.post('/fonddata/getArchiveTypeSchema')
      this.metadataList = data.data
    },
    //添加功能确定按钮。 ---去除多余步骤的确定方法--最终提交功能()
    saveDetermine() {
      this.$refs.addForm.validate(async valid => {
        if (valid) {
          let obj1 = this.data.find(item => item.batch_no === this.addForm.batch_no)
          if (obj1 && this.opType === 'add') {
            this.$message.error('批次号不能重复！')
            return
          }
          this.loading = true
          const valid = await this.$refs.addForm.validate()
          if (valid) {
            this.vIfd = false
            //编号放进去
            this.addForm.yuanSJ.batch_name = this.addForm.batch_name
            this.addForm.yuanSJ.batch_no = this.addForm.batch_no
            //存放编码
            this.addForm.yuanSJ.archivers_category_code = this.addForm.yuanSJ.code
            this.addForm.yuanSJ.system_code = 'INSPUR-DZZW-MACHINING'
            this.addForm.yuanSJ.region_code = "350100";
            //存放原文格式，目标格式
            this.addForm.yuanSJ.original_format = this.addForm.original_format
            this.addForm.yuanSJ.target_format = this.addForm.target_format
            //排序
            let list = []
            //用于显示中文
            let listName = []
            for (let i = 0; i < this.value.length; i++) {
              for (let j = 0; j < i; j++) {
                if (this.value[i] < this.value[j]) {
                  let m = this.value[i];
                  this.value[i] = this.value[j];
                  this.value[j] = m;
                }
              }
            }
            //编号放进去
            this.value.forEach((item) => {
              this.segmentData.forEach((value => {
                if (item == value.key) {
                  list.push(value.value)
                  listName.push(value.label)
                }
              }))
            })
            try {
              this.$http.post('/fonddata/insertRegister', Object.assign(this.addForm.yuanSJ,
                  {
                    segment: list.join(),
                    segmentName: listName.join()
                  }), {timeout: 30000}).then(({data}) => {
                if (data && data.success) {
                  this.addDialogVisible = false
                  this.value = [0,27,33]
                  this.$refs.addForm.resetFields();
                  this.loadData()
                  this.$message.success('操作成功！')
                } else {
                  this.loading = false
                  this.addDialogVisible = false
                  this.value = [0,27,33]
                  this.$refs.addForm.resetFields();
                  this.$message.error(data.message)
                }
              })
            } catch (e) {
            }
          }
        } else {
          return false;
        }
      });
    },
    //取消
    cancel() {
      this.addDialogVisible = false;
      this.value = [0,27,33]
      this.$refs.addForm.resetFields();
    },
    //添加
    showdialog() {
      let date = this.$moment(new Date()).format('YYYYMMDDHHmm')
      this.addForm = {
        batch_no: date + "A" + Math.floor(Math.random() * 10000),
        userName: this.userName
      }
      this.opType = 'add'
      this.title = '新增批次'
      this.vIfd = true
      this.inputDisabled = false
      this.addDialogVisible = true
    },
    //搜索
    loadData(params) {
      this.loading = true
      this.$http.post('/register/page', params).then(({data}) => {
        if (data.success) {
          this.data = data.data.data
          this.page.index = data.data.start / data.data.size + 1
          this.page.size = data.data.size
          this.page.total = data.data.total
          this.valuePerson = []
          this.valuePerson = this.personData
          this.personData = []
          this.addForm.personId = []
          this.getPerson()
          this.getPermission()
        } else {
          this.$message.error(data.message)
        }
        this.loading = false
      })
    },
    getPerson() {
      this.personData = []
      this.$http.post('/person/page', {
        page: false
      }).then(({data}) => {
        if (data && data.success) {
          let admin = data.data.findIndex(item => {
            if (item.id == 'admin') {
              return true
            }
          })
          data.data.splice(admin, 1)
          data.data.forEach((item, i) => {
            this.personData.push({
              key: item.id,
              label: item.userName
            })
          })
        } else {
          this.personData = []
        }
      })
    },
    getPermission() {
      this.permissionData = []
      this.$http.post('/sysrole/page', {
        page: false
      }).then(({data}) => {
        if (data && data.success) {
          data.data.forEach((item) => {
            this.permissionData.push({
              key: item.id,
              label: item.name
            })
          })
        } else {
          this.permissionData = []
        }
      })
    },
    //新查看
    detailStatics(row) {
      this.$router.push({
        path: '../statistics/check',
        query: {row: row}
      })
    },
    remove() {
      if (this.multipleSelection.length === 0) {
        this.$message.error("请至少选择一条数据")
        return
      }
      let ids = ''
      for (let i = 0; i < this.multipleSelection.length; i++) {
        ids = ids + this.multipleSelection[i].id + ","
      }
      const param = Object.assign({}, {ids: ids})
      this.$confirm("确认删除？", '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }).then(() => {
        this.$http.post('/register/deleteByids', param, {timeout: 20000}).then(({data}) => {
          if (data.success) {
            this.$message({
              message: '操作成功！',
              type: 'success'
            });
          } else {
            this.$message.error(data.message)
          }
          this.loadData()
        })
      }).catch(() => {
      });
    },
    //jpg拆分
    async ads(row) {
      const data = this.$post('uploadfiles/batchTiffToJpgByPath', {
        formDefinitionId: row.formDefinitionId,
        registerId: row.id,
        batchName: row.batch_name,
        batchNo: row.batch_no
      })
      data.then(data => {
        if (data.success) {
          this.$message.success('原文正在拆分中，请稍后查看！')
        } else {
          this.$message.success(data.data.message)
        }
      })
    },
    //ocr识别
    async txt(row) {
      const data = this.$post('ocrQueue/addOcrQueue', {
        formDefinitionId: row.formDefinitionId,
        registerId: row.id,
        batchName: row.batch_name,
        batchNo: row.batch_no
      })
      data.then(data => {
        if (data.success) {
          this.$message.success('jpg正在转换中，请稍后查看！')
        } else {
          this.$message.success(data.data.message)
        }
      })
    },
    //拆件
    async loadDaFenLei(row) {
      if (row.archivers_category_code.indexOf("WS") != -1) {
        const data = await this.$post('ocr/batchChaiJIan', {
          registerId: row.id,
          formDefinitionId: row.formDefinitionId,
        })
        if (data.data.success) {
          this.$message.success('自动拆件中，请在拆件记录中查看结果！')
        } else {
          this.$message.error(data.data.message)
        }
      } else {
        this.$message.error("不是文书档案不需要进行拆件操作！！！")
      }
    },
    //执行ofd转换操作
    async doSend(row) {
      const data = await this.$post('uploadfiles/JpgAndTxtToOfd', {
        registerId: row.id,
        formDefinitionId: row.formDefinitionId,
      })
      data.then(data => {
        if (data.success) {
          this.$message.success('原文正在拆分中，请稍后查看！')
        } else {
          this.$message.success(data.data.message)
        }
      })
    },
    //TXT转Excel
    async doExcel(row) {
      const data = this.$post('ocr/ocrTxtToExcel', {
        formDefinitionId: row.formDefinitionId,
        registerId: row.id,
      })
      data.then(data => {
        if (data.success) {
          this.$message.success('原文正在拆分中，请稍后查看！')
        } else {
          this.$message.success(data.data.message)
        }
      })
    },
    //获取当前登录人（先这样获取）
    user() {
      this.$http.post('/fonddata/user').then(({data}) => {
        if (data && data.success) {
          this.userName = data.data
        }
      })
    },
    //=======================================================以下是表单配置方案=======================================
    configTest(row) {
      this.$router.push({
        path: '../archive/manage/impscheme',
        query: {batch_no: row.batch_name}
      })
    },
    deleteArrElement(i) {
      if (this.value.indexOf(i) !== -1) this.value.splice(this.value.indexOf(i), 1)
    },
    /*
    configTest(row) {
        let formDefinitionId = row.formDefinitionId;
        this.$http.post('/manage/form/selectFormDefinitionById', {formDefinitionId: formDefinitionId}).then(({data}) => {
            if (data.success) {
                this.formDefinition = data.data
                this.loadFields(formDefinitionId);
            } else {
                this.$message.error(data.message)
            }
        })
    },
    loadFields(formDefinitionId) {
        this.$http.post('/manage/form/findFieldList', {formDefinitionId: formDefinitionId}).then(({data}) => {
            if (data.success) {
                this.fieldData = data.data
                this.detailConfig = true;
            } else {
                this.$message.error(data.message)
            }
        })
    },
    //添加排序字段
    add(index, row) {
        const len = this.itemData.length
        row.order = len + 1
        row.code = row.fieldCode
        row.name = row.label
        row.id = null
        row.codeLength = row.fieldLength
        row.codeType = row.fieldTypeStr
        let flag = true
        for (const itemDatum of this.itemData) {
            if (itemDatum.code == row.code) {
                flag = false
            }
        }
        if (flag) {
            this.fieldData.splice(index, 1)
            this.itemData.push(row)
        } else {
            this.$message.warning("该字段方案中已存在")
        }
    },
    clearData() {
        this.itemData = []
    },
    //将选中的排序字段删除
    subtract(index, row) {
        row.hashKey = ''
        this.itemData.splice(index, 1)
        if (this.formId) {
            this.fieldData.push(row)
        }
        this.deleteItem.push(row)
    },
    //将点击的字段排序上移一位
    up(index, row) {
        if (index !== 0) {
            this.itemData.splice(index, 1)
            this.itemData.splice(index - 1, 0, row)
        }
    },
    saveFormConfig() {
        for (let i = 0; i < this.itemData.length; i++) {
            this.itemData[i].order = i + 1
            this.itemData[i].businessId = this.schemeId
            if (!this.itemData[i].hashKey) {
                this.$message.error("请将字段显示名补充完整")
                return
            }
        }
        if (this.deleteItem.length > 0) {
            let ids = ''
            for (const argument of this.deleteItem) {
                if (argument.id) {
                    ids += argument.id + ","
                }
            }
        }
    },*/
  },
  //判断数组中是否该数字存在，如果存在则删除
  //=======================================================以下是被淘汰的=======================================
  /*async savePerson() {
      let personIdLinShi = ''
      let personNameLinShi = ''
      let obj = {};
      let array = [];
      if (this.opType == 'edit') {
          this.addForm.personId = this.personIds
      }
      if (this.addForm.personId) {
          //穿梭框获取右侧的数据，放入数据字典，方便取值
          for (let i = 0; i < this.personData.length; i++) {
              for (let j = 0; j < this.addForm.personId.length; j++) {
                  if (this.addForm.personId[j] == this.personData[i].key) {
                      obj = {
                          "key": this.personData[i].key,
                          "label": this.personData[i].label
                      }
                      array.push(obj);
                  }
              }
          }
          array.map(item => {
              personIdLinShi += item.key + ','
              personNameLinShi += item.label + ','
          })
          this.addForm.personId = personIdLinShi.substring(0, personIdLinShi.length - 1)
          this.addForm.personName = personNameLinShi.replaceAll(',', ' ')
      }
      this.personDialogVisible = false
      this.permissionDialogVisible = true
  },
  async save() {
      const valid = await this.$refs.addForm.validate()
      if (valid) {
          let obj = this.data.find(item => item.batch_name === this.addForm.batch_name)
          if (obj && this.opType === 'add') {
              this.$message.error('批次号不能重复！')
              return
          }
          this.addDialogVisible = false
          this.personDialogVisible = true
      }
  },
  async bindRoleUser() {
      if (this.opType === 'add') {
          await this.$post('/fonddata/bindRoleUser', {
              id: this.selectRoleId,
              personIds: this.selectPersonId.length === 1 ? this.selectPersonId[0] : this.selectPersonId.join(',')
          })
          this.roleIds.push({
              roleId: this.selectRoleId,
              personIds: this.selectPersonId.length === 1 ? this.selectPersonId[0] : this.selectPersonId.join(',')
          })
          this.addForm.roleId = JSON.stringify(this.roleIds)
      } else {
          await this.$post('/fonddata/bindRoleUser', {
              id: this.selectRoleId,
              personIds: this.selectPersonId.length === 1 ? this.selectPersonId[0] : this.selectPersonId.join(',')
          })
          this.roleIds.push({roleId: this.selectRoleId, personIds: this.selectPersonId})
      }
      this.addForm.roleId2 = JSON.stringify(this.roleIds)
      this.dialogShow = false
      this.permissionDialogVisible = true
      this.$message.success('分配成功！')
  },
  async handleClick(row) {
      let obj = {};
      let array = [];
      for (let i = 0; i < this.personData.length; i++) {
          for (let j = 0; j < this.valuePerson.length; j++) {
              if (this.valuePerson[j] == this.personData[i].key) {
                  obj = {
                      "key": this.personData[i].key,
                      "label": this.personData[i].label,
                  }
                  array.push(obj);
              }
          }
      }
      this.persons = array
      if (this.opType === 'edit') {
          let obj1 = []
          if (!this.addForm.roleId2) {
              this.addForm.roleId2 = this.addForm.roleId
          }
          let result = JSON.parse(this.addForm.roleId);
          if (result != null && result != "" && result != undefined) {
              for (let i = 0; i < result.length; i++) {
                  obj1 = {
                      "roleId": result[i].roleId,
                      "personIds": result[i].personIds
                  }
              }
              let roId = obj1.personIds.length === 1 ? obj1.personIds[0] : obj1.personIds.split(",")
              this.rolePersons = []
              for (const id of roId) {
                  if (id) {
                      this.rolePersons.push(id)
                  }
              }
              this.selectPersonId = [obj1.personIds]
          }
      } else {
          this.rolePersons = []
      }
      this.selectRoleId = row.key
      this.dialogShow = true
  },
  //最终点击完成
  async savePermission() {
      this.loading = true
      let register = ''
      if (this.opType == 'add') {
          register = '/insertRegister'
      } else {
          register = '/updateRegister'
      }
      this.$http.post('/fonddata' + register, this.addForm).then(({data}) => {
          if (data && data.success) {
              this.permissionDialogVisible = false
              this.loadData()
              this.$message.success('操作成功！')
          } else {
              this.loading = false
              this.permissionDialogVisible = false
              this.$message.error(data.message)
          }
      })
  },
  handleChangePermission(value1, direction, movedKeys) {
      this.selectPersonId = value1
  },
  handleChange(value, direction, movedKeys) {
      this.addForm.personId = value
  },
  //获取全宗号
   async getFond() {
       const {data} = await this.$http.post('/fonddata/getFondByOrgCode')
       this.fondData = data.data
   },
  //!**获取门类名称
 selectGetCategoryName(code){
     this.$http.post('/fonddata/getCategoryName',{code:code})
         .then(({data})=>{
             console.log(data)
             console.log(data.data)
             if(data.success){
                 this.addForm.archivers_category_code = data.data
             }
         })
 },
   /!**获取执行方案名称
   selectScheme(standard){
       this.$http.post('/fonddata/getScheme',{standard:standard})
           .then(({data})=>{
               console.log(data)
               console.log(data.data)
               if(data.success){
                   this.addForm.code = data.data
               }
           })
   },
  //点击全宗获取门类方法
  selectFond(value) {
      this.fondData.forEach(item => {
          if (value == item.id) {
              this.fond = item
              this.getCategory(item.arcTypes)
          }
      })
  },
  //获取门类信息
  async getCategory(arcTypes) {
      const {data} = await this.$http.post('/fonddata/getCategory', {arcTypes: arcTypes})
      this.cateGoryData = data.data
  },
  // 点击节点
  handleCheck(val){
      console.log(val)
      console.log("点击事件")
      let arr = val.toString().split(',');
      this.getArchivebsp(arr[1])
      this.addForm.archivers_category_code = arr[1]
  },
  //获取元数据方案
  async getArchivebsp(id) {
      const {data} = await this.$http.post('/fonddata/getArchiveBspDict', {code: id})
      this.CategoryBspDict = data.data
  },
  //旧查看
  edit(row) {
      this.opType = 'edit'
      this.title = '查看批次'
      this.vIfd = false
      this.inputDisabled = true
      let ids = ''
      console.log(row)
      this.addForm = Object.assign({}, row)
      this.addForm.yuanSJ = this.addForm.metadataName
      console.log(this.addForm)
      this.valuePerson = []
      if (this.addForm.personId) {
          ids = this.addForm.personId.split(",")
          for (const id of ids) {
              if (id) {
                  this.valuePerson.push(id)
              }
          }
      }
      this.personIds = this.valuePerson
      this.cateGoryData = [{label: row.form_scheme, value: row.archivers_category_code}]
      this.addDialogVisible = true
  },*/
  watch: {
    //原文格式
    'addForm.original_format'(v) {
      if (v!==undefined){
        if (v == 3) {
          //纸质只能扫描
          this.deleteArrElement(4)
          this.value.push(4)
          //不能拆tif
          this.deleteArrElement(1)
        } else {
          //非纸质
          //不能扫描
          this.deleteArrElement(4)
          //只能拆tif
          this.deleteArrElement(1)
          this.value.push(1)
        }
      }
    },
    //目标格式
    'addForm.target_format'(v) {
      if (v == 2) {
        let arr = [24]
        arr.forEach(i => {
          this.deleteArrElement(i)
        })
        this.value = this.value.concat(arr)
      }
    },
    'value'(v){
      //表示门类里面有公文，可以自动拆件
      if (this.addForm.archivers_category_code_name&&this.addForm.archivers_category_code_name.includes("公文")) {
        if (v.includes(7)){
          //自动拆件在数组中的index
          this.segmentData[4].disabled = false
        }else {
          this.segmentData[4].disabled = true
        }
      } else {
        this.segmentData[4].disabled = true
      }

    },
    'addForm.archivers_category_code_name'(v){
      if (v&&v.includes("公文")){
        if (this.value.includes(7)){
          //自动拆件在数组中的index
          this.segmentData[4].disabled = false
        }else {
          this.segmentData[4].disabled = true
        }
    }else {
        this.segmentData[4].disabled = true
    }
    }
  }
}
</script>

<style lang="scss">
.select-tree {
  height: auto;
  max-height: 200px;
  overflow-y: auto;
  background-color: white;
  padding: 0;
}

.el-transfer-panel {
  width: 350px !important;
}
</style>
