# 米客 A14｜CustoMIUIzer A14

简体中文 | [English](README_EN.md)

米客 A14｜CustoMIUIzer A14 是面向 **HyperOS 1 / Android 14** 的系统界面与交互定制模块，仅支持 **arm64-v8a / libxposed API 102**。

r14.23.5 减少设置页重复分组计算，收紧示例文件与 Service 入口，修复原生作用域失败处理。同签名覆盖安装保留应用数据。

源码关于页完整名称统一为「米客 A14｜CustoMIUIzer A14」，短标签保留「米客 A14」。既有正式 APK 不因文案变更重打包；应用内新文案随后续正式构建生效。

[下载最新正式版](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/latest) · [发行记录](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases)

## 当前版本

| 项目 | 值 |
| --- | --- |
| 版本 | `r14.23.5` |
| versionCode | `243` |
| 维护与开发 | `thetvplus` |
| 应用 ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.23.5.apk` |
| 大小 | `3,799,533` bytes |
| APK SHA-256 | `99625206aa8814626da57d4c5448fb05e19d6b2d1da9c34a4cbab6885bfe3c75` |
| 源码 tag | [r14.23.5](https://github.com/thetvplus/customiuizer-a14/tree/r14.23.5) |
| 源码提交 | `04f6204ac7fe2e0286cc6c6d566123f9ec0ba8f0` |

## 兼容范围与要求

- HyperOS 1 / Android 14（SDK 34）；
- `arm64-v8a` 设备；
- libxposed API 102（最低运行基线）；
- 不支持 Android 13、Android 15、Android 16 或其他 MIUI / HyperOS 大版本；
- 请勿与上游版或其他 CustoMIUIzer 派生模块同时启用。

## 主要功能

- 状态栏图标、电池、信号、网速、日期与温度；
- Duo 三合一状态栏：原生绘制，支持短过渡、耳机提示与垂直微调；
- 灵动额头 / 动态岛、USB 默认用途、音量与亮度面板；
- 控制中心、通知、锁屏、充电和媒体界面；
- Launcher、最近任务、文件夹、图标与桌面手势；
- 导航栏、按键、自定义动作、电源菜单和系统动画；
- 应用、权限、安装、分享、隐私应用和应用锁行为。

Duo 默认关闭，修改后重启系统界面。完整变化见 [CHANGELOG_CN.md](CHANGELOG_CN.md)。

## 安装与升级

1. 从本仓库 Release 下载 `CustoMIUIzer-A14-r14.23.5.apk`；
2. 启用模块；
3. 确认作用域包含 `system`、桌面等必要应用；
4. 完整重启设备。

## 风险提示

模块通过 Hook 修改系统进程，功能可用性取决于设备 ROM 与系统应用版本。ROM 更新可能改变类、方法或资源结构，异常时请先停用相关功能并保留日志。

本版完整本地回归及追加定向回归、Release Lint、R8/DEX 与正式签名检查通过。最终 APK 在 fuxi / HyperOS 1 V816.0.7.0.UMCTWXM、Vector 2.2 (3112) / API 102 完成设置导航、服务绑定、权限隔离、六类示例读取及 SystemUI / 桌面重启定向验收。仅测场景内的调用与功能正确性，不宣称实机 CPU、PSS 或功耗收益。

验证仅覆盖记录的设备、ROM 和测试场景，其他 ROM、框架实现、全功能组合及长期运行仍需专项验收。

源码与问题反馈：<https://github.com/thetvplus/customiuizer-a14>
