<template>
    <section>
        <el-link type="primary" @click="volumeEntry">查看</el-link>
    </section>
</template>

<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        name: 'look',
        methods: {
            volumeEntry() {
                let type = 1;
                if (this.parentIndex) {
                    type = 2
                    this.message.formId = this.childrenIndex.formId;
                    this.message.ajFormId = this.formId;
                } else {
                    this.message.formId = this.formId;
                    this.message.ajFormId = this.formId;
                }
                this.message.id = this.row.id;
                this.message.type = type;
                this.$router.push({
                    path: '../../register/register/inEntry',
                    query: {message: this.message}
                })
            }
        }
    }
</script>