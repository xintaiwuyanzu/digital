<template>
    <section>
        <nac-info title="操作进度">
            <span style="padding: 50px">当前选中批次：<span style="color: red">{{batchName}}</span></span>
          <el-button type="primary" v-on:click="totalPercentage()">总进度条</el-button>
          <el-button type="primary" v-on:click="refresh()">刷新</el-button>
        </nac-info>
        <el-container>
            <el-aside class="el-aside" width="12%" style="margin-top: 1%;overflow-x: hidden;">
                <el-card class="box-card">
                    <el-scrollbar style="height: 100%;width: 100%" :native="false">
                        <div v-for="o in data" :key="o.id" class="text item">
                            <el-popover
                                    placement="right"
                                    width="100"
                                    trigger="hover"
                                    :content="o.batch_name">
                                <el-button slot="reference" type="text"
                                           style="width: 100px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap "
                                           @click="showData(o.id,o.batch_name)">{{ o.batch_name }}
                                </el-button>
                            </el-popover>
                        </div>
                    </el-scrollbar>
                </el-card>
            </el-aside>
            <el-main width="80%" style="margin-top:-0.5%">
                <!--放组件-->
                <percentags-tags ref="batchData" v-if="show" :batchId="this.batchId"></percentags-tags>
            </el-main>
        </el-container>
    </section>
</template>

<script>
    import percentagsTags from "@/views/percentag/data";

    export default {
        name: "data",
        data() {
            return {
                show: false,
                batchId: '',
                data: [],
                batchName: ''
            }
        },
        components: {
            percentagsTags
        },
        methods: {
            $init() {
                this.show = false
                this.loadData();
            },

            loadData() {
                this.loading = true
                this.$http.post('/register/page', {page: false}).then(({data}) => {
                    if (data.success) {
                        this.data = data.data
                        this.showData(this.data[0].id, this.data[0].batch_name);
                    } else {
                        this.$message.error(data.message)
                    }
                    this.loading = false
                })
            },

            refresh() {
                this.$nextTick(() => {
                    this.$refs.batchData.$init()
                })
            },
          totalPercentage(){
            this.batchName = "总进度条"
            this.show = true
            this.$nextTick(() => {
              this.$refs.batchData.totalPercentage()
            })
          },
            showData(batchId, batchName) {
                this.batchName = batchName
                this.batchId = batchId
                this.show = true
                this.$nextTick(() => {
                    this.$refs.batchData.refresh()
                })
            },
        }
    }
</script>

<style scoped>
    .el-aside {
        height: 500px;
    }

    .textShow {
        font-size: 12px;
        color: red;
        font-weight: bold;
    }

    .text {
        background-color: rgba(235, 238, 245, 0.8);
        font-size: 12px;
        text-overflow: ellipsis;
        overflow: hidden;
    }


    .item {
        padding: 18px 0;
        text-overflow: ellipsis;
        margin-bottom: 2%;
        padding: 10px;
        border-radius: 10px;
    }

    .el-main {
        padding: 20px 10px 10px;

    }

    .box-card {
        width: 100%;
    }
</style>