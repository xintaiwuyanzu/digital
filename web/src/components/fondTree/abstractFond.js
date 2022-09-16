export default {
    props: {
        /**
         * 全宗Id，传全宗Id则只显示指定全宗的树
         *
         *  不传则多一个下拉选择，选择全宗
         */
        fondId: {type: String, required: false},
        /**
         * 加载数据是否带有权限
         */
        withPermission: {type: Boolean, default: false},
        /**
         * 是否显示选择框
         */
        showCheck: {default: false}
    },
    data() {
        return {loading: false}
    },
    computed: {
        //根据权限不同获取数据的路径不同
        url() {
            return '/register/page'
            //return `/register/${this.withPermission ? 'personResource' : 'page'}`
        }
    }
}
