<template>
  <section>
    <nac-info title="拆件">
      <el-tag @click="loadData" style="cursor:pointer">{{ dangHao }}</el-tag>
      <el-button type="primary" @click="disassemblyDialog" :loading="loading">拆件详情
      </el-button>
      <el-button type="primary" icon="el-icon-sort-up" @click="loadPre" :loading="loading">上一件</el-button>
      <el-button type="primary" icon="el-icon-sort-down" @click="loadNext" :loading="loading">保存并下一件
      </el-button>
      <el-button v-if="id" type="primary" icon="el-icon-save" @click="save" :loading="loading">暂存</el-button>
      <el-button v-if="id" type="primary" icon="el-icon-save" @click="saveSubmit" :loading="loading">保存并提交
      </el-button>
      <tagging-button v-show="id!==''" :obj="obj()" ref="tagging"></tagging-button>
            <return-button v-if="id" :id.sync=id :query="$route.query._query" :types="rType"/>
            <el-button type="primary" v-if="id!==''" @click="back" style="text-align: center" icon="el-icon-save">返回</el-button>
    </nac-info>
    <div class="index_main" v-loading="loading">
      <dragImgContainer ref="imgContainer"/>
    </div>
    <div>
      <el-dialog title="拆件详情" :visible.sync="disassemblyRecordDialog">
        <el-table :data="disassemblyRecordData">
          <el-table-column property="fileName" label="文件名"></el-table-column>
          <el-table-column property="disassemblyRecord" label="拆件详情"></el-table-column>
        </el-table>
      </el-dialog>
    </div>
  </section>
</template>
<script>
import returnButton from "@/components/customButton/returnButton";
import dragImgContainer from "./dragImgContainer";
import './style.scss'
import {v4} from 'uuid'
import taggingButton from "@/components/customButton/taggingButton";

