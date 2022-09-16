<template>
  <section>
    <nac-info title="图像处理">
      <el-tag @click="elTag" style="cursor:pointer">{{ dangHao }}</el-tag>
      <el-button type="primary" v-on:click="convert()" icon="el-icon-refresh">高清转换</el-button>
      <!--      <el-button type="primary" v-on:click="convert(0)" icon="el-icon-refresh">高清转换jpg</el-button>-->
      <!--        <el-button type="primary" v-on:click="convert(1)" icon="el-icon-refresh">高清转换文件夹</el-button>-->
      <!--          <el-button type="primary" v-on:click="convert(2)" icon="el-icon-refresh">高清转换md5</el-button>-->
      <el-button type="primary" v-on:click="loadOne('up')" icon="el-icon-sort-up">上一件
      </el-button>
      <el-button type="primary" v-on:click="loadOne('down')" icon="el-icon-sort-down">下一件
      </el-button>
      <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交并下一件</el-button>
      <el-button type="primary" v-if="id!==''" v-on:click="uploadImg">完成并上传</el-button>
<!--      <el-button type="primary" v-if="id!==''" v-on:click="submit" icon="el-icon-save">提交</el-button>-->
      <tagging-button v-show="id" :obj="obj()" ref="tagging"></tagging-button>
      <return-button v-if="id!==''" :id.sync=id :query="this.$route.query._query" :types="this.rtype"
                     @toDetail="loadOne"></return-button>
      <el-button type="primary" v-if="id!==''" @click="back" style="text-align: center" icon="el-icon-save">返回</el-button>

    </nac-info>
    <div class="index_main" v-loading="loading">
      <el-row style="height: 100%">
        <Split style="height: 100%;" :gutterSize="6">
          <SplitArea :size="25">
            <div class="index_main" style="width: 98%">
              <div class="table-container">
                <el-table
                    ref="table2Img"
                    :data="ImgData"
                    stripe
                    border
                    height="80vh"
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
                  <el-table-column
                      v-if="false"
                      prop="filePath"
                      label="地址"
                      align="center"
                      header-align="center"
                      sortable>
                  </el-table-column>
                </el-table>
              </div>
              <el-pagination
                  @current-change="index=>imgLoadData(index-1,this.formId,this.id,this.type )"
                  :current-page.sync="Page.index"
                  :page-size="Page.size"
                  layout="total, prev, pager, next,jumper"
                  :total="Page.total">
              </el-pagination>
            </div>
          </SplitArea>
          <SplitArea :size="midImgSize">
            <div class="imageEditorApp">
              <tui-image-editor ref="tuiImageEditor"
                                :include-ui="useDefaultUI"
                                :options="imgOptions"
                                @addText="onAddText"
                                @objectMoved="onObjectMoved">
              </tui-image-editor>
            </div>
          </SplitArea>
          <SplitArea :size="rightContentSize">
            <el-row class="gallery">
              <volumes-detail-disabled :form-definition-id="ajFormId" :fond-id="ajFondId"
                                       :form="volumesForm" ref="volumesForm"/>
            </el-row>
          </SplitArea>
        </Split>
        <span class="detailBtn"><el-button style="font-size: 14px;" size="mini" type="primary" @click="detail"
        >查<br/>看<br/>详<br/>情</el-button></span>
      </el-row>
      <el-dialog
          :title="fileName"
          :visible.sync="isImgReplace"
          width="100%"
          style="text-align: center"
      >
        <el-row style="padding-top: 20px">
          <el-col :span="12">
            <span style="font-size: 30px;">原图</span>
            <el-scrollbar style="height: 400px">
              <img :src="this.pathJpg" style="width: auto;height:350px;padding: 20px"/>
            </el-scrollbar>
          </el-col>
          <el-col :span="12">
            <span style="font-size: 30px;">高清化效果图</span>
            <el-scrollbar style="height: 400px">
              <img :src="this.pathJpg" style="width: auto;height:350px;padding: 20px"/>
            </el-scrollbar>
          </el-col>
        </el-row>
        <span slot="footer" class="dialog-footer">
    <el-button @click="isImgReplace = false">取 消</el-button>
    <el-button type="primary" @click="confirm">确认更换</el-button>
  </span>
      </el-dialog>
    </div>
  </section>
