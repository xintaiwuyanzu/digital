<template>
    <section>
        <el-link type="primary" @click="initial">查 看</el-link>
    </section>
</template>

<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        name: 'inIndex',
        methods: {
            initial() {
                let type = 1;
                if (this.parentIndex) {
                    type = 2
                    this.message.formId = this.childrenIndex.formId;
                    this.message.ajFormId = this.formId;
                    this.message.ajFondId = this.fond.id;
                } else {
                    this.message.formId = this.formId;
                    this.message.ajFormId = this.formId;
                    this.message.ajFondId = this.fond.id;
                }
                this.message.rtype = this.row.status_info//
                this.message.id = this.row.id;
                this.message.dangHao = this.row.archival_code;
                this.message.fondId = this.fond.id;
                this.message.registerId = this.fond.registerId;
                this.message.type = type;
                this.message.code = this.category.code;
                const nowPage = (parseInt(this.eventBus.page.index) - 1) * parseInt(this.eventBus.page.size)
                const _query = this.eventBus.getQueryByQueryType('query')
                const t = this.IfStatus(this.eventBus.defaultForm.status_info)
                this.$router.push({
                    path: '../../over/seeOfd',
                    query: {
                        message: this.message,
                        ajRow: this.row,
                        type: t,
                        index: parseInt(this.row.$index) + nowPage + 1,
                        _query
                    }
                })
            }
        }
    }
</script>