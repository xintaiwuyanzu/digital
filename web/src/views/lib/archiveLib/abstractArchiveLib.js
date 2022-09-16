/**
 * 档案各种库的入口，通过参数调用渲染各种库
 */
export default {
    props: {
        //查询参数
        query: {type: Object},
        //是否自动关闭左侧菜单
        autoCloseMenu: {default: true},
    },
    provide() {
        //向所有列表页注入本全局对象
        return {
            libIndex: this,
            category: this.category,
            fond: this.fond,
            register: this.register
        }
    },
    data() {
        return {
            //门类表单对象
            formInfo: {},
            //所有的孩子对象
            childIndexs: {
                //案卷
                PRO: null,
                //文件
                FILE: null
            },
            //门类基本信息
            category: {},
            //缓存全宗信息
            fond: {},
            fondId: '',
            //缓存批次信息
            register: {},
            loading: false,
            hideTree: true
        }
    },
    computed: {
        archives() {
            const {archiveType} = this.category
            if (this.formInfo.id && archiveType) {
                const data = [
                    {title: '', formId: this.formInfo.arcFormId, id: 'ARC'},
                    {title: '', formId: this.formInfo.fileFormId, id: 'FILE'}
                ]
                return data.filter(d => d.formId)
            }
            return []
        }
    },
    methods: {
        async check(v) {
            this.loading = true
            const {data} = await this.$post('/manage/categoryconfig/forms', {categoryId: v.id})
            if (data.success && data.data) {
                this.formInfo = data.data
            } else {
                this.formInfo = {}
            }
            this.category = v.data
            this.fond = v.data
            if (this.fond.registerId) {
                this.$post('/register/selectOneRegister', {id: this.fond.registerId}).then(({data}) => {
                    if (data.success) {
                        this.register = data.data
                    } else {
                        this.$message.error(data.message)
                    }
                })
            }
            this.childIndexs = {}
            this.loading = false
        },
        $init() {
            if (this.autoCloseMenu) {
                //this.$store.commit('closeMenu')
            }
            //页面初始化的时候尝试绑定传参
            if (this.$route.query.registerId) {
                this.fondId = this.$route.query.registerId
            }
        },
        addChild({type, child}) {
            this.$set(this.childIndexs, type, child)
        }
    },
    mounted() {
        //通过事件的方式收集所有child
        this.$on('childIndex', this.addChild)
    }
}