</template>

<script>
import indexMixin from '@/util/indexMixin'
import splitPane from 'vue-splitpane'
import 'tui-image-editor/dist/svg/icon-a.svg'
import 'tui-image-editor/dist/svg/icon-b.svg'
import 'tui-image-editor/dist/svg/icon-c.svg'
import 'tui-image-editor/dist/svg/icon-d.svg'
import 'tui-image-editor/dist/tui-image-editor.css'
import 'tui-color-picker/dist/tui-color-picker.css'
import sampleImage from './chuli.png'
import {ImageEditor} from '../../../index'
import volumesDetailDisabled from "@/views/lib/components/volumesDetailDisabled";
import taggingButton from "@/components/customButton/taggingButton";
// 通过在主题中改变样式隐藏掉我们不需要的header
const customTheme = {
  // 在这里换上你喜欢的logo图片
  "common.bi.image": sampleImage,
  // load button
  "loadButton.backgroundColor": "#fff",
  "loadButton.border": "1px solid #ddd",
  "loadButton.color": "#222",
  "loadButton.fontFamily": "NotoSans, sans-serif",
  "loadButton.fontSize": "12px",
  "loadButton.display": "none", // 可以直接隐藏掉
  // download button
  "downloadButton.backgroundColor": "#fdba3b",
  "downloadButton.border": "1px solid #fdba3b",
  "downloadButton.color": "#fff",
  "downloadButton.fontFamily": "NotoSans, sans-serif",
  "downloadButton.fontSize": "12px",
  "downloadButton.display": "none", // 可以直接隐藏掉
  // icons default
  "menu.normalIcon.color": "#8a8a8a",
  "menu.activeIcon.color": "#555555",
  "menu.disabledIcon.color": "#434343",
  "menu.hoverIcon.color": "#e9e9e9",
  "submenu.normalIcon.color": "#8a8a8a",
  "submenu.activeIcon.color": "#e9e9e9",
  "menu.iconSize.width": "24px",
  "menu.iconSize.height": "24px",
  "submenu.iconSize.width": "32px",
  "submenu.iconSize.height": "32px",
  // submenu primary color
  "submenu.backgroundColor": "#1e1e1e",
  "submenu.partition.color": "#858585",
  // submenu labels
  "submenu.normalLabel.color": "#858585",
  "submenu.normalLabel.fontWeight": "lighter",
  "submenu.activeLabel.color": "#fff",
  "submenu.activeLabel.fontWeight": "lighter",
  // checkbox style
  "checkbox.border": "1px solid #ccc",
  "checkbox.backgroundColor": "#fff",
  // rango style
  "range.pointer.color": "#fff",
  "range.bar.color": "#666",
  "range.subbar.color": "#d1d1d1",
  "range.disabledPointer.color": "#414141",
  "range.disabledBar.color": "#282828",
  "range.disabledSubbar.color": "#414141",
  "range.value.color": "#fff",
  "range.value.fontWeight": "lighter",
  "range.value.fontSize": "11px",
  "range.value.border": "1px solid #353535",
  "range.value.backgroundColor": "#151515",
  "range.title.color": "#fff",
  "range.title.fontWeight": "lighter",
  // colorpicker style
  "colorpicker.button.border": "1px solid #1e1e1e",
  "colorpicker.title.color": "#fff"
};
const locale_zh = {
  Crop: "裁剪",
  ZoomIn: "放大",
  ZoomOut: "缩小",
  DeleteAll: "全部删除",
  Delete: "删除",
  Undo: "撤销",
  Redo: "反撤销",
  Reset: "重置",
  Flip: "镜像",
  Rotate: "旋转",
  Draw: "画",
  Shape: "形状标注",
  Icon: "图标标注",
  Text: "文字标注",
  Mask: "遮罩",
  Filter: "滤镜",
  Bold: "加粗",
  Italic: "斜体",
  Underline: "下划线",
  Left: "左对齐",
  Center: "居中",
  Right: "右对齐",
  Color: "颜色",
  "Text size": "字体大小",
  Custom: "自定义",
  Square: "正方形",
  Apply: "应用",
  Cancel: "取消",
  "Flip X": "X 轴",
  "Flip Y": "Y 轴",
  Range: "区间",
  Stroke: "描边",
  Fill: "填充",
  Circle: "圆",
  Triangle: "三角",
  Rectangle: "矩形",
  Free: "曲线",
  Straight: "直线",
  Arrow: "箭头",
  "Arrow-2": "箭头2",
  "Arrow-3": "箭头3",
  "Star-1": "星星1",
  "Star-2": "星星2",
  Polygon: "多边形",
  Location: "定位",
  Heart: "心形",
  Bubble: "气泡",
  "Custom icon": "自定义图标",
  "Load Mask Image": "加载蒙层图片",
  Grayscale: "灰度",
  Blur: "模糊",
  Sharpen: "锐化",
  Emboss: "浮雕",
  "Remove White": "除去白色",
  Distance: "距离",
  Brightness: "亮度",
  Noise: "噪音",
  "Color Filter": "彩色滤镜",
  Sepia: "棕色",
  Sepia2: "棕色2",
  Invert: "负片",
  Pixelate: "像素化",
  Threshold: "阈值",
  Tint: "色调",
  Multiply: "正片叠底",
  Blend: "混合色",
  Hand: '选中',
  History: '操作历史',
  Load: '加载',
  Resize: '缩放'
};
let flag = true;
export default {
  mixins: [indexMixin],
  components: {
    splitPane, volumesDetailDisabled,
    'tui-image-editor': ImageEditor,
    returnButton: () => import("@/components/customButton/returnButton"),
    taggingButton
  },
  data() {
    return {
      useDefaultUI: true,
      id: this.$route.query.message.id,
      type: this.$route.query.message.type,
      dangHao: this.$route.query.message.dangHao,
      propsData: this.$route.query.message,
      formId: this.$route.query.message.formId,
      code: this.$route.query.message.code,
      rtype: this.$route.query.message.rtype,
      ajFormId: this.$route.query.message.ajFormId,
      ajFondId: this.$route.query.message.ajFondId,
      index: this.$route.query.index,
      ajRow: this.$route.query.ajRow,
      fileRow: '',
      imgOptions: {
        includeUI: {
          loadImage: {
            path: sampleImage,
            name: '测试1'
          },
          locale: locale_zh,
          theme: customTheme,
          menuBarPosition: 'bottom'
        },
        cssMaxWidth: 1920,
        cssMaxHeight: 1080,
      },
      ImgData: [],
      archiveData: [],
      loading: false,
      Page: {
        size: 30,
        index: 0,
        total: 0
      },
      pathJpg: '',
      pathHDJpg: '',
      indexOfd: '',
      volumesForm: {},
      midImgSize: 74,
      rightContentSize: 0,
      contentSize: false,
      isImgReplace: false,
      fileName: '',
      flowType: '',
      flowStringName: ''
    }
  },

  created() {
    document.addEventListener('keydown', this.handleKeyDown)

  },
  methods: {
    $init() {
      this.id = this.$route.query.message.id
      this.type = this.$route.query.message.type
      this.dangHao = this.$route.query.message.dangHao
      this.propsData = this.$route.query.message
      this.formId = this.$route.query.message.formId
      this.code = this.$route.query.message.code
      this.rtype = this.$route.query.message.rtype
      this.ajFormId = this.$route.query.message.ajFormId
      this.ajFondId = this.$route.query.message.ajFondId
      this.index = this.$route.query.index
      this.ajRow = this.$route.query.ajRow
      this.flowPath()
      this.volumesForm = Object.assign({}, this.ajRow)
      this.imgLoadData(0, this.formId, this.id, this.type)
    },
    handleKeyDown(e) {
      var key = window.event.keyCode ? window.event.keyCode : window.event.which
      if (event.ctrlKey && key === 83) {
        if (flag) {
          //存放需要调用的方法
          console.log("我是快捷键")
          flag = false
          this.$message({
            message: '我是快捷键',
            type: 'success'
          });
          setTimeout(
              () => flag = true,
              1200
          );
        }
        e.preventDefault() //取消浏览器原有的ctrl+s操作
      }
    },
    convert(v) {
      //等接口通了之后再正常使用
      this.isImgReplace = true
      this.$post('/dishi/test', {
        inputPath: this.pathJpg,
        outPath: this.pathHDJpg,
        type: v
      }).then(({data}) => {
        // if (data.success) {
        //   this.fileName = this.pathHDJpg.substring(this.pathHDJpg.lastIndexOf("\\"))
        //   this.isImgReplace = true
        // }
        // else {
        //   this.$message.error(data.message)
        // }
      })
      /*async submit() {
          this.$post('/register/updateStatus', Object.assign(this.$route.query._query, {
              type: this.$route.query.type,
              id: this.id,
          })).then(({data}) => {
              if (data.success) {
                  this.id = ''
                  this.$message.success('提交成功，请在操作页查看结果！')
                  this.loadOne()
              } else {
                  this.$message.error(data.message)
              }
          })
      },*/
    },
    confirm() {
      this.$refs.tuiImageEditor.invoke('loadImageFromURL', this.pathHDJpg + "?temp=" + Math.random(), this.fileName);
      this.isImgReplace = false
    },
    keys() {
      var that = this;
      document.onkeydown = function (e) {
        let key = window.event.keyCode;
        if (key == 37) {
          that.keyLeft();
        } else if (key == 38) {
          that.keyUp();
          return false; //有上下滚动条的时候，不向上滚动
        } else if (key == 39) {
          that.keyRight();
        } else if (key == 40) {
          that.keyDown();
          return false; //有上下滚动条的时候，不向上滚动
        }
      };
    },
    elTag() {
      this.url1 = ''
      this.imgLoadData(0, this.formId, this.id, this.type)
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
        if (this.flag){
          this.flag = false
          this.back()
        }
        this.flag = false
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
          await this.lurArchive(this.archiveData[0])
          await this.judge(this.archiveData[0])
          this.index = page.index
          this.flag = false
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
    //切换档案执行方法
    lurArchive(row) {
      this.id = row.id
      this.dangHao = row.archival_code
      this.imgLoadData(0, this.formId, this.id, this.type)
    },
    back(){
      this.$router.push({path:"/processing",selectFond:this.$route.query.message.FondId})
    },
    //加载图像数据
    async imgLoadData(index, formId, id, type) {
      this.loading = true
      let params = Object.assign({}, {pageIndex: index, formDefinitionId: formId, id: id, type: type})
      let path = '/processing/findImgPage'
      if (type == '2') {
        path = '/processing/findImgPageByVId'
      }
      const {data} = await this.$post(path, params)
      if (data.success) {
        this.ImgData = data.data.data
        this.Page.index = data.data.start / data.data.size + 1
        this.Page.size = data.data.size
        this.Page.total = data.data.total
        if (this.ImgData.length > 0) {
          this.fileRow = this.ImgData[0]
          this.pathJpg = this.ImgData[0].filePath
          this.pathHDJpg = this.pathJpg.substring(0, this.pathJpg.indexOf("\\"))
              + this.pathJpg.substring(this.pathJpg.indexOf("\\", this.pathJpg.indexOf("\\") + 1));
          this.indexOfd = this.ImgData[0].index
          this.$refs.tuiImageEditor.invoke('loadImageFromURL', this.ImgData[0].filePath + "?temp=" + Math.random(), this.ImgData[0].fileName)
        }
      } else {
        this.$message.error(data.message)
      }
      this.loading = false
    },
    //点击左侧图像显示在编辑器内
    imgOpenDetails(row) {
      this.index2Picture = row.index
      this.indexOfd = row.index
      this.show = false
      this.imgOptions.includeUI.loadImage.path = row.filePath
      this.pathJpg = row.filePath
      this.pathHDJpg = this.pathJpg.substring(0, this.pathJpg.indexOf("\\")) + this.pathJpg.substring(this.pathJpg.indexOf("\\", this.pathJpg.indexOf("\\") + 1));
      this.imgOptions.includeUI.loadImage.name = row.fileName
      this.$refs.tuiImageEditor.invoke('loadImageFromURL', row.filePath + "?temp=" + Math.random(), row.fileName);
      this.show = true
      this.fileRow = row
    },
    //保存图像最后结果
    async uploadImg() {
      this.loading = true
      const base64String = await this.$refs.tuiImageEditor.editorInstance.toDataURL();
      let params = {
        contents: base64String
      }
      const {data} = await this.$post('uploadfiles/updateFileImg', Object.assign(this.fileRow, params))
      if (data.success) {
        this.$refs.tuiImageEditor.invoke('loadImageFromURL', this.fileRow.filePath + "?temp=" + Math.random(), this.fileRow.fileName);
        this.$message.success("保存成功！")
      } else {
        this.$message.warning(data.message.replace("服务器错误：", ""))
      }
      this.loading = false
    },

    lsubmit: function () {
      if (this.ImgData.length > 0) {
        if (this.index2Picture != 0) {
          this.index2Picture--
          this.$refs.tuiImageEditor.invoke('loadImageFromURL', this.ImgData[this.index2Picture].filePath, 1)
          this.$refs.table2Img.setCurrentRow(this.ImgData[this.index2Picture]);
        }
      }
    },
    rsubmit: function () {
      if (this.ImgData.length > 0) {
        if (this.index2Picture < this.ImgData.length - 1) {
          this.index2Picture++
          this.$refs.tuiImageEditor.invoke('loadImageFromURL', this.ImgData[this.index2Picture].filePath + "?temp=" + Math.random(), 1)
          this.$refs.table2Img.setCurrentRow(this.ImgData[this.index2Picture]);
        }
      }
    },
    onAddText(res) {
      console.group('addText');
      console.log('Client Position : ', res.clientPosition);
      console.log('Origin Position : ', res.originPosition);
      console.groupEnd();
    },
    onObjectMoved(res) {
      console.group('objectMoved');
      console.log('Left : ', res.left);
      console.log('Top : ', res.top);
      console.groupEnd();
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
        type: this.rtype,
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

    ZoomOut() {
      let GetDiv = document.getElementById("imgOptions");
      let oldWidth = GetDiv.cssMaxWidth;
      let oldHeight = GetDiv.cssMaxHeight;

      console.log(oldWidth)
      console.log(oldHeight)
    },
    //查看详情默认开启
    detail() {
      this.contentSize = !this.contentSize
      if (!this.contentSize) {
        this.rightContentSize = 0
        this.midImgSize = 74
      } else {
        this.rightContentSize = 30
        this.midImgSize = 45
      }
    },
  },
  destroyed() {
    document.removeEventListener('keydown', this.handleKeyDown)
  },

}
</script>

<style lang="scss" scoped>
.imageEditorApp {
  width: 100%;
  height: 100%;
}

.gutter gutter-horizontal {
  height: 0px;
}

.detailBtn {
  height: 50%;
  display: block;
  bottom: 3px;
  right: 3px;
  position: fixed;
}
</style>
