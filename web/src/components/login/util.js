import jsCookie from 'js-cookie'

/**
 * cookie的默认路径
 * @type {string}
 */
const defaultPath = process.env.BASE_URL
jsCookie.defaults = {path: defaultPath}
const AUTH_KEY = '$auth'
const DAUTH_KEY = 'dauth'

export default {
    /**
     *获取登录token
     */
    getToken() {
        return this.getCookie(AUTH_KEY)
    },
    /**
     * 设置登录token
     */
    setToken(token) {
        this.setCookie(AUTH_KEY, token)
        this.setCookie(DAUTH_KEY, token)
    },
    /**
     * 清空登录token
     */
    cleanToken() {
        this.cleanCookie(AUTH_KEY)
        this.cleanCookie(DAUTH_KEY)
    },
    /**
     *获取前端缓存
     */
    getCookie(key) {
        return jsCookie.get(key)
    },
    /**
     * 设置前端缓存
     */
    setCookie(key, value) {
        jsCookie.set(key, value)
    },
    cleanCookie(key) {
        jsCookie.remove(key)
    }
}
