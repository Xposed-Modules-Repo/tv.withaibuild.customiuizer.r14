# Changelog

本文件只记录 LSPosed 模块仓库中的用户可见版本变化。完整提交历史、工程说明与历史
Release 归档见[个人维护仓库](https://github.com/tomthenpc/customiuizer-a14)。

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
