<template>
    <el-select :value="id" style="width: 100%" :placeholder="placeholder" @change="change" clearable filterable>
        <el-option
                v-for="item in config"
                :key="item.id"
                :label="item.name"
                :value="item.id">
        </el-option>
    </el-select>
</template>

<script>
    export default {
        props: {
            id: {type: String},
            placeholder: {type: String}
        },
        model: {event: 'selected', prop: 'id'},
        data() {
            return {
                config: [],
                label: ''
            }
        },
        methods: {
            change(v) {
                if (v) {
                    this.label = this.config.find(d => d.id === v).name
                } else {
                    this.label = ''
                }
                this.$emit('selected', v)
            }
        },
        mounted() {
            this.$http.post('/z3950/page')
                .then(({data}) => {
                    if (data.success) {
                        this.config = data.data.data
                    }
                })
        }
    }
</script>
