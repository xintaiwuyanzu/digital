<template>
    <section>
        <el-table :data="resources" row-key="id" border default-expand-all height="100%" class="resourceSelect">
            <el-table-column prop="label" label="资源名称" header-align="center" show-overflow-tooltip min-width="100px"/>
            <el-table-column label="权限" width="50" align="center">
                <template slot-scope="scope">
                    <el-checkbox
                            :indeterminate="matcher[scope.row.id]?matcher[scope.row.id].length!==parts.length:false"
                            :value="!!matcher[scope.row.id]"
                            @change="v=>changAll(scope.row.id,v)"/>
                </template>
            </el-table-column>
            <el-table-column label="功能" header-align="center" v-if="parts.length>0">
                <el-table-column :label="part.label" v-for="part in parts"
                                 :key="part.code"
                                 :width="part.label.length*25"
                                 align="center">
                    <template slot-scope="scope">
                        <el-checkbox @change="v=>change(scope.row.id,v,part.code)"
                                     :value=" matcher[scope.row.id]?!!(matcher[scope.row.id].indexOf(part.code)+1):false"/>
                    </template>
                </el-table-column>
            </el-table-column>
        </el-table>
    </section>
</template>
<script>

    /**
     * 权限编辑组件
     */
    export default {
        props: {
            resources: {type: Array, default: () => []},
            //parts: {type: Array, default: () => []},
            code: {type: String, default: ''}
        },
        data() {
            return {
                parts: [{code: 'test', label: '测试'}],
                matcher: {}
            }
        },
        methods: {
            $init() {
                this.matcher = {}
                if (this.code) {
                    this.code.trim().split(";")
                        .forEach(c => {
                            if (c) {
                                const arr = c.trim().split(':')
                                this.matcher[arr[0].trim()] = arr.length > 1 ? arr[1].split(',').map(v => v.trim()) : []
                            }
                        })
                }
            },
            changAll(id, t) {
                if (t) {
                    this.$set(this.matcher, id, this.parts.map(p => p.code))
                } else {
                    this.$delete(this.matcher, id)
                }
            },
            change(id, t, p) {
                const arr = this.matcher[id]
                if (t) {
                    if (arr) {
                        if (arr.indexOf(p) < 0) {
                            this.$set(arr, arr.length, p)
                        }
                    } else {
                        this.$set(this.matcher, id, [p])
                    }
                } else {
                    if (arr) {
                        const index = arr.indexOf(p);
                        if (index >= 0) {
                            this.$delete(arr, index)
                        }
                        if (arr.length === 0) {
                            this.$delete(this.matcher, id)
                        }
                    }
                }
            },
            getCode() {
                return Object.entries(this.matcher)
                    .map(([key, value]) => value.length > 0 ? [key, value.join(',')].join(':') : key)
                    .join(";")
            }
        }
    }
</script>
<style lang="scss">
    .resourceSelect {
        line-height: normal;

        th {
            padding: 0px;
        }
    }
</style>
