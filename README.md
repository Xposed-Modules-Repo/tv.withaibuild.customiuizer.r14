# 米客 A14

[简体中文](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/README.md) |
[English](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/README_EN.md)

面向 **HyperOS 1 / Android 14** 的 CustoMIUIzer Kotlin 重构版 Xposed 模块。

本页面用于 LSPosed 模块仓库展示和下载。源码、完整 changelog、构建说明与工程文档位于
[个人维护仓库](https://github.com/tomthenpc/customiuizer-a14)。

项目谱系：本维护版的最上游为 **Mikanoshi/CustoMIUIzer**，Android 14 功能语义参考
[MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12)。
本仓库不是上述项目的官方发布。

当前版本变化见本仓库的
[CHANGELOG](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/CHANGELOG.md)，
完整工程记录以个人维护仓库为准。

## 适用环境

- HyperOS 1 / Android 14（SDK 34）
- `arm64-v8a`
- 实现现代 libxposed API 101 或 API 102 的框架
- 模块包名：`tv.withaibuild.customiuizer.r14`

不支持 Android 15、Android 16，也不保证其他 MIUI/HyperOS 版本。请勿与上游版或其他
CustoMIUIzer 派生模块同时启用。

## 功能简介

- 状态栏图标、电池、信号、网速、日期和温度；
- 控制中心、音量面板、亮度和通知行为；
- 锁屏、充电信息、媒体界面和快捷操作；
- Launcher、最近任务、文件夹、图标和桌面手势；
- 导航栏、按键、自定义动作、电源菜单和系统动画；
- 应用、权限、安装、分享、隐私应用和应用锁行为。

功能可用性取决于设备 ROM 和系统应用版本。

## r14.13.8 更新重点

- 优化 Hook 进程与设置应用工具代码的边界，减少系统进程加载无关类。
- 清理 GlobalActions 遗留的 6 个转发桩，调用点直接使用实际实现。
- 快速重启 Receiver 不再依赖是否配置自定义动作；未配置任何动作时，应用内“重启系统”
  仍可由 SystemUI 正常接收和执行。
- 区分快速重启广播无人接收与接收端执行失败，不再把后者误报为“未连接 LSPosed 服务”。
- 自定义动作行为保持不变；同一 APK 继续兼容 libxposed API 101/102。
- Release 通过 R8、资源压缩、zipalign 和 APK v2 正式签名检查。

### 验证状态

本版本通过静态门禁（117 文件 / 0 违规）、176 项单元测试、lint 三档 0 errors 与
Debug/Release 构建。

已在 Android 14 / HyperOS 1 与 LSPosed 2.1.1（7790）上完成实机验收：模块在 SystemUI
与 Launcher 正常加载，两次快速重启均完成，日志中 P0/P1 为 0，未发现目标进程崩溃、
Hook 异常或 Receiver 重复注册。

已知问题：系统 Toast 屏蔽仍可能无效，本版本未改动相关逻辑。签名证书与
`r14.13.5`—`r14.13.7` 相同，可直接覆盖安装。

## 本维护版的区别

- 以 `MonwF/customiuizer@v24.10.12` 作为 Android 14 功能语义参考，但不是上游官方发布。
- 使用独立包名、版本线、签名和发布流程，不会覆盖上游安装身份。
- 仅维护 HyperOS 1 / Android 14，不把 Android 15/16 兼容性混入本版本。
- 使用现代 libxposed API 101/102；不依赖 Legacy Xposed Hook API。
- 重点治理长驻系统进程中的生命周期、重复注册、热路径和关闭功能后的额外成本。

## 性能与省电说明

相较早期实现，本版本通过减少无效 Hook、重复监听、后台残留任务、热路径分配和异常重试，
理论上可降低 SystemUI、Launcher 与 `system_server` 的额外 CPU、内存和唤醒开销。
收益取决于启用功能、ROM 和使用方式；未提供未经同设备对照测量的固定百分比。

从 `r14.12.0` 或更早公开版本升级时，请先完成设置备份；由于签名证书已经更换，
必须卸载旧版后再安装。

## 安装

1. 从 [r14.13.8 Release](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/186-r14.13.8)
   下载并安装 APK。
2. 在 LSPosed/Vector 中启用模块并确认推荐作用域。
3. 打开模块设置一次。
4. 完整重启设备。

API 101 管理器可能因为模块声明 `targetApiVersion=102` 显示面向较新 API 的提示。该提示
不等于加载失败，应以目标进程日志和实际功能为准。

APK SHA-256：

`B0E7D4A3CB50E39748531D5B0FD3CB95F81C1F777DDAC9E346B8C8D67B8CBE62`

签名证书 SHA-256：

`C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

## 反馈

请在[源码仓库](https://github.com/tomthenpc/customiuizer-a14)提交问题，并附：

- 模块版本和 APK 来源；
- 设备、ROM 与系统应用版本；
- 框架名称及实际 libxposed API；
- 完整重启后的模块、`system_server`、SystemUI 或 Launcher 日志；
- 可重复的功能开关和操作步骤。
