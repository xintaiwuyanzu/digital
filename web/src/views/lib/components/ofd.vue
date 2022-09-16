<template>
    <section>
        <el-button slot="reference" v-show="this.parentIndex" @click="seeOfd()" type="primary">ofd工具</el-button>
    </section>
</template>
<script>
    import abstractComponent from "./abstractComponent";

    /**
     * 头部搜索按钮
     */
    export default {
        extends: abstractComponent,
        name: "search",
        data() {
            return {
                listFields: [],
                form: {},
                width: "600"
            }
        },
        methods: {
            async seeOfd() {
                window.open("http://localhost:8087/api/download/getUploadDownLoadOfd?type=ofd&fileFullPath=" + '/upload/ofd.msi')
                this.$message.info('请安装ofd查看工具！');
            }
        },
    }
</script>