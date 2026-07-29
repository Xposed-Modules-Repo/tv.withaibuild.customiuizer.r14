# 米客 A14

[简体中文](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/README.md) |
[English](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/README_EN.md)

面向 **HyperOS 1 / Android 14** 的系统界面与交互定制 Xposed 模块。

本页面用于 LSPosed 模块仓库展示和下载。源码、完整 changelog、构建说明与工程文档位于
[个人维护仓库](https://github.com/tomthenpc/customiuizer-a14)。

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

## r14.13.7 更新重点

- **未连接 LSPosed 服务时改的设置不再丢失**：设置应用与服务断开期间，每一次偏好改动过去都
  被直接丢弃，重新连上也不会补发。模块每个宿主进程只读一次快照并据此决定装哪些 hook，因此
  断开期间打开的开关会永久无效 ——「专辑封面设为壁纸」不生效就是这么来的。现在连接建立时做
  一次全量对账，未下发的改动会在对话框里明确告知。
- **快速重启不再被误判为不可用**：该功能是发广播给 SystemUI 里的模块，与设置应用自己有没有
  连上服务无关。改为直接发送有序广播，只有确实无人处理时才提示。
- **列表偏好损坏不再拖垮系统进程**：读取列表类偏好时遇到类型变化或非法字符串会抛异常，而
  调用点在 SystemUI 和 `system_server` 的 hook 里。现在一律回退到默认值。
- **状态栏电池/温度的格式与单位无需重启 SystemUI 即可生效**：ticker 过去一直读 hook 时捕获的
  旧配置。真正无法热更新的两个开关已在界面上标注。
- **锁屏专辑封面的并发与缓存**：快速切歌时可能并行生成多张全屏图；缓存按条数计数（长屏上约
  31 MB）且实际永远不命中。改为代次校验 + 按字节限额 + 正确的缓存键，画质与裁剪行为不变。
- **图标加载队列饱和不再让图标永久空白**。
- 同一 APK 保持 libxposed API 101/102 兼容；
- Release 通过 R8、资源压缩、zipalign 和 APK v2 签名检查。

> 本轮的根因是 LSPosed/Vector 守护进程在设置应用连续快速重启后会停止向模块推送 service
> binder，而 `libxposed-service` 没有任何索取或重试接口。该问题属于框架侧，模块内无法修复；
> 本版本做的是让这个状态不再丢数据、不再误伤其他功能并明确告知用户。分析见源码仓库的
> [LSPOSED_BINDER_DELIVERY.md](https://github.com/tomthenpc/customiuizer-a14/blob/r14.13.7/docs/LSPOSED_BINDER_DELIVERY.md)。

### 验证状态

本版本通过静态门禁（116 文件 / 0 违规）、171 项单元测试、lint 三档 0 errors 与 Debug/Release
构建，**但尚未完成实机验收**。本版本改动了运行在 SystemUI 内的锁屏专辑封面处理器与状态栏
ticker，影响面比 `r14.13.6` 更靠近系统进程；如遇 SystemUI 异常请回退到 `r14.13.6`。
签名证书与 `r14.13.5`、`r14.13.6` 相同，可双向直接覆盖安装。

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

1. 从 [r14.13.7 Release](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/185-r14.13.7)
   下载并安装 APK。
2. 在 LSPosed/Vector 中启用模块并确认推荐作用域。
3. 打开模块设置一次。
4. 完整重启设备。

API 101 管理器可能因为模块声明 `targetApiVersion=102` 显示面向较新 API 的提示。该提示
不等于加载失败，应以目标进程日志和实际功能为准。

APK SHA-256：

`11D01A737BED25C3C4D31153DE22CB918A651D0DD043D0374E2C0E41D32492CC`

签名证书 SHA-256：

`C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

## 反馈

请在[源码仓库](https://github.com/tomthenpc/customiuizer-a14)提交问题，并附：

- 模块版本和 APK 来源；
- 设备、ROM 与系统应用版本；
- 框架名称及实际 libxposed API；
- 完整重启后的模块、`system_server`、SystemUI 或 Launcher 日志；
- 可重复的功能开关和操作步骤。
