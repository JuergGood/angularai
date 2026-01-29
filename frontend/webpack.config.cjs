module.exports = {
  devtool: 'source-map',
  module: {
    rules: [
      {
        test: /\.(js|ts)$/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: [
              ['@babel/preset-env', { targets: { browsers: ['last 2 versions'] } }],
              '@babel/preset-typescript',
            ],
            plugins: [
              ['@babel/plugin-proposal-decorators', { legacy: true }],
              ['@babel/plugin-transform-class-properties', { loose: true }],
              ['@babel/plugin-transform-private-methods', { loose: true }],
              ['@babel/plugin-transform-private-property-in-object', { loose: true }],
              'babel-plugin-istanbul'
            ],
          },
        },
        enforce: 'post',
        include: /src/,
        exclude: [
          /node_modules/,
          /cypress/,
          /\.spec\.ts$/
        ],
      },
    ],
  },
};
