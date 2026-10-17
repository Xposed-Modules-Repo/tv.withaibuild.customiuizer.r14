# CustoMIUIzer A14｜HyperOS 1 / Android 14

简体中文 | [English](README_EN.md)

CustoMIUIzer A14 是面向 HyperOS 1 / Android 14 的系统界面与交互定制模块。

## 当前版本

| 项目 | 值 |
| --- | --- |
| 预发布 | `r14.20.9` |
| 正式版 | `r14.20.8` |
| versionCode | `206` |
| 应用 ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.20.9.apk` |
| 大小 | `3882834` bytes |
| APK SHA-256 | `BB179572B7CF9FB6D80DC5E078126EAE383375486EF9D93816345CDCC5C1B9DC` |

## 兼容范围与要求

- HyperOS 1 / Android 14（SDK 34）；
- `arm64-v8a` 设备；
- libxposed API 101/102；
- 不支持 Android 15、Android 16 或其他 MIUI / HyperOS 大版本；
- 请勿与上游版或其他 CustoMIUIzer 派生模块同时启用。

## 主要功能

- 状态栏图标、电池、信号、网速、日期与温度；
- 灵动额头 / 动态岛、USB 默认用途、音量与亮度面板；
- 控制中心、通知、锁屏、充电和媒体界面；
- Launcher、最近任务、文件夹、图标与桌面手势；
- 导航栏、按键、自定义动作、电源菜单和系统动画；
- 应用、权限、安装、分享、隐私应用和应用锁行为。

`r14.20.9` 是预发布：system_server 在偏好快照未就绪时不再安装业务功能，SystemUI 快速重启不再跳过 Hook catalog，WindowManager 热路径类型错误不再打穿系统窗口管理，自定义状态栏高度资源替换只进入 android、系统界面和桌面。详细变化见 [CHANGELOG_CN.md](CHANGELOG_CN.md)。

## 安装与升级

1. 从本仓库 Release 下载 `CustoMIUIzer-A14-r14.20.9.apk`；
2. 启用模块；
3. 确认作用域包含 `system`、桌面等必要应用；
4. 完整重启设备。

## 风险提示

模块通过 Hook 修改系统进程，功能可用性取决于设备 ROM 与系统应用版本。ROM 更新可能改变类、方法或资源结构，异常时请先停用相关功能并保留日志。本版本为预发布，当前正式版仍为 `r14.20.8`。

本预发布已通过正式 Release/R8 构建、版本、v2 签名、zipalign、`debuggable=false`、Xposed 元数据与 provenance 校验。启用后请完整重启。

源码与问题反馈：<https://github.com/tomthenpc/customiuizer-a14>
