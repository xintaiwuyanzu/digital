import vue from 'vue'
import lib from '@dr/auto/lib'
import './styles/archiveLib.scss'
import EVueContextmenu from 'e-vue-contextmenu'
import VueSplit from 'vue-split-panel'
import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'

vue.use(Viewer)
vue.use(VueSplit)
vue.use(EVueContextmenu)
vue.prototype.setCookie = function (c_name, value, expiredays) {
    var exdate = new Date()
    exdate.setDate(exdate.getDate() + expiredays)
    document.cookie = c_name + "=" + escape(value) +
        ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString())
};

//获取cookie
vue.prototype.getCookie = function (c_name) {
    if (document.cookie.length > 0) {
        var c_start = document.cookie.indexOf(c_name + "=")
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1
            var c_end = document.cookie.indexOf(";", c_start)
            if (c_end == -1) c_end = document.cookie.length
            return unescape(document.cookie.substring(c_start, c_end))
        }
    }
    return ""
};

vue.prototype.clearCookie = function (c_name) {
    this.setCookie(c_name, '', -1)
}

Viewer.setDefaults({
    Options: {
        'inline': true, // 启用 inline 模式
        'button': true, // 显示右上角关闭按钮
        'navbar': true, // 显示缩略图导航
        'title': true, // 显示当前图片的标题
        'toolbar': true, // 显示工具栏
        'tooltip': true, // 显示缩放百分比
        'movable': true, // 图片是否可移动
        'zoomable': true, // 图片是否可缩放
        'rotatable': true, // 图片是否可旋转
        'scalable': true, // 图片是否可翻转
        'transition': true, // 使用 CSS3 过度
        'fullscreen': true, // 播放时是否全屏
        'keyboard': true, // 是否支持键盘
        'url': 'data-source' // 设置大图片的 url
    }
})
lib.start({vue, zIndex: 5000})
