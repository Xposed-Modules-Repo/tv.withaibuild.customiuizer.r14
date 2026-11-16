# 米客 A14｜CustoMIUIzer A14

简体中文 | [English](README_EN.md)

面向 **HyperOS 1 / Android 14 / arm64-v8a** 的系统界面与交互定制模块，需要 **libxposed API 102 或更高版本**。

[下载正式 APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/310-r14.27.9/CustoMIUIzer-A14-r14.27.9.apk) · [发行说明](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/310-r14.27.9) · [更新记录](CHANGELOG_CN.md)

[Telegram 交流群](https://t.me/customiuizerA14)

## 当前正式版

`r14.27.9` · versionCode `310` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.9.apk` (6,293,117 bytes)

SHA-256: `d2bd1f64612eb2d5b4184a8ee87907cadd902631a6767520b725cf8efc138bfa`

[源码提交](https://github.com/thetvplus/customiuizer-a14/commit/9224160f6b13be43a9dd4a6da8a1ff92a0e795c0): `9224160f6b13be43a9dd4a6da8a1ff92a0e795c0`

## 本版变化

自 r14.26.11 起的变更：

本摘要对照 r14.27.7 的实际实现。累计说明见[双语更新记录](https://github.com/thetvplus/customiuizer-a14/blob/r14.27.9/CHANGELOG.md#r14279--2026-11-16)。

- 改善 DECK 在横屏点击卡片时的窗口坐标和遮挡交接。打开最前卡时跳过多余的窗口裁切。
- iOS 手势指示条接入桌面及系统界面的原生横屏手势条。窗口尺寸尚未稳定时保留系统绘制。
- 改善 Dock 从最近任务返回后的模糊材料准备，减少动态岛更新状态栏遮挡时的计算。
- 自动定位在延迟关闭期间保留触发关闭的任务身份，避免其他任务移除干扰最后一张卡片的判断。
- 精简音量面板自动隐藏及通知自动展开的运行状态，保留原有设置和行为。
- 升级后重启桌面和系统界面。首次启用自动定位需要重启 Android。

## 安装与兼容

1. 下载并安装正式 APK；同签名覆盖安装保留应用数据。
2. 在 Xposed 框架中启用模块并选择所需作用域。
3. 重启相关进程或设备，按设置页提示启用功能。

其他 Android / HyperOS 大版本不在支持范围内。请勿与上游或其他 CustoMIUIzer 派生模块同时启用。

[本版使用说明](https://github.com/thetvplus/customiuizer-a14/blob/9224160f6b13be43a9dd4a6da8a1ff92a0e795c0/README.md) · [本版兼容矩阵](https://github.com/thetvplus/customiuizer-a14/blob/9224160f6b13be43a9dd4a6da8a1ff92a0e795c0/COMPATIBILITY.md) · [项目主页](https://github.com/thetvplus/customiuizer-a14)

此仓库只维护正式下载说明。开发分支的未发布内容不代表本 APK 的功能或验收结果。
