<template>
    <section>
        <nac-info title="拆件过程">
        </nac-info>
        <div class="card" style="position:relative" align="center">
            <el-card>
                <el-form ref="anForm" :model="anForm" :inline="true" label-width="400px">
                    <br/>
                    <el-form-item>
                        <el-button type="primary" @click="adsDialog()"> 批量拆分</el-button>
                        <el-button type="primary" @click="txtDialog()"> OCR批量识别</el-button>
                        <el-button type="primary" @click="wsType()"> 自动批量分件</el-button>
                        <el-button type="primary" @click="txtHb()"> TXT结果合并</el-button>
                        <pdf-ofd style="margin-left: 10px"></pdf-ofd>
                    </el-form-item>

                    <br/>
                    <h3 style="color: red">操作过程耗时较长,请勿重复提交！！！</h3>
                </el-form>
            </el-card>
        </div>
      <el-dialog :visible.sync="fondTree" title="请选择批次，然后选择全宗后点击确定。" width="30%">
        <fond-tree v-on:check="check" ref="fondTree"></fond-tree>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="saveFondAndCatagray">确 定</el-button>
        </div>
      </el-dialog>
      <el-dialog :visible.sync="batchDialog" title="批量拆分" width="800px" :close-on-click-modal="false">
        <div class="card" style="position:relative" align="center">
          <el-card>
            <el-form ref="form" :model="form" :inline="true" label-width="110px">
              <el-form-item label="选择全宗门类：" prop="categoryName">
                <el-input v-model="form.categoryName" style="width: 300px">
                  <el-button slot="append" icon="el-icon-search" type="primary"
                             @click="getFondAndCatagory()"></el-button>
                </el-input>
              </el-form-item>
              <br/>
              <el-form-item>
                <el-button type="primary" @click="ads()"> 开始拆件</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-dialog>
        <el-dialog :visible.sync="batchOCR_Dialog" title="ocr识别" width="800px" :close-on-click-modal="false">
        <div class="card" style="position:relative" align="center">
          <el-card>
            <el-form ref="form" :model="form" :inline="true" label-width="110px">
              <el-form-item label="选择全宗门类：" prop="categoryName">
                <el-input v-model="form.categoryName" style="width: 300px">
                  <el-button slot="append" icon="el-icon-search" type="primary"
                             @click="getFondAndCatagory()"></el-button>
                </el-input>
              </el-form-item>
              <br/>
              <el-form-item>
                <el-button type="primary" @click="txt()"> 开始识别</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-dialog>
        <el-dialog :visible.sync="switchWsType" title="自动批量拆分" width="800px" :close-on-click-modal="false">
            <div class="card" style="position:relative" align="center">
                <el-card>
                    <el-form ref="form" :model="form" :inline="true" label-width="110px">
                        <el-form-item label="选择全宗门类：" prop="categoryName">
                            <el-input v-model="form.categoryName" style="width: 300px">
                                <el-button slot="append" icon="el-icon-search" type="primary"
                                           @click="getFondAndCatagory()"></el-button>
                            </el-input>
                        </el-form-item>
                        <br/>
                        <el-form-item>
                            <el-button type="primary" @click="loadDaFenLei()"> 开始批量分件</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </div>
        </el-dialog>
        <el-dialog :visible.sync="hbTxtType" title="txt结果合并" width="800px" :close-on-click-modal="false">
            <div class="card" style="position:relative" align="center">
                <el-card>
                    <el-form ref="form" :model="form" :inline="true" label-width="110px">
                        <el-form-item label="选择全宗门类：" prop="categoryName">
                            <el-input v-model="form.categoryName" style="width: 300px">
                                <el-button slot="append" icon="el-icon-search" type="primary"
                                           @click="getFondAndCatagory()"></el-button>
                            </el-input>
                        </el-form-item>
                        <br/>
                        <el-form-item>
                            <el-button type="primary" @click="loadHbFile()"> 开始合并</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </div>
        </el-dialog>
        </section>
</template>

