module.exports = {
    lintOnSave: false,
    devServer: {
        //花生壳映射内网配置项 disableHostCheck+host
        //disableHostCheck: true,
        //host: '192.168.1.142',
        proxy: {
            '/api': {
                target: 'http://localhost:8087/api',
                pathRewrite: {'^/api': '/'}
            }, '/upload': {
                target: 'http://localhost:8087/upload',
                pathRewrite: {'^/upload': '/'}
            }, '/filePath': {
                target: 'http://localhost:8087/filePath',
                pathRewrite: {'^/filePath': '/'}
            }, '/pdfPath': {
                target: 'http://localhost:8087/pdfPath',
                pathRewrite: {'^/pdfPath': '/'}
            }, '/ofdPath': {
                target: 'http://localhost:8087/ofdPath',
                pathRewrite: {'^/ofdPath': '/'}
            }, '/splitPath': {
                target: 'http://localhost:8087/splitPath',
                pathRewrite: {'^/splitPath': '/'}
            }, '/fileThumbnailPath': {
                target: 'http://localhost:8087/fileThumbnailPath',
                pathRewrite: {'^/fileThumbnailPath': '/'}
            }, '/download': {
                target: 'http://localhost:8087/download',
                pathRewrite: {'^/download': '/'}
            }
        }
    },
    pluginOptions: {
        dr: {
            limit: {
                maxChunks: 400
            }
        }
    }
}
