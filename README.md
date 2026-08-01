# CustoMIUIzer A14｜HyperOS 1 / Android 14

简体中文 | [English](README_EN.md)

CustoMIUIzer A14 是面向 HyperOS 1 / Android 14 的系统界面与交互定制模块。

## 当前版本

| 项目 | 值 |
| --- | --- |
| 版本 | `r14.16.1` |
| versionCode | `192` |
| 应用 ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.16.1.apk` |
| 大小 | `3369409` bytes |
| APK SHA-256 | `F213BA3F939FAA7BD12150D75A538529E9517D9CE865B6611C7A3C93C8370258` |
| 签名证书 SHA-256 | `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70` |

## 兼容范围与要求

- HyperOS 1 / Android 14（SDK 34）；
- `arm64-v8a` 设备；
- 实现 libxposed API 101 或 102 的 LSPosed / Vector；
- 不支持 Android 15、Android 16 或其他 MIUI / HyperOS 大版本；
- 请勿与上游版或其他 CustoMIUIzer 派生模块同时启用。

API 101 管理器可能因为模块声明 `targetApiVersion=102` 显示面向较新 API 的提示，该提示本身不代表加载失败。

## 主要功能

- 状态栏图标、电池、信号、网速、日期与温度；
- 控制中心、通知、音量、亮度、锁屏、充电和媒体界面；
- Launcher、最近任务、文件夹、图标与桌面手势；
- 导航栏、按键、自定义动作、电源菜单和系统动画；
- 应用、权限、安装、分享、隐私应用和应用锁行为。

`r14.16.1` 重点提升运行稳定性、内存释放和 SystemUI 高频路径性能，并让设置开关在点击后立即显示状态反馈。详细变化见 [CHANGELOG.md](CHANGELOG.md)。

## 安装与升级

1. 从本仓库 Release 下载 APK 并核对 SHA-256；
2. 安装 APK，在 LSPosed / Vector 中启用模块；
3. 确认推荐作用域包含 `system`；
4. 打开一次模块设置并完整重启设备。

`r14.13.5` 及之后使用当前签名的版本可以直接覆盖安装。从 `r14.12.0` 或更早版本升级时，旧签名无法兼容：请先备份设置和记录作用域，再卸载旧版、安装新版、恢复设置并完整重启。

## 风险提示

模块通过 Hook 修改系统进程，功能可用性取决于设备 ROM 与系统应用版本。ROM 更新可能改变类、方法或资源结构，异常时请先停用相关功能并保留日志。

本版本已通过完整离线门禁、正式 Release/R8 构建、版本、签名、zipalign、`debuggable=false` 和 Xposed 元数据校验；新增变化尚未完成全部功能逐项实机行为验证，不代表所有设备和功能均已 `DEVICE_VERIFIED`。

源码与问题反馈：<https://github.com/tomthenpc/customiuizer-a14>