<script>
    import indexMixin from '@/util/indexMixin'
    import pdfOfd from "../lib/components/pdfOfd";

    export default {
        components:{pdfOfd},
        data() {
            return {
                form: {categoryName: ''},
                anForm: {},
                switchWsType: false,
                hbTxtType: false,
                fondTree: false,
                ajFormDefinitionId: '',
                wjFormDefinitionId: '',
                fondId: '',
                registerId: '',
                catagrayConfig: {},
                fond: {},
                categoryCode: '',
                categoryId: '',
                percentage: 0,
                total: 0,
                timeStop: '',
                batchDialog:false,
                batchOCR_Dialog:false,
            }
        },
        mixins: [indexMixin],
        mounted() {
            setInterval(() => {
                if (this.percentage >= 100) {
                    this.show = false
                }
            }, 2000)//进度条加载100后关闭进度条框
        },
        methods: {
            adsDialog(){
              this.batchDialog = true
            },
            async ads() {
              this.loading = false
              const data = this.$post('uploadfiles/batchTiffToJpgByPath',{
                formDefinitionId:this.wjFormDefinitionId,
              })
              data.then(data =>{
                if (data.data.success) {
                  this.$message.success('原文正在拆分中，请稍后查看！')
                  this.batchDialog = false
                } else {
                  this.$message.success(data.data.message)
                  this.batchDialog = false
                }
              })
              this.$refs.form.resetFields();
              this.loading = false
            },

            txtDialog(){
              this.batchOCR_Dialog =true
            },
            async txt() {
                const data = this.$post('uploadfiles/batchJpgToTxt',{
                  formDefinitionId:this.wjFormDefinitionId,
                  registerId:this.registerId
                })
                data.then(data =>{
                  if (data.data.success) {
                    this.$message.success('jpg正在转换中，请稍后查看！')
                  } else {
                    this.$message.success(data.data.message)
                  }
                })
                this.batchOCR_Dialog =false
                this.$refs.form.resetFields();
            },
            //点击分类 弹出选择框
            wsType() {
                this.switchWsType = true
            },
            //弹出选择批次档案方案选择框
            getFondAndCatagory() {
                this.fondTree = true
            },
            //确定本次选择的批次档案
            saveFondAndCatagray() {
                this.fondTree = false
                this.$http.post('/manage/categoryconfig/page', {
                    page: false,
                    businessId: this.categoryId
                }).then(({data}) => {
                    if (data.success) {
                        if (data.data.length == 0) {
                            this.ajFormDefinitionId = ''
                            this.wjFormDefinitionId = ''
                            this.$message.error("所选全宗门类没有表单方案，请重新选择")
                            return
                        }
                        this.catagrayConfig = data.data.find(v => {
                            return v.default === true
                        })
                        this.formTableTypeChange()
                    }
                })
            },
            formTableTypeChange() {
                this.ajFormDefinitionId = this.catagrayConfig.arcFormId
                this.wjFormDefinitionId = this.catagrayConfig.fileFormId
                this.$refs.form.validateField('categoryName')
            },
            //开始执行
            async loadDaFenLei() {
                this.loading = true
                if (this.$refs.form) {
                    let valid = await this.$refs.form.validate()
                    if (valid) {
                        const data = await this.$post('ocr/batchChaiJIan', {
                            registerId: this.registerId,
                            formDefinitionId:this.wjFormDefinitionId,
                        })
                      console.log(data.data)
                        if (data.data.success) {
                            this.$message.success('自动拆件中，请在拆件记录中查看结果！')
                            this.loading = false
                            this.switchWsType = false
                        } else {
                            this.$message.success(data.data.message)
                        }
                    } else {
                        this.$message.error('请选择需要拆分的档案!')
                    }
                }
            },
            check(node) {
                this.currentFond(node.parentId)
                this.fondId = node.data.fondId
                this.form.categoryName = this.batch_name + '-' + node.label
                this.categoryCode = node.data.code
                this.categoryId = node.id
                this.registerId = node.parentId
            },
            currentFond(parentId) {
                if (parentId && this.$refs.fondTree.fonds) {
                    this.fond = this.$refs.fondTree.fonds.find(f => f.id === parentId)
                    this.batch_name = this.fond.batch_name
                    return this.batch_name
                }
            },

            /**
             * TXT 合并
             * @returns {Promise<void>}
             */
            txtHb() {
                this.hbTxtType = true
            },
            //开始合并
            async loadHbFile() {
                this.loading = true
                if (this.$refs.form) {
                    let valid = await this.$refs.form.validate()
                    if (valid) {
                        const data = await this.$post('ocr/txtHb', {
                            registerId: this.registerId,
                            ajFormDefinitionId: this.ajFormDefinitionId,
                            wjFormDefinitionId: this.wjFormDefinitionId,
                            hookFondCategory: this.form.categoryName,
                            _QUERY: JSON.stringify({key: "status_info", type: 'i', value: 'RECEIVE'})
                        })
                        if (data.data.success) {
                            this.$message.success('自动拆件中，请在拆件记录中查看结果！')
                            this.loading = false
                            this.hbTxtType = false
                        } else {
                            this.$message.success(data.data.message)
                        }
                    } else {
                        this.$message.error('请选择需要拆分的档案!')
                    }
                }
            }
        }
    }
</script>