export default {
  name: "inEntry",
  components: {returnButton, dragImgContainer, taggingButton},
  data() {
    return {
      //当前处于第几条
      index: parseInt(this.$route.query.index),
      //档号
      dangHao: this.$route.query.message.dangHao,
      //当前编辑表单数据Id
      id: this.$route.query.message.id,
      //加载状态
      loading: false,
      //文件夹数据
      volumesData: [],
      //图片数据
      ImgData: [],
      disassemblyRecordDialog: false,
      disassemblyRecordData: [],
      flowType: '',
      flowStringName: '',
      flag:false
    }
  },
  provide() {
    return {
      splitParent: this,
    }
  },
  computed: {
    rType() {
      if (this.$route.query.message) {
        return this.$route.query.message.rtype
      } else {
        return ''
      }
    },
  },
  methods: {
    async $init() {
      this.index = parseInt(this.$route.query.index)
      this.dangHao = this.$route.query.message.dangHao
      this.id = this.$route.query.message.id
      this.flowPath()
      await this.loadData()

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
    async loadData() {
      this.loading = true
      const {data} = await this.$post("/wsSplit/updateSplitStatus", {
        formDefinitionId: this.$route.query.message.ajFormId,
        formDataId: this.id
      });
      if (data && data.success) {
        await this.loadImage()
        await this.loadDirData()
      } else {
        this.$message.error(data.message)
      }

      this.loading = false
    },
    async loadImage() {
      //加载图片数据
      const {data} = await this.$post('/processing/findImgPage', {
        formDefinitionId: this.$route.query.message.ajFormId,
        id: this.id,
        type: this.$route.query.message.type,
        page: false,
      })
      if (data.success) {
        //根据文件夹数据对图片分组，
        this.ImgData = data.data.map((v, sourceIndex) => {
          const index = v.fileName.indexOf('_')
          if (index > 0) {
            v.label = v.fileName.substring(0, index)
          } else {
            v.label = v.fileName.split('.')[0]
          }
          return {...v, sourceIndex: sourceIndex + 1, id: v4()}
        })
      }
    },
    async loadDirData() {
      const {data} = await this.$post('/fileStructure/wjJgDataTree', {
            archivers_category_code: this.$route.query.message.code,
            aj_archival_code: this.dangHao,
            registerId: this.$route.query.message.registerId
          },
          {timeout: 20000})
      if (data.success) {
        this.volumesData = data.data
        this.volumesData.forEach(v => {
          //从第几页开始
          let pageNumber = parseInt(v.data.page_number)
          if (pageNumber === 0) {
            pageNumber = 1
            //页数包括在总数内，业务从1开始
          }
          //总共有几页
          const total = parseInt(v.data.total_number_of_pages)
          let index = 0;
          for (let i = 0; i < total; i++) {
            const j = pageNumber - 1 + i
            index++;
            const imgData = this.ImgData[j]
            if (imgData) {
              imgData.pid = imgData.sourcePid = v.id
              imgData.sourceIndex = imgData.dirIndex = index
            }
          }
        })
      }
    },
    async loadPre() {
      await this.doLoad(this.index - 1)
    },
    async loadNext() {
      const result = await this.saveDirs()
      if (result) {
        await this.doLoad(this.index + 1)
      }
    },
    async doLoad(index) {
      let page = {size: 1, index}
      const query = this.$route.query._query
      const {data} = await this.$post('/manage/formData/formDataPage',
          Object.assign(
              //默认参数
              query,
              //分页参数
              page),
          {timeout: 20000})
      if (data.success) {
        if (data.data.data.length === 1) {
          const index = data.data.start + 1
          const item = data.data.data[0]
          if (item.id !== this.id) {
            await this.$router.replace({
              query: {
                message: Object.assign({},
                    this.$route.query.message,
                    {
                      rtype: item.status_info,
                      id: item.id,
                      dangHao: item.archival_code
                    }),
                type: this.$route.query.type,
                index,
                _query: this.$route.query._query
              }
            })
            await this.$init()

          } else {
            await this.$init()
            if (this.flag) {
              this.flag = false
              this.back()
            }
            this.flag = false
            this.$message.warning("未查询到更多数据")
          }
        } else {
          await this.$init()
          this.$message.warning("未查询到更多数据")
        }
      } else {
        await this.$init()
        this.$message.warning('查询失败：' + data.message)
      }
      this.$refs.tagging.$init()
    },
    back() {
      this.$router.push({
        path: "/wssplit",
        selectFond:this.$route.query.message.FondId
      })
    },
    async save() {
      const result = await this.saveDirs()
      if (result) {
        await this.$init()
      }
    },
    async saveSubmit() {
      const result = await this.saveDirs()
      if (result) {
        this.loading = true
        const {data} = await this.$post('/register/updateStatus', Object.assign(this.$route.query._query, {
          type: this.flowType,
          id: this.id,
          //手动拆分
          status: "manualOperation"
        }))
        if (data.success) {
          this.$message.success('提交到' + "" + this.flowStringName + "!")
          this.flag = true
          await this.doLoad(this.index + 1)
          // await this.$init()
        } else {
          this.$message.error(data.message)
          this.loading = false
        }
      } else {
        // this.$message.success("当前档案已有操作人，为确保唯一性，您不能更改该条档案")
      }
    },
    async saveDirs() {
      //先计算文件夹排序
      const pidOrder = {}
      this.volumesData.forEach((v, i) => pidOrder[v.id] = i)

      const tempImgs = []
      //先判断没有pid的情况
      for (let i = 0; i < this.ImgData.length; i++) {
        const item = this.ImgData[i]
        if (!item.pid) {
          this.$message.error(item.label + '没有选中文件夹')
          this.$refs.imgContainer.scrollTo(item.id)
          return
        }
        tempImgs.push({
          fileName: item.fileName,
          filePath: item.filePath,
          pidOrder: pidOrder[item.pid],
          pid: item.pid,
          dirIndex: item.dirIndex,
          tag: item.tag
        })
      }
      const volCounts = {}
      const pthohParaM = tempImgs.sort((a, b) => {
        if (a.pidOrder === b.pidOrder) {
          return a.dirIndex - b.dirIndex
        } else {
          return a.pidOrder - b.pidOrder
        }
      }).map((v, i) => {
        if (volCounts[v.pid]) {
          volCounts[v.pid] = volCounts[v.pid] + 1
        } else {
          volCounts[v.pid] = 1
        }
        return [i + 1, v.fileName, v.filePath].join('@')
      }).join(',')

      let total = 0;
      const volumesDataParaM = this.volumesData.map(v => {
        const count = volCounts[v.id] || 0
        total += count
        return [
          total - count + 1,
          count,
          v.data.archives_item_number,
          v.data.note,
          v.data.file_type,
          v.description,
          v.id
        ].join('@')
      }).join(',')
      this.loading = true
      const id = this.id
      const {data} = await this.$http.post('wsSplit/saveChangeTree', Object.assign(this.$route.query._query, {
        type: this.$route.query.type,
        id: this.id,
        pthohParaM: pthohParaM,
        volumesDataParaM: volumesDataParaM
      }))
      if (data && data.success) {
        this.$message.success("保存成功！")
        return true
      } else {
        this.$message.error(data.message)
        this.loading = false
      }
    },
    flowPath() {
      this.$http.post('/fonddata/flowPath', {
        fid: this.$route.query.message.formId,
        type: this.$route.query.message.rtype,
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
    disassemblyRecord() {
      let obj = this.obj()
      this.$http.post('/ocr/disassemblyRecord', obj).then((data) => {
        if (data.data.success) {
          this.disassemblyRecordData = data.data.data
        }
      })
    },
    disassemblyDialog() {
      this.disassemblyRecord()
      this.disassemblyRecordDialog = true
    }
  }
}
</script>