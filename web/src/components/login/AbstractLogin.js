import util from "@dr/framework/src/components/login/util";
import {SM4} from 'gm-crypto'

/**
 * 登录父类，存储所有登录相关的方法
 */
export default {
    data() {
        const loginType = process.env.NODE_ENV === 'production' ? 'bsp' : 'default'
        return {
            dataLoading: false,
            title: document.title,
            //登录校验地址
            validatePath: 'login/validate',
            //登录表单对象
            loginForm: {
                loginType,
                //用户名
                username: '',
                //密码
                password: '',
                //记住密码
                rememberMe: false
            }
        }
    },
    methods: {
        /**
         * 登录方法
         * @param username
         * @param password
         */
        async doLogin({username, password}) {
            if (username && password) {
                this.dataLoading = true
                const {data} = await this.$post(this.validatePath, {
                    username: this.securityPassword(username),
                    password: this.securityPassword(password),
                    loginType: this.loginForm.loginType
                })
                if (data.success) {
                    //设置前端缓存
                    util.setToken(data.data)
                    this.$message({duration: 1000, message: '登录成功！', type: 'success'})
                    await this.$router.push('/main')
                } else {
                    this.$message.error(data.message)
                }
                this.dataLoading = false
            } else {
                this.$message.error('请输入用户名或密码！')
            }
        },
        /**
         * 加密密码
         * @param password
         * @returns {string}
         */
        securityPassword(password) {
            const sm4Key = '617263686976655f66757a686f756c63'
            return SM4.encrypt(password, sm4Key, {
                mode: SM4.constants.ECB,
                inputEncoding: 'utf-8',
                outputEncoding: 'hex'
            })
        },
    }
}