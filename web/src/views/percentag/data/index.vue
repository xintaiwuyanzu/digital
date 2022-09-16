<template>
    <section>
        <div class="index_main">
            <el-row style="margin-top: 40px">
                <el-col :span="4" style="text-align: center;font-size: 15px;margin-top: 4px;line-height:25px;">
                    <span>TIF转换进度：</span>
                </el-col>
                <el-col :span="17">
                    <el-progress :text-inside="true" :stroke-width="30" :percentage="this.jpg"
                                 :color="customColors"></el-progress>
                </el-col>
                <el-col :span="3" style="text-align: center;font-size: 15px;margin-top: 4px">
                    <span>{{ data.jpgFinish }}/{{ data.all }}份</span>
                    <br/><span>预计:{{ data.jpgNeedTime }}分钟</span>
                </el-col>
            </el-row>
            <el-row style="margin-top: 40px">
                <el-col :span="4" style="text-align: center;font-size: 15px;margin-top: 4px;line-height:25px;">
                    <span>OCR识别进度：</span>
                </el-col>
                <el-col :span="17">
                    <el-progress :text-inside="true" :stroke-width="30" :percentage="ocr"
                                 :color="customColors"></el-progress>
                </el-col>
                <el-col :span="3" style="text-align: center;font-size: 15px;margin-top: 4px">
                    <span>{{ data.ocrFinish }}/{{ data.all }}份</span>
                    <br/><span>预计:{{ data.ocrNeedTime }}分钟</span>
                </el-col>
            </el-row>
            <el-row style="margin-top: 40px">
                <el-col :span="4" style="text-align: center;font-size: 15px;margin-top: 4px;line-height:25px;">
                    <span>档案拆件进度：</span>
                </el-col>
                <el-col :span="17">
                    <el-progress :text-inside="true" :stroke-width="30" :percentage="split"
                                 :color="customColors"></el-progress>
                </el-col>
                <el-col :span="3" style="text-align: center;font-size: 15px;margin-top: 4px">
                    <span>{{ data.splitFinish }}/{{ data.all }}份</span>
                    <br/><span>预计:{{ data.splitNeedTime }}分钟</span>
                </el-col>
            </el-row>
            <el-row style="margin-top: 40px">
                <el-col :span="4" style="text-align: center;font-size: 15px;line-height:25px;margin-top: 4px">
                    <span>OFD转换进度：</span>
                </el-col>
                <el-col :span="17">
                    <el-progress :text-inside="true" :stroke-width="30" :percentage="ofd"
                                 :color="customColors"></el-progress>
                </el-col>
                <el-col :span="3" style="text-align: center;font-size: 15px;margin-top: 4px;">
                    <span>{{ data.ofdFinish }}/{{ data.all }}份</span>
                    <br/><span>预计:{{data.ofdNeedTime }}分钟</span>
                </el-col>
            </el-row>
            <el-row style="margin-top: 40px">
                <el-col :span="4" style="text-align: center;font-size: 15px;line-height:25px;margin-top: 6px">
                    <span>封包上传进度：</span>
                </el-col>
                <el-col :span="17">
                    <el-progress :text-inside="true" :stroke-width="30" :percentage="compose"
                                 :color="customColors"></el-progress>
                </el-col>
                <el-col :span="3" style="text-align: center;font-size: 15px;margin-top: 4px">
                    <span>{{ data.packetFinish }}/{{ data.all }}份</span>
                     <br/><span>预计:{{data.packetNeedTime }}分钟</span>
                </el-col>
            </el-row>
        </div>
    </section>
</template>

<script>
    import indexMixin from '@/util/indexMixin'

    export default {
        name: "index",
        mixins: [indexMixin],
        props: {
            batchId: String
        },
        data() {
            return {
                jpg: 0.0,
                ocr: 0.0,
                split: 0.0,
                ofd: 0.0,
                compose: 0.0,
                businessId: '',
                customColor: '#409eff',
                customColors: [
                    {color: '#f56c6c', percentage: 20},
                    {color: '#e6a23c', percentage: 40},
                    {color: '#5cb87a', percentage: 60},
                    {color: '#1989fa', percentage: 80},
                    {color: '#6f7ad3', percentage: 100}
                ],
                data: {}
            }
        },
        methods: {
            $init() {
                this.businessId = this.batchId
                let params = Object.assign({}, {
                    businessId: this.businessId
                })
                this.$http.post('/register/getPercentage', params).then(({data}) => {
                    if (data.success) {
                        if (data.data.all) {
                            this.data = data.data
                            this.jpg = parseFloat(this.toPercent(data.data.jpgFinish).toFixed(2))
                            this.ocr = parseFloat(this.toPercent(data.data.ocrFinish).toFixed(2))
                            this.split = parseFloat(this.toPercent(data.data.splitFinish).toFixed(2))
                            this.ofd = parseFloat(this.toPercent(data.data.ofdFinish).toFixed(2))
                            this.compose = parseFloat(this.toPercent(data.data.packetFinish).toFixed(2))
                        } else {
                            this.data = data.data
                            this.data.all = 0
                        }
                    } else {
                        this.$message.error(data.message)
                    }
                })
            },
            refresh() {
                this.jpg = 0.0
                this.ocr = 0.0
                this.split = 0.0
                this.ofd = 0.0
                this.compose = 0.0
                this.data = []
                this.businessId = ''
                this.businessId = this.batchId
                let params = Object.assign({}, {
                    businessId: this.businessId
                })
                this.$http.post('/register/getPercentage', params).then(({data}) => {
                    if (data.success) {
                        if (data.data.all) {
                            this.data = data.data
                            this.jpg = parseFloat(this.toPercent(data.data.jpgFinish).toFixed(2))
                            this.ocr = parseFloat(this.toPercent(data.data.ocrFinish).toFixed(2))
                            this.split = parseFloat(this.toPercent(data.data.splitFinish).toFixed(2))
                            this.ofd = parseFloat(this.toPercent(data.data.ofdFinish).toFixed(2))
                            this.compose = parseFloat(this.toPercent(data.data.packetFinish).toFixed(2))
                        } else {
                            this.data = data.data
                            this.data.all = 0.0
                            this.jpg = 0.0
                            this.ocr = 0.0
                            this.split = 0.0
                            this.ofd = 0.0
                            this.compose = 0.0
                        }
                    } else {
                        this.$message.error(data.message)
                    }
                })
            },
          totalPercentage() {
            this.jpg = 0.0
            this.ocr = 0.0
            this.split = 0.0
            this.ofd = 0.0
            this.compose = 0.0
            this.data = []
            this.$http.post('/register/getTotalPercentage').then(({data}) => {
              if (data.success) {
                if (data.data.all) {
                  this.data = data.data
                  this.jpg = parseFloat(this.toPercent(data.data.jpgFinish).toFixed(2))
                  this.ocr = parseFloat(this.toPercent(data.data.ocrFinish).toFixed(2))
                  this.split = parseFloat(this.toPercent(data.data.splitFinish).toFixed(2))
                  this.ofd = parseFloat(this.toPercent(data.data.ofdFinish).toFixed(2))
                  this.compose = parseFloat(this.toPercent(data.data.packetFinish).toFixed(2))
                } else {
                  this.data = data.data
                  this.data.all = 0.0
                  this.jpg = 0.0
                  this.ocr = 0.0
                  this.split = 0.0
                  this.ofd = 0.0
                  this.compose = 0.0
                }
              } else {
                this.$message.error(data.message)
              }
            })
          },
            toPercent(v) {
                return parseInt(v) * 100 / parseInt(this.data.all)
            },
        }
    }
</script>

<style scoped>

</style>
