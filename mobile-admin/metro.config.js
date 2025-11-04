const { getDefaultConfig } = require('expo/metro-config')

const config = getDefaultConfig(__dirname)

config.resolver = {
  ...config.resolver,
  resolveRequest: (context, moduleName, platform) => {
    if (moduleName === 'expo/virtual/rsc') {
      return {
        type: 'empty',
      }
    }
    return context.resolveRequest(context, moduleName, platform)
  },
}

module.exports = config
