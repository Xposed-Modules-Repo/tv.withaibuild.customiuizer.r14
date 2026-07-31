# 米客 A14 Kotlin 重构｜HyperOS 1 / Android 14｜支持 API 101/102

简体中文 | [English](README_EN.md)

面向 **HyperOS 1 / Android 14** 的系统界面与交互定制模块。

## 当前正式版

| 项目           | 值                                 |
| ------------ | --------------------------------- |
| 版本           | `r14.15.3`                        |
| versionCode  | `191`                             |
| 系统           | HyperOS 1 / Android 14            |
| ABI          | `arm64-v8a`                       |
| 应用 ID        | `tv.withaibuild.customiuizer.r14` |
| libxposed    | API 101–102                       |
| APK          | `CustoMIUIzer-A14-r14.15.3.apk`   |
| 大小           | `3107265` bytes                   |
| APK SHA-256  | `F7AB34722B0193DD8C97DF0146C968E5A6064655AD497061E902CD1545375E7E` |
| 签名证书 SHA-256 | `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70` |

## r14.15.3 更新重点

* 恢复此前误删的 `system` 作用域，修复 `system_server` Hook 未加载；
* 加固 Global Actions 和其他 Receiver 的异常隔离、信任验证、生命周期及并发注册；
* 改进 Hook 加载诊断和兼容信息；
* 状态栏网速粗体保留系统字体，并增加双排行距调整；
* 补充网速设置本地化提示；
* 修复设置文本样式和 About 页面换行。

## 适用环境

* HyperOS 1 / Android 14（SDK 34）；
* `arm64-v8a`；
* 实现 libxposed API 101 或 API 102 的 LSPosed / Vector；
* 不支持 Android 15 或 Android 16。

功能可用性取决于设备 ROM 和系统应用版本。请勿与上游版本或其他 CustoMIUIzer 派生模块同时启用。

## 重要升级说明

`r14.13.5` 及之后的新签名版本可直接覆盖安装。

从 `r14.12.0` 或更早版本升级时，由于旧签名私钥已经遗失，需要先备份设置并记录作用域，然后卸载旧版、安装新版本、重新启用作用域、恢复设置并完整重启。

## 安装

1. 从本仓库 Release 下载 APK；
2. 核对 APK SHA-256；
3. 安装 APK；
4. 在 LSPosed / Vector 中启用模块；
5. 确认推荐作用域包含 `system`；
6. 打开一次模块设置并完整重启设备。

API 101 管理器可能因为模块声明 `targetApiVersion=102` 显示面向较新 API 的提示。该提示不等于加载失败，应以目标进程日志和实际功能为准。

## 验证状态

本版本已完成正式 Release APK 构建、签名、zipalign、包信息和 Xposed 元数据基础检查，并确认 APK 中包含 `system` 与 `android` 作用域。

本次发布未执行完整单元测试、Lint、工程 Audit、ADB regression 或全部功能实机回归。

## 源码与反馈

源码、完整 changelog 和工程说明：

`https://github.com/tomthenpc/customiuizer-a14`

反馈时请提供模块版本、设备、ROM、框架版本、实际作用域、复现步骤，以及相关 `system_server`、SystemUI 或 Launcher 日志。
