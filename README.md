# CustoMIUIzer A14｜HyperOS 1 / Android 14

简体中文 | [English](README_EN.md)

CustoMIUIzer A14 是面向 HyperOS 1 / Android 14 的系统界面与交互定制模块。

## 当前版本

| 项目 | 值 |
| --- | --- |
| 版本 | `r14.22.6` |
| versionCode | `229` |
| 维护与开发 | `thetvplus` |
| 应用 ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.6.apk` |
| 大小 | `3733397` bytes |
| APK SHA-256 | `BFAACA985D2C0F445711463E6381EC4CB5400C0AA9DEA2FE1ACFF72752939192` |

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

`r14.22.6` 减少壁纸回调、温度格式化、备份、搜索和语言列表的重复分配；固定并校验 libxposed 官方依赖，对齐 Fragment KTX。紧凑资源表保留全部资源内容，正式 APK 比 r14.22.5 减少 151,787 字节。构建使用 JDK 25；JVM 21/25 试验未证明稳定运行收益，Android 输出继续保持 17。

## 安装与升级

1. 从本仓库 Release 下载 `CustoMIUIzer-A14-r14.22.6.apk`；
2. 启用模块；
3. 确认作用域包含 `system`、桌面等必要应用；
4. 完整重启设备。

## 风险提示

模块通过 Hook 修改系统进程，功能可用性取决于设备 ROM 与系统应用版本。ROM 更新可能改变类、方法或资源结构，异常时请先停用相关功能并保留日志。

正式 APK 已在 Xiaomi 13 / HyperOS 1 / Android 14 / API 102 上安装、重启并验证模块加载、设置界面及资源一致性，原有设置与作用域保持。API 101 通过静态兼容检查，尚无 API 101 框架实机验收；测试不代表全部功能和 ROM 组合。

源码与问题反馈：<https://github.com/thetvplus/customiuizer-a14>
