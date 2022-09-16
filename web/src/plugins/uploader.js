import utils from '@dr/auto/lib/utils'

/**
 * vue-uploader 懒加载
 * @param vue
 */
export default (vue) => {
    vue.component('uploader', utils.makeSync(import('vue-simple-uploader/src/components/uploader')))
    vue.component('uploader-btn', utils.makeSync(import('vue-simple-uploader/src/components/btn')))
    vue.component('uploader-drop', utils.makeSync(import('vue-simple-uploader/src/components/drop')))
    vue.component('uploader-unsupport', utils.makeSync(import('vue-simple-uploader/src/components/unsupport')))
    vue.component('uploader-list', utils.makeSync(import('vue-simple-uploader/src/components/list')))
    vue.component('uploader-files', utils.makeSync(import('vue-simple-uploader/src/components/files')))
    vue.component('uploader-file', utils.makeSync(import('vue-simple-uploader/src/components/file')))
}
