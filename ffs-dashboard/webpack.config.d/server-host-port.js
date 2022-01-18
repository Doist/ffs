module.exports = (env) => {
    const webpack = require("webpack")
    const definePlugin = new webpack.DefinePlugin({
        SERVER_HOST: '"' + env.host + '"',
        SERVER_PORT: env.port,
    })
    config.plugins.push(definePlugin)
    return config
}

