let libs = []
let persons = []
export default {
    props: {
        /**
         *是否显示搜索框，
         * index的列表可以服用给别的模块做弹出选择功能，
         * 这个时候可能不需要显示搜索框
         */
        showSearch: {default: true},
        /**
         * 查询条件
         */
        query: {
            type: Object,
            default() {
                return {}
            }
        }
    },
    watch: {
        query(v) {
            if (v) {
                this.loadMenus(v)
            }
        }
    },
    filters: {
        getLibOne(v) {
            if (v) {
                const lib = libs.find(d => d.id === v)
                if (lib) {
                    return lib.fondName
                }
            }
        },
        getPersonName(v) {
            if (v) {
                let person = persons.find(d => d.id === v)
                if (person) {
                    return person.userName
                }
            }
        },
    },
    data() {
        return {
            data: [],
            page: {
                size: 15,
                index: 0,
                total: 0
            },
            selectIds: [],
            loading: false,
            notFeeUser: ['0059']
        }
    },
    methods: {
        /**
         * 编辑或新增表单
         * @param form
         */
        editForm(form) {
            if (this.$refs.form) {
                this.$refs.form.editForm(form)
            }
        },
        /**
         *根据传入的参数加载数据
         * @param params
         */
        loadData(params, useSearchForm) {
            this.loading = true
            if (useSearchForm && this.$refs.form && this.$refs.form.getSearchForm) {
                params = this.$refs.form.getSearchForm(params)
            }
            this.$http.post(this.apiPath() + '/page', params).then(({data}) => {
                if (data && data.success) {
                    this.data = data.data.data
                    this.page.index = data.data.start / data.data.size + 1
                    this.page.size = data.data.size
                    this.page.total = data.data.total
                }
                this.loading = false
            })
        },
        /**
         *删除指定的数据
         * @param id 指定的数据或者true
         */
        remove(id) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                let ids = []
                if (id === true) {
                    ids = [...this.selectIds]
                } else if (id) {
                    ids = [id]
                }
                if (ids.length > 0) {
                    this.loading = true
                    this.$http.post(this.apiPath() + '/delete', {id: ids.join(',')})
                        .then(({data}) => {
                            if (data.success) {
                                this.$message.success('删除成功！')
                                this.selectIds = []
                                this.loadData()
                            } else {
                                this.$message.error(data.message)
                                this.loading = false
                            }
                        })
                } else {
                    this.$message.warning('请选择要删除的数据列！')
                }
            })
        },
        handleTableSelect(row) {
            this.selectIds = row.map(r => r.id)
        },
        isEmpty(v) {
            switch (typeof v) {
                case 'undefined':
                    return true;
                case 'string':
                    if (v.replace(/(^[ \t\n\r]*)|([ \t\n\r]*$)/g, '').length == 0) return true;
                    break;
                case 'boolean':
                    if (!v) return true;
                    break;
                case 'number':
                    if (0 === v || isNaN(v)) return true;
                    break;
                case 'object':
                    if (null === v || v.length === 0) return true;
                    for (const i in v) {
                        return false;
                    }
                    return true;
            }
            return false;
        }
    }
}
