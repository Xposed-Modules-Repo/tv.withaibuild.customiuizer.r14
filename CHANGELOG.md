# Changelog

本文件只记录 LSPosed 模块仓库中的用户可见版本变化。完整提交历史、工程说明与历史
Release 归档见[个人维护仓库](https://github.com/tomthenpc/customiuizer-a14)。

## r14.13.4

- 完善应用内语言、About 页面、日间/夜间主题和设置页面重建；
- 修复搜索返回状态及 Root 重启功能的异步执行和错误反馈；
- 修复 SystemUI 状态栏文本图标长期持有失效 View 的问题；
- 优化资源 Hook 高频未命中路径，减少装箱、反射和无效解析；
- 修复 Kotlin 迁移后的 CPU thermal zone 扫描控制流；
- 移除 pair 配置解析中的重复 Regex 编译；
- 改进 RemotePreferences 空快照和监听器注册状态；
- 同一 APK 保持 libxposed API 101/102 兼容；
- Release 通过 R8、资源压缩、zipalign 和 APK v2 签名检查。

### 重要升级说明

`r14.12.0` 及更早公开版本使用的旧签名私钥已经遗失。`r14.13.4` 使用新的正式签名，
不能直接覆盖安装旧版本。

升级前必须先备份模块设置并记录 LSPosed/Vector 作用域，然后卸载旧版本、安装新版本、
重新启用作用域、恢复设置并完整重启设备。

- APK：`CustoMIUIzer-A14-r14.13.4.apk`
- SHA-256：`E8A2BD362C0540972441B8D1DE0BCACE8FE85FEF71F31406F3B4DA1A4027D26C`
- 签名证书 SHA-256：`C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

下载：[182-r14.13.4](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/182-r14.13.4)

## r14.12.0

- 同一 APK 支持实现 libxposed API 101 或 API 102 的框架；
- 核心 Hook、设置 UI 和工具代码完成保守 Kotlin 迁移；
- 修复 SystemUI 重建后的重复 Hook、Receiver、Observer、Coroutine 和动画任务；
- 收敛高频 Hook 与绘制路径中的重复反射、资源查询、格式化和临时对象；
- 功能关闭时尽量不注册对应 Hook 或长期监听；
- Release 通过 R8、资源压缩、zipalign 和 APK v2 签名检查；
- API 101 实机完整重启日志未发现模块相关崩溃、ANR、Hook 或链接错误；
- API 102 工程兼容已验证，仍需对应框架环境的独立实机验证。

下载：[174-r14.12.0](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/174-r14.12.0)

## 维护版边界

- 仅维护 HyperOS 1 / Android 14（SDK 34）和 `arm64-v8a`；
- 包名为 `tv.withaibuild.customiuizer.r14`，与上游安装身份分离；
- `MonwF/customiuizer@v24.10.12` 只作为 Android 14 功能语义参考；
- 不支持 Android 15、Android 16，也不启用 API 102 Hot Reload；
- 性能和省电收益取决于 ROM、功能组合和使用方式，不声明未经同设备测量的固定比例。
