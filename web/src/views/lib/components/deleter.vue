<template>
    <section>
        <el-button type="danger" v-if="currentSelect.length>0" @click="onDelete">彻底删除</el-button>
    </section>
</template>

<script>
    import abstractComponent from "./abstractComponent";

    export default {
        extends: abstractComponent,
        name: 'deleter',
        methods: {
            onDelete() {
                this.eventBus.$emit('delete')
            }
        }
    }
</script>
