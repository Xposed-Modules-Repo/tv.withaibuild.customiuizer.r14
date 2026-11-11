# 米客 A14｜CustoMIUIzer A14

简体中文 | [English](README_EN.md)

面向 **HyperOS 1 / Android 14 / arm64-v8a** 的系统界面与交互定制模块，需要 **libxposed API 102 或更高版本**。

[下载正式 APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/305-r14.27.5/CustoMIUIzer-A14-r14.27.5.apk) · [发行说明](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/305-r14.27.5) · [更新记录](CHANGELOG_CN.md)

[Telegram 交流群](https://t.me/customiuizerA14)

## 当前正式版

`r14.27.5` · versionCode `305` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.5.apk` (6,265,729 bytes)

SHA-256: `99bb4e3b2094d49b6f90bb8c3c719bdf5b5fd78d16e78815a74f06439dbb0645`

[源码提交](https://github.com/thetvplus/customiuizer-a14/commit/1a64b974b732105837adf6d7f8f9d3756ec12205): `1a64b974b732105837adf6d7f8f9d3756ec12205`

## 本版变化

自 r14.27.3 起的变更：

- 新增可选的 iOS 12 中央音量框，支持上下微调并自动收起静音、勿扰快捷按钮。音量百分比可用浮层、Toast 胶囊或动态岛显示。
- 新增可选的 iOS 电池、五圆点蜂窝信号和通知栏媒体卡片样式。
- 改善 DECK 打开后排卡片时的前卡退场、卡片补位和窗口阴影交接，修复返回桌面时 Dock 背景和模糊恢复。
- 整理设置分组和搜索路径，保留首页四个入口，兼容现有偏好和备份。
- 更新后重启桌面和系统界面，使相关样式生效。

## 安装与兼容

1. 下载并安装正式 APK；同签名覆盖安装保留应用数据。
2. 在 Xposed 框架中启用模块并选择所需作用域。
3. 重启相关进程或设备，按设置页提示启用功能。

其他 Android / HyperOS 大版本不在支持范围内。请勿与上游或其他 CustoMIUIzer 派生模块同时启用。

[使用说明与支持](https://github.com/thetvplus/customiuizer-a14/tree/1a64b974b732105837adf6d7f8f9d3756ec12205) · [兼容矩阵](https://github.com/thetvplus/customiuizer-a14/blob/1a64b974b732105837adf6d7f8f9d3756ec12205/COMPATIBILITY.md)

此仓库只维护正式下载说明。开发分支的未发布内容不代表本 APK 的功能或验收结果。
