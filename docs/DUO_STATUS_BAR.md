# Duo 三合一状态栏

正式构建版本：`r14.21.10` / versionCode `219`。稳定源码基线：`e4daedd4d9c0c5d72abef16521f2eb6e59d4b71c`（r14.21.9），保留后续的状态栏、设置、应用列表与计步修复。按用户要求交付本地正式签名包，目标 HyperOS 1 实机验收稍后进行。

入口：系统 → 状态栏 → Duo 三合一状态栏。默认关闭；开启、关闭、修改百分比或最大尺寸后需重启系统界面。

外圈显示电量，顶部显示百分比或充电闪电，中间显示 Wi-Fi，下方四点显示默认数据卡的蜂窝强度。充电为绿色、省电为黄色、低于 20% 为红色，其余跟随原生电池文字颜色。Wi-Fi 已连接但网络未验证时保留 `!` 提示；飞行模式关闭蜂窝点，仍可显示重新开启的 Wi-Fi。

本实现参考 [Duo Status Bar](https://github.com/kvmy666/duoStatusBar/tree/b7201384cc04c152576553d50d8fe5ddd4430cb0) 的[几何说明](https://github.com/kvmy666/duoStatusBar/blob/b7201384cc04c152576553d50d8fe5ddd4430cb0/docs/DESIGN-duo.md)，并核对其实际效果图。该项目的[参考说明](https://github.com/kvmy666/duoStatusBar/blob/b7201384cc04c152576553d50d8fe5ddd4430cb0/docs/references.md) 指向 [Status Trio](https://github.com/lingyired/status-trio)。这里独立编写 Canvas 绘制和 HyperOS 1 的集成，不引入其 Rive、Compose、字体、媒体资源或动画系统。

## 开销与所有权

- 沿用 Feature 的 SystemUI / PACKAGE_READY 安装入口和 API 101 安全边界；关闭时不解析 ROM 字段，不创建业务 Hook、视图、Handler、Receiver 或 Observer。
- 开启后旁路读取原生 BatteryController、WifiSignalController、MobileSignalController 和 CallbackHandler 的现有事件，不改参数、原生状态或调用次数。
- 字段与方法只在安装时解析。电池和网络使用原子可见的整数快照，双卡状态仅有两个物理槽位；未知状态不冒充零强度。
- 相同状态不触发刷新；后台事件至多排入一个待处理的主线程刷新。无轮询、动画循环、新服务或线程。最后一个宿主释放时移除待处理刷新。
- 四个宿主弱引用、每个宿主最多八个信号视图。原生图标组替换时移除失效引用；视图脱离时恢复并释放原生子视图。
- 绘制使用预建 Paint、Path 与固定几何。帧内不创建字符串、数组或集合；数字字符串只在电量变化时更新。
- 原生电池宿主承载图标，继承原有位置、可见性、淡出与父布局；尺寸遵守原有行高和测量约束，不修改 Insets、窗口高度或 Dynamic Island。

## 回退与验证边界

必需的类、方法、字段不匹配时，不安装替换；部分安装失败时撤销已经安装的 Hook。电量、Wi-Fi 或默认数据卡状态不完整时，保留原生图标。普通运行或绘制异常使该进程的 Duo 回退到原生显示；致命错误按项目规则继续抛出。

自动测试覆盖未知状态、双卡默认数据卡切换、无服务、订阅移除与旧回调、飞行模式与 Wi-Fi 共存、网络未验证、重复事件、并发写入、圆环映射、原生可见性恢复和默认关闭的 Feature 接线。

本地 JVM、静态门禁、lint 与正式签名构建不能替代目标 HyperOS 1 实机验收。当前未连接设备，不宣称 ROM ABI、显示效果、实际内存、CPU、耗电或帧率已获实机通过。

实机收口需检查：桌面 / 应用 / 锁屏、深浅背景、横屏、双排状态栏、信号左移、默认数据卡切换 / 拔卡、Wi-Fi 断连 / 无互联网、飞行模式 / 充电 / 省电、关闭后重启恢复原生显示，以及重复解锁 / 旋转后的视图数量、内存和 SystemUI 日志。
