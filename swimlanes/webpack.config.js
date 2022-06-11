const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");

const distDirPath = path.join(__dirname, "dist");

module.exports = {
  devServer: {
    contentBase: distDirPath,
    disableHostCheck: true,
    hot: false,
    port: 8081,
  },
  devtool: "inline-source-map",
  entry: {
    app: "./src/index.ts",
  },
  externals: {
    jquery: "jQuery",
    swa: "swa",
    teamwork: "teamwork",
    gadgets: "gadgets",
    utils: "TBUtils",
    react: "react",
    "react-dom": "react-dom",
    graphql: "graphql",
    TB: "TB",
  },
  mode: "development",
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: "ts-loader",
        exclude: /node_modules/,
      },
    ],
  },
  output: {
    libraryTarget: "window",
    path: distDirPath,
    filename: "bundle.js",
  },
  resolve: {
    extensions: [".ts", ".tsx", ".js", ".json"],
  },
  plugins: [
    new CopyWebpackPlugin({
      patterns: [
        {
          from: path.resolve(__dirname, "public"),
          to: ".",
        },
      ],
    }),
  ],
};
