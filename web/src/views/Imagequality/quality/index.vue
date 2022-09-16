<template>
    <section>
        <nac-info title="图像质检">
            <el-tag @click="elTag" style="cursor:pointer">{{ dangHao }}</el-tag>
            <el-button type="primary" v-on:click="loadOne('up')" icon="el-icon-sort-up">上一件
            </el-button>
            <el-button type="primary" v-on:click="loadOne('down')" icon="el-icon-sort-down">下一件
            </el-button>
            <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交并下一件</el-button>
            <tagging-button v-show="id!==''" :obj="obj()" ref="tagging"></tagging-button>
<!--            <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交</el-button>-->
            <return-button v-if="id!==''" :id.sync=id :query="this.$route.query._query" :types="this.rtype" @toDetail="loadOne"></return-button>
            <el-button type="primary" @click="back()" >返回</el-button>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <el-row style="height: 100%">
                <Split style="height: 100%;" :gutterSize="6">
                    <SplitArea :size="15">
                        <div style="height: 100%">
                            <div class="moveRow" style="height: 100%">
                                <el-table row-key="id"
                                          :data="ImgData"
                                          stripe
                                          border
                                          height="100%"
                                          style="width: 100%"
                                          @row-click="imgOpenDetails"
                                          highlight-current-row>
                                    <el-table-column
                                            prop="fileName"
                                            label="文件名"
                                            align="center"
                                            header-align="center"
                                            sortable>
                                    </el-table-column>
                                    <el-table-column
                                            prop="fileSize"
                                            label="大小"
                                            align="center"
                                            header-align="center"
                                            sortable>
                                    </el-table-column>
                                </el-table>
                            </div>
                        </div>
                    </SplitArea>
                    <SplitArea :size="55">
                        <div class="imageEditorApp">
                            <div class="imageEditorApp">
                                <viewer :images="images" style="height: 200px">
                                    <img v-for="src in images" :src="src" :key="src" width="800"/>
                                </viewer>
                            </div>
                        </div>
                    </SplitArea>
                    <SplitArea :size="30">
                        <el-row class="gallery">
                            <div style="margin: 0 0 0 2px">
                                <volumes-form :form-definition-id="ajFormId" :fond-id="ajFondId"
                                              :category-id="categoryId"
                                              :form="form" ref="form"/>
                                <div slot="footer" class="dialog-footer">
                                    <el-button type="primary" @click="save('form')" class="btn-submit">保 存</el-button>
                                </div>
                            </div>
                        </el-row>
                    </SplitArea>
                </Split>
            </el-row>
        </div>
    </section>
</template>

