# CustoMIUIzer A14｜HyperOS 1 / Android 14

简体中文 | [English](README_EN.md)

CustoMIUIzer A14 是面向 HyperOS 1 / Android 14 的系统界面与交互定制模块。

## 当前版本

| 项目 | 值 |
| --- | --- |
| 版本 | `r14.22.5` |
| versionCode | `228` |
| 维护与开发 | `thetvplus` |
| 应用 ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.5.apk` |
| 大小 | `3885184` bytes |
| APK SHA-256 | `B59F25D3CFC5E8DF2C3BF91761E1ED118DF7E5F0B1F4C4398549F81638CF86D8` |

## 兼容范围与要求

- HyperOS 1 / Android 14（SDK 34）；
- `arm64-v8a` 设备；
- libxposed API 101/102；
- 不支持 Android 15、Android 16 或其他 MIUI / HyperOS 大版本；
- 请勿与上游版或其他 CustoMIUIzer 派生模块同时启用。

## 主要功能

- 状态栏图标、电池、信号、网速、日期与温度；
- Duo 三合一状态栏：原生绘制，支持短过渡、耳机提示与垂直微调；
- 灵动额头 / 动态岛、USB 默认用途、音量与亮度面板；
- 控制中心、通知、锁屏、充电和媒体界面；
- Launcher、最近任务、文件夹、图标与桌面手势；
- 导航栏、按键、自定义动作、电源菜单和系统动画；
- 应用、权限、安装、分享、隐私应用和应用锁行为。

`r14.22.3` 新增可选 Duo 三合一状态栏，支持网络类型文字、耳机短提示、自动尺寸和控制中心交接，并修复信号恢复、减少绘图开销。Duo 默认关闭，设置变更后重启系统界面。合并更新见 [CHANGELOG_CN.md](CHANGELOG_CN.md)。

`r14.22.5` 加固主题资源错误处理与控制中心文字着色，简化 CI 重复步骤，并将二维码改为像素完全一致的无损 WebP。正式 APK 比优化前减少 76,703 字节（1.936%），保留必要的依赖、动态信号资源和 R8 优化。

## 安装与升级

1. 从本仓库 Release 下载 `CustoMIUIzer-A14-r14.22.5.apk`；
2. 启用模块；
3. 确认作用域包含 `system`、桌面等必要应用；
4. 完整重启设备。

## 风险提示

模块通过 Hook 修改系统进程，功能可用性取决于设备 ROM 与系统应用版本。ROM 更新可能改变类、方法或资源结构，异常时请先停用相关功能并保留日志。

用户已确认优化前的 r14.22.5 构建通过 HyperOS 1 / Android 14 实机测试。发布前的变化仅涉及二维码无损编码和构建来源标识；最终 APK 已通过本地构建、资源一致性及签名检查。启用后请完整重启。

源码与问题反馈：<https://github.com/thetvplus/customiuizer-a14>
