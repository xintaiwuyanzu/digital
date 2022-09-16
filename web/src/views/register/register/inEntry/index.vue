<template>
    <section>
        <nac-info title="查看" back>
        </nac-info>
        <div class="index_main" v-loading="loading">
            <el-row style="height: 100%">
                <Split style="height: 100%;" :gutterSize="6">
                    <SplitArea :size="20">
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
                                </el-table>
                            </div>
                        </div>
                    </SplitArea>
                    <SplitArea :size="70">
                        <div class="imageEditorApp">
                            <div class="imageEditorApp">
                                <viewer :images="images" style="height: 200px">
                                    <img v-for="src in images" :src="src" :key="src" width="800"/>
                                </viewer>
                            </div>
                        </div>
                    </SplitArea>
                </Split>
            </el-row>
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

    export default {
        mixins: [indexMixin],
        components: {splitPane},
        data() {
            return {
                id: this.$route.query.message.id,
                type: this.$route.query.message.type,
                message: this.$route.query.message,
                formId: this.$route.query.message.formId,
                ajFormId: this.$route.query.message.ajFormId,
                ImgData: [],
                images: [],
                page: {
                    size: 15,
                    index: 0,
                    total: 0
                },
            }
        },
        methods: {
            //点击图片
            async imgOpenDetails(row) {
                this.images = []
                this.images.push(row.filePath + "?temp=" + Math.random())
            },
            $init() {
                this.imgLoadData(0, this.formId, this.id, this.type)
            },
            //加载图像数据
            async imgLoadData(index, formId, id, type) {
                this.loading = true
                let params = Object.assign({}, {pageIndex: index, formDefinitionId: formId, id: id, type: type})
                let path = '/processing/findImgPage'
                if (this.type == '2') {
                    path = '/processing/findImgPageByVId'
                    params = Object.assign({}, {
                        formDefinitionId: formId,
                        id: id,
                        type: type,
                        page: false,
                    })
                } else {
                    params = Object.assign({}, {
                        formDefinitionId: this.ajFormId,
                        id: id,
                        type: type,
                        page: false,
                    })
                }
                const {data} = await this.$post(path, params)
                if (data.success) {
                    this.ImgData = data.data
                    if (this.ImgData.length > 0) {
                        this.images.push(this.ImgData[0].filePath + "?temp=" + Math.random())
                    }
                } else {
                    this.$message.error(data.message)
                }
                this.loading = false
            },
        },
    }
</script>

<style lang="scss" scoped>
    .imageEditorApp {
        width: 100%;
        height: 100%;
    }
</style>
