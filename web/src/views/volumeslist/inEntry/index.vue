<template>
    <section>
        <nac-info title="档案著录">
            <el-tag @click="elTag" style="cursor:pointer">{{ dangHao }}</el-tag>
            <el-button type="primary" v-on:click="loadOne('up')" icon="el-icon-sort-up">上一件
            </el-button>
            <el-button type="primary" v-on:click="loadOne('down')" icon="el-icon-sort-down">下一件
            </el-button>
            <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交并下一件</el-button>
            <tagging-button v-show="id!==''" :obj="obj()" ref="tagging"></tagging-button>
<!--            <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交</el-button>-->
            <return-button v-if="id!==''" :id.sync=id :query="this.$route.query._query"
                           :types="this.rType" :formDefinitionId="this.ajFormId" @toDetail="loadOne"></return-button>
            <el-button type="primary" v-on:click="back()">返回</el-button>
        </nac-info>

        <div class="index_main" v-loading="loading">
            <el-row style="height: 100%">
                <Split style="height: 100%;" :gutterSize="6">
                    <SplitArea :size="18">
                        <div style="height: 100%">
                            <el-table
                                :data="volumesData"
                                stripe
                                border
                                height="100%"
                                style="width: 100%"
                                @row-click="openJnDetails"
                                highlight-current-row>
                                <el-table-column
                                    type="index"
                                    label="序号"
                                    align="center"
                                    header-align="center"
                                    sortable
                                    width="55">
                                </el-table-column>
                                <el-table-column
                                    v-if="this.type===1"
                                    label="文件类型"
                                    align="center"
                                    header-align="center"
                                    sortable>
                                    <template v-slot="scope">
                                        {{ scope.row.file_type | dict('file') }}
                                    </template>
                                </el-table-column>
                                <el-table-columnR
                                    v-if="this.type===2"
                                    label="档号"
                                    align="center"
                                    header-align="center"
                                    sortable>
                                    <template v-slot="scope">
                                        {{ scope.row.archival_code }}
                                    </template>
                                </el-table-columnR>
                                <el-table-column
                                    prop="total_number_of_pages"
                                    label="页数"
                                    align="center"
                                    header-align="center"
                                    sortable
                                    width="70">
                                </el-table-column>
                            </el-table>
                        </div>
                    </SplitArea>
                    <SplitArea :size="52">
                        <iframe ref="iframe" frameborder="0" id="content" :src="src" style="width: 99%; height:99%"/>
                    </SplitArea>
                    <SplitArea :size="30">
                        <el-row class="gallery">
                            <div style="margin: 0px 0 0 10px">
                                <volumes-form :form-definition-id="ajFormId" :fond-id="ajFondId"
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
import taggingButton from "@/components/customButton/taggingButton";

