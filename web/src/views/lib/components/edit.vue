<template>
    <section>
        <el-link type="primary" @click="edit">编辑</el-link>
    </section>
</template>

<script>
    import abstractColumnComponent from "./abstractColumnComponent";

    export default {
        extends: abstractColumnComponent,
        name: 'edit',
        methods: {
            edit() {
                this.eventBus.$emit('edit', this.row)
            }
        }
    }
</script>
