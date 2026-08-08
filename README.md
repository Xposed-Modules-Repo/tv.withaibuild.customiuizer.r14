# CustoMIUIzer A14｜HyperOS 1 / Android 14

简体中文 | [English](README_EN.md)

CustoMIUIzer A14 是面向 HyperOS 1 / Android 14 的系统界面与交互定制模块。

## 当前版本

| 项目 | 值 |
| --- | --- |
| 版本 | `r14.18.2` |
| versionCode | `195` |
| 应用 ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.18.2.apk` |
| 大小 | `3468849` bytes |
| APK SHA-256 | `77F868590C631271251991EDEBF066919460E2F1DA955EFDC10271207EAF3E77` |

## 兼容范围与要求

- HyperOS 1 / Android 14（SDK 34）；
- `arm64-v8a` 设备；
- libxposed API 101/102；
- 不支持 Android 15、Android 16 或其他 MIUI / HyperOS 大版本；
- 请勿与上游版或其他 CustoMIUIzer 派生模块同时启用。

## 主要功能

- 状态栏图标、电池、信号、网速、日期与温度；
- 控制中心、通知、音量、亮度、锁屏、充电和媒体界面；
- Launcher、最近任务、文件夹、图标与桌面手势；
- 导航栏、按键、自定义动作、电源菜单和系统动画；
- 应用、权限、安装、分享、隐私应用和应用锁行为。

`r14.18.2` 重点改进通知弹窗和锁屏功能的兼容性与失败保护、修复充电信息字号被系统样式重置的问题，并简化 SystemUI / system_server 生命周期 Hook。详细变化见 [CHANGELOG_CN.md](CHANGELOG_CN.md)。

## 安装与升级

1. 从本仓库 Release 下载 `CustoMIUIzer-A14-r14.18.2.apk`；
2. 启用模块；
3. 推荐作用域包含 `system`；
4. 完整重启设备。

## 风险提示

模块通过 Hook 修改系统进程，功能可用性取决于设备 ROM 与系统应用版本。ROM 更新可能改变类、方法或资源结构，异常时请先停用相关功能并保留日志。

本版本已通过完整离线门禁、正式 Release/R8 构建、版本、v2 签名、zipalign、`debuggable=false` 和 Xposed 元数据校验，且 Release Candidate 已完成安装并通过实机冒烟验证。功能可用性仍取决于具体 ROM 与系统应用版本，不宣称所有功能或所有设备均已全面 `DEVICE_VERIFIED`。

源码与问题反馈：<https://github.com/tomthenpc/customiuizer-a14>
