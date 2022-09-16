import {http} from '@dr/framework/src/plugins/http'
import {parse} from "qs";
import util from "@dr/framework/src/components/login/util";
import {Message} from 'element-ui'

export default (vue, router) => {
    const back = () => {
        Message.error("单点登录失败！，返回上一页面")
        setTimeout(() => {
            history.back()
        }, 2000)
    }

    router.beforeEach((to, from, next) => {
        if (to.path === '/login') {
            //如果是访问登录页面，并且带有token参数，则拦截并且使用sso登录
            const query = parse(location.search ? location.search.substr(1, location.search.length) : '')
            if (query.token) {
                //如果首页是redirect跳转到login的
                http().post('ssoLogin/validate', {token: query.token})
                    .then(({data}) => {
                        if (data.success) {
                            util.setToken(data.data)
                            next('/home')
                        } else {
                            back()
                        }
                    })
                    .catch(() => back())
            } else {
                next()
            }
        } else if (from.path === '/login' && to.path === '/main/') {
            console.warn('这里强制重定向到home页面，console中会报错，无需理会')
            //如果是从登录页面跳转首页，直接跳转首页
            next('/home')
        } else if (from.path === '/' && to.path !== '/home') {
            if (process.env.NODE_ENV !== "development") {
                next('/home')
            } else {
                next()
            }
        } else {
            next()
        }
    })
}