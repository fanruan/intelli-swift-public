// see http://vuejs-templates.github.io/webpack for documentation.
var path = require('path')
var swiftBaseUrl = 'http://localhost:8080/swift'
module.exports = {
    build: {
        env: require('./prod.env'),
        index: path.resolve(__dirname, 'D:/swift-new/target/main/static/index.html'),
        assetsRoot: path.resolve(__dirname, 'D:/swift-new/target/main/static'),
        assetsSubDirectory: 'static',
        assetsPublicPath: '/vue-admin/',
        productionSourceMap: true,
        productionGzip: false,
        productionGzipExtensions: ['js', 'css'],
        bundleAnalyzerReport: process.env.npm_config_report
    },
    dev: {
        env: require('./dev.env'),
        port: 8888,
        autoOpenBrowser: true,
        assetsSubDirectory: 'static',
        assetsPublicPath: '/',
        proxyTable: {
            '/api/user/login': {
                target: swiftBaseUrl,
                changeOrigin: true,
                pathRewrite: {
                    "^/api/user/login": "/api/user/login"
                }
            },
            '/api/table/query': {
                target: swiftBaseUrl,
                changeOrigin: true,
                pathRewrite: {
                    "^/api/table/query": "/api/table/query"
                }
            },
            '/api/table/create': {
                target: swiftBaseUrl,
                changeOrigin: true,
                pathRewrite: {
                    "^/api/table/create": "/api/table/create"
                }
            },
            '/api/service/query': {
                target: swiftBaseUrl,
                changeOrigin: true,
                pathRewrite: {
                    "^/api/service/query": "/api/service/query"
                }
            },
            '/api/segment/query': {
                target: swiftBaseUrl,
                changeOrigin: true,
                pathRewrite: {
                    "^/api/segment/query": "/api/segment/query"
                }
            },
            '/api/segment/location/query': {
                target: swiftBaseUrl,
                changeOrigin: true,
                pathRewrite: {
                    "^/api/segment/location/query": "/api/segment/location/query"
                }
            }
        },
        cssSourceMap: false
    }
}
