# 米客 A14｜CustoMIUIzer A14

简体中文 | [English](README_EN.md)

面向 **HyperOS 1 / Android 14 / arm64-v8a** 的系统界面与交互定制模块，需要 **libxposed API 102 或更高版本**。

[下载正式 APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/304-r14.27.3/CustoMIUIzer-A14-r14.27.3.apk) · [发行说明](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/304-r14.27.3) · [更新记录](CHANGELOG_CN.md)

[Telegram 交流群](https://t.me/customiuizerA14)

## 当前正式版

`r14.27.3` · versionCode `304` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.3.apk` (6,160,184 bytes)

SHA-256: `1650c02a2f7ca654b87148a6d40f01356f04b10591609e802061f5da303bb149`

[源码提交](https://github.com/thetvplus/customiuizer-a14/commit/5169eb1c537d639c78fdd84eefdd479f066c4982): `5169eb1c537d639c78fdd84eefdd479f066c4982`

## 本版变化

自 r14.26.11 起的变更：

- 新增可独立开启的 iOS DECK 堆叠任务，支持已适配桌面的横竖屏。
- 改善 DECK 卡片缩放、阴影和点击恢复，修复手势条快速切换时的异常放大。
- 新增 iOS 手势指示条，黑白切换更柔和，功能开关更易搜索。
- 新增 iOS 音量条及默认窄条选项，支持触摸放大、过拖回弹和展开控制。
- 改善动态岛伸缩、内容切换与返回上岛；恢复长岛右侧内容右对齐，保留摄像头避让。
- 更新 Duo 图标、字体与布局，精简设置说明并保留搜索和升级兼容。
- 修复通知提示、控制中心反馈、音量按钮及旧设置页面恢复等问题。
- 更新后重启桌面与系统界面。DECK 需“横向”最近任务；手势条需全面屏手势及可见的系统指示条。

## 安装与兼容

1. 下载并安装正式 APK；同签名覆盖安装保留应用数据。
2. 在 Xposed 框架中启用模块并选择所需作用域。
3. 重启相关进程或设备，按设置页提示启用功能。

其他 Android / HyperOS 大版本不在支持范围内。请勿与上游或其他 CustoMIUIzer 派生模块同时启用。

[使用说明与支持](https://github.com/thetvplus/customiuizer-a14/tree/5169eb1c537d639c78fdd84eefdd479f066c4982) · [兼容矩阵](https://github.com/thetvplus/customiuizer-a14/blob/5169eb1c537d639c78fdd84eefdd479f066c4982/COMPATIBILITY.md)

此仓库只维护正式下载说明。开发分支的未发布内容不代表本 APK 的功能或验收结果。
