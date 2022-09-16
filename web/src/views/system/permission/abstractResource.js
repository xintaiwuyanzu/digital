export default {
    data() {
        return {
            providers: [],
            groups: [],
            resources: [],
            parts: []
        }
    },
    watch: {
        /**
         * 监听资源类型
         * @param v
         * @param o
         */
        resourceType(v, o) {
            if (v && v !== o) {
                this.loadGroup(v)
            }
        },
        groupId(v, o) {
            if (v && v !== o && this.resourceType) {
                this.loadResource(this.resourceType, v)
            }
        }
    },
    methods: {
        /**
         * 获取所有资源类型
         * @returns {Promise<void>}
         */
        async loadResourceProvider() {
            const {data} = await this.$post('/sysResource/resourceProvider')
            this.providers = data.data
        },
        /**
         * 获取指定类型资源的所有分组
         * @param type
         * @returns {Promise<void>}
         */
        async loadGroup(type) {
            if (type !== '*') {
                const {data} = await this.$post('/sysResource/resourceGroup', {type})
                data.data.push({id: '*', name: '所有分组'})
                this.groups = data.data
                const parts = await this.$post('/sysResource/resourcePart', {type})
                this.parts = parts.data.data
            } else {
                this.groups = []
            }
            this.resources = []
        },
        /**
         * 获取指定类型和分组的所有资源
         *获取指定类型
         * @param type
         * @param group
         * @returns {Promise<void>}
         */
        async loadResource(type, group = 'default') {
            const {data} = await this.$post('/sysResource/resourceTree', {type, group})
            this.resources = data.data ? data.data : []
        },
        getProvideLabel(type) {
            const pro = this.providers.find(p => p.type === type)
            return pro ? pro.name : type
        }
    }
}
