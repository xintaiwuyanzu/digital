<template>
    <section>
        <el-button type="primary" size="mini" @click="disassembly"> 识别并拆件</el-button>
    </section>
</template>

<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: "threeInOne",
        data() {
            return {}
        },

        methods: {
            hook() {
                const _query = this.eventBus.getQueryByQueryType('query')
                this.$confirm('此操作将生成对应tif文件的jpg文件,' +
                    '对应jpg的txt文本和对应档案的文件结构。,是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    const data = this.$post('threeInOne/dataCleaning',
                        Object.assign(
                            _query,
                            {registerId: this.fond.registerId}),
                        {timeout: 20000})
                    if (data.data.success) {
                        this.$message.success('原文正在拆分中，请稍后查看！')
                    } else {
                        this.$message.success(data.data.message)
                    }
                })
            },
            disassembly(){
                const _query = this.eventBus.getQueryByQueryType('query')
                this.$confirm('此操作将生成对应tif文件的jpg文件,' +
                    '对应jpg的txt文本和对应档案的文件结构。,是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$post('ocr/batchChaiJIan',
                        Object.assign(
                            _query,
                            {registerId: this.fond.registerId}),
                    ).then((data)=>{
                        if (data.data&&data.data.success) {
                            this.$message.success('原文正在拆分中，请稍后查看！')
                        }
                    })


                })
            }
        },
    }
</script>