export default {
    name: "index",
    mixins: [indexMixin],
    components: {volumesForm, returnButton: () => import("@/components/customButton/returnButton"), taggingButton},
    data() {
        return {
            message: this.$route.query.message,
            id: this.$route.query.message.id,
            fondCode: this.$route.query.message.fondCode,
            dangHao: this.$route.query.message.dangHao,
            type: this.$route.query.message.type,
            ajRow: this.$route.query.ajRow,
            ajFormId: this.$route.query.message.ajFormId,
            ajFondId: this.$route.query.message.ajFondId,
            formId: this.$route.query.message.formId,
            code: this.$route.query.message.code,
            registerId: this.$route.query.message.registerId,
            rType: this.$route.query.message.rtype,
            volumesData: [],
            src: '',
            archiveData: [],
            form: {},
            page: {
                size: 15,
                index: 0,
                total: 0
            },
            index: this.$route.query.index,
            flowType: '',
            flowStringName: '',
            flag:false

        }
    },
    created() {
        this.flowPath()
    },
    methods: {
        elTag() {
            this.form = Object.assign({}, this.ajRow)
            this.JnLoadData()
            this.form = Object.assign({}, this.ajRow)
        },
        $init() {
            this.message = this.$route.query.message
            this.dangHao = this.$route.query.message.dangHao
            this.type = this.$route.query.message.type
            this.ajRow = this.$route.query.ajRow
            this.ajFormId = this.$route.query.message.ajFormId
            this.ajFondId = this.$route.query.message.ajFondId
            this.formId = this.$route.query.message.formId
            this.code = this.$route.query.message.code
            this.categoryId = this.$route.query.message.ajFondId
            this.id = this.$route.query.message.id
            this.rtype = this.$route.query.message.rtype
            this.index =  this.$route.query.index
            this.JnLoadData()
            this.form = Object.assign({}, this.ajRow)
        },
        obj() {
            let obj = new Object();
            if (this.$route.query.message && this.$route.query.message.ajFormId) {
                obj.formDefinitionId = this.$route.query.message.ajFormId
            }
            obj.archivesId = this.id
            //状态 0，无标注 1，有标注
            obj.wssplitTaggingCondition = '1'
            return obj
        },
        //加载卷内信息数据
        async JnLoadData(param) {
            const defaultParams = {
                archivers_category_code: this.code,
                aj_archival_code: this.dangHao,
                registerId: this.registerId
            }

            const {data} = await this.$post('/fileStructure/page',
                Object.assign(
                    //默认参数
                    defaultParams,
                    //分页参数
                    this.page,
                    //需要查询的参数
                    param),
                {timeout: 20000})
            if (data.success) {
                this.volumesData = data.data.data
                this.page.index = data.data.start / data.data.size + 1
                this.page.size = data.data.size
                this.page.total = data.data.total
                console.log(this.volumesData)
                let pageDate = this.volumesData.findIndex(item => {
                    if (parseInt(item.total_number_of_pages) !== 0) {
                        return true
                    }
                })
                console.log(pageDate)
                if (pageDate !== -1) {
                    this.seeOfd(this.volumesData[pageDate])
                } else {
                    this.$message.warning("文件为空")
                }


            }
        },
        //点击档号展示基本信息
        openJnDetails(row) {
            let params = Object.assign({}, {
                fondCode: row.fonds_identifier,
                ajDh: row.aj_archival_code,
                fileType: row.file_type,
                archiveCode: row.archival_code
            })
            this.$http.post('/ofd/getOfd', params).then(({data}) => {
                if (data.success) {
                    this.src = data.data
                } else {
                    this.$message.warning(data.message)
                }
                this.loading = false
            });
        },
        //点击查看OFD文件
        async seeOfd(row) {
            const {data} = await this.$http.post('/ofd/getOfd', {
                fondCode: row.fonds_identifier,
                ajDh: row.aj_archival_code,
                fileType: row.file_type,
                archiveCode: row.archival_code
            })
            if (data.success) {
                this.src = data.data
            } else {
                this.$message.warning(data.message)
            }
        },
        //保存方法
        async save() {
            //准备参数
            const defaultParams = {
                formDefinitionId: this.ajFormId,
                fondId: this.message.fondId,
                categoryId: this.message.fondId,
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
            if (data.success) {
                if (data.data.data.length > 0) {
                    this.archiveData = data.data.data
                    this.judge(this.archiveData[0])
                    this.lurArchive(this.archiveData[0])
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
        async judge(archiveData) {
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
        async submit() {
            this.$post('/register/updateStatus', Object.assign(this.$route.query._query, {
                type: this.flowType,
                id: this.id,
            })).then(({data}) => {
                if (data.success) {
                    this.id = ''
                  this.$message.success('提交到' + "" + this.flowStringName + "!")
                  this.flag = true
                  this.loadOne()
                } else {
                    this.$message.error(data.message)
                }
            })
        },
        flowPath() {
            this.$http.post('/fonddata/flowPath', {
                fid: this.formId,
                type: this.rType,
                state: 1
            }).then(({data}) => {
                if (data && data.success) {
                    //type给提交，用于提交，
                    this.flowType = data.data.flowBatchName
                    this.flowStringName = data.data.flowStringName
                }
            })
            //退回功能，将当前type给后台，后台吧前面的都返回回来。
        },
        back() {
            this.$router.push({path: '/volumeslist',
              selectFond:this.$route.query.message.FondId})
        }
    }
}
</script>

<style scoped>

</style>