<script>
    import indexMixin from '@/util/indexMixin'
    import volumesForm from "../../lib/volumesForm/index";
    import returnButton from "../../../components/customButton/returnButton";
    import taggingButton from "@/components/customButton/taggingButton";
    export default {
        name: "index",
        mixins: [indexMixin],
        components: {volumesForm,returnButton,taggingButton},
        data() {
            return {
                message: this.$route.query.message,
                dangHao: this.$route.query.message.dangHao,
                type: this.$route.query.message.type,
                ajRow: this.$route.query.ajRow,
                ajFormId: this.$route.query.message.ajFormId,
                ajFondId: this.$route.query.message.ajFondId,
                formId: this.$route.query.message.formId,
                code: this.$route.query.message.code,
                categoryId: this.$route.query.message.ajFondId,
                id: this.$route.query.message.id,
                rtype:this.$route.query.message.rtype,
                index: this.$route.query.index,
                ImgData: [],
                images: [],
                archiveData: [],
                form: {},
                flowType:'',
                flowStringName:'',
                flag:false
            }
        },
        created() {
          this.flowPath()
        },
        methods: {
          back(){
            this.$router.push({path:"/Imagequality",selectFond:this.$route.query.message.FondId})
          },
          obj(){
            let obj = new Object();
            if (this.$route.query.message&&this.$route.query.message.ajFormId){
              obj.formDefinitionId = this.$route.query.message.ajFormId
            }
            obj.archivesId = this.id
            //状态 0，无标注 1，有标注
            obj.wssplitTaggingCondition = '1'
            return obj
          },
            elTag() {
                this.form = Object.assign({}, this.ajRow)
                this.imgLoadData()
            },
            $init() {
                this.dangHao= this.$route.query.message.dangHao
                this.type= this.$route.query.message.type
                this.ajRow= this.$route.query.ajRow
                this.ajFormId= this.$route.query.message.ajFormId
                this.ajFondId= this.$route.query.message.ajFondId
                this.formId= this.$route.query.message.formId
                this.code= this.$route.query.message.code
                this.categoryId= this.$route.query.message.ajFondId
                this.id= this.$route.query.message.id
                this.rtype=this.$route.query.message.rtype
                this.index =  this.$route.query.index
                this.imgLoadData()
                this.form = Object.assign({}, this.ajRow)
            },

            //加载图片原文数据
            imgLoadData() {
                this.loading = true
                let params = Object.assign({}, {
                    formDefinitionId: this.ajFormId,
                    id: this.ajRow.id,
                    type: this.type,
                    page: false,
                })
                this.$http.post('/processing/findImgPage', params).then(({data}) => {
                    if (data.success) {
                        this.ImgData = data.data
                        this.images = []
                        if (this.ImgData.length > 0) {
                            for (let i = 0; i < this.ImgData.length; i++) {
                                this.images.push(this.ImgData[i].filePath + "?temp=" + Math.random())
                            }
                        }
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },
            //点击图片
            imgOpenDetails(row) {
                this.images = []
                let url = row.filePath + "?temp=" + Math.random()
                this.images.push(url)
            },
            //保存方法
            async save() {
                //准备参数
                const defaultParams = {
                    formDefinitionId: this.ajFormId,
                    fondId: this.ajFondId,
                    categoryId: this.categoryId,
                }
                const url = `/manage/formData/${this.form.id ? "updateFormData" : "insertFormData"}`
                const {data} = await this.$post(url, Object.assign(defaultParams, this.form))
                if (data.success) {
                    this.ajRow = data.data
                    this.$init()
                    this.$message.success("保存成功！")
                } else {
                    this.$message.warning(data.message.replace("服务器错误：", ""))
                }
            },
            //上一件，下一件
            async loadOne(type) {
                let page = {size: 1, index: 0}
                if ('down' === type) {
                    page.index = parseInt(this.index) + 1
                } else if ('up' === type) {
                    page.index = parseInt(this.index) - 1
                } else {
                    page.index = parseInt(this.index)
                }
                if (page.index === 0) {
                    this.$message.error("已是第一份")
                    return
                }
                const query = this.$route.query._query
                const {data} = await this.$post('/manage/formData/formDataPage',
                    Object.assign(
                        //默认参数
                        query,
                        //分页参数
                        page),
                    {timeout: 20000})
              console.log(data)
                if (data.success) {
                    if (data.data.data.length > 0) {
                        this.archiveData = data.data.data
                        await this.judge(this.archiveData[0])
                        this.lurArchive(this.archiveData[0])
                        console.log(page.index)
                        this.index = page.index
                    } else {
                      if (this.flag){
                        this.flag = false
                        this.back()
                      }
                        this.flag = false
                        this.$message.error("已是最后一份")
                    }
                } else {
                    this.$message.error(data.message)
                }
              this.$refs.tagging.$init()
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
            //切换档案
            lurArchive(row) {
                this.id = row.id
                this.dangHao = row.archival_code
                this.ajRow = row
                this.elTag()
            },
            //提交
            async submit() {
                this.$post('/register/updateStatus', Object.assign(this.$route.query._query, {
                    type: this.flowType,
                    id: this.id,
                })).then(({data}) => {
                    if (data.success) {
                        this.id = ''
                      this.$message.success('提交到' + "" + this.flowStringName + "!")
                      //主要用于判断是不是通过提交进入的下一件
                        this.flag = true
                        this.loadOne()
                    } else {
                        this.$message.error(data.message)
                    }
                })
            },
            flowPath(){
              this.$http.post('/fonddata/flowPath', {
                fid: this.formId,
                type:this.rtype,
                state:1
              }).then(({data}) => {
                if (data && data.success) {
                  //type给提交，用于提交，
                  this.flowType = data.data.flowBatchName
                  this.flowStringName = data.data.flowStringName
                }})
              //退回功能，将当前type给后台，后台吧前面的都返回回来。
            },

            /*back(){
              this.$router.replace({path: '../../Imagequality'});
              //this.$router.replace('../../Imagequality')
            }*/
        }
    }
</script>

<style scoped>

</style>
