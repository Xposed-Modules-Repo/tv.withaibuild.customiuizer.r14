# Changelog

简体中文 | [English](CHANGELOG_EN.md)

## r14.18.0 — 2026-08-06

面向 HyperOS 1 / Android 14（SDK 34）、`arm64-v8a` 与 libxposed API 101/102。

### 核心变化

- 锁屏充电信息新增字号调节；默认保持系统字号，重启 SystemUI 后生效。
- 加固锁屏充电信息初始化与热路径；关闭详情时跳过无效调用，减少重复安装、无效读取和异常回退开销。
- 修复启用状态栏电池或温度信息时可能导致的 SystemUI 崩溃，并加固旧 Handler、过期 View、ROM 字段兼容和自定义图标创建路径。
- 修复左侧状态栏自定义文字图标在深色背景下不可见，补齐 tint 注册、初始同步、重建和释放生命周期。
- 新增状态栏高度与 WindowInsets、SystemUI 窗口同步，支持运行时应用及禁用后恢复系统高度；fuxi 无重启切换仍待实机验证。
- 加固状态栏和控制中心手势、View、回调及 ClassLoader 生命周期，减少重复触发、状态冲突和过期对象残留。
- 优化进程路由、Feature 安装去重和关闭功能的初始化路径；普通异常保持隔离，致命错误继续传播。
- 构建产物增加 Git revision 与 provenance 记录，功能语义清单、Python 门禁、单元测试和 lint 纳入统一验证。

### 验证状态

- `python tools/verify.py full`、功能语义校验、源码风险扫描、CI 可移植性检查及 Python 全量测试均通过。
- Python 工具测试共 405 项通过，Android JVM 单元测试与 `lintDebug` 通过。
- 状态栏高度的 `44 → 40 → 12 → 44 → disabled` 无重启实机验证尚未执行，本版本不标记为全面 `DEVICE_VERIFIED`。

### 产物信息

- APK：`CustoMIUIzer-A14-r14.18.0.apk`
- 大小：`3436081` bytes
- SHA-256：`31D839BDE68749D16FC13FC426B3B4975E84A29F9910326D33BBE00815FE9953`
- versionCode / versionName：`193 / r14.18.0`
- 已通过完整离线门禁、Release/R8 构建、v2 签名、zipalign、`debuggable=false`、SDK 与 Xposed 元数据检查。

## r14.16.1 — 2026-08-01

- 按目标进程和功能开关安装模块功能，避免关闭功能仍创建无关 Hook、Receiver、Observer 或任务，并阻止同一进程重复安装 Feature。
- 修复启动早期偏好快照、安装失败状态和反射缓存边界，降低功能开关未生效、重复安装和缓存无界增长风险。
- 完善 Receiver、Observer、天气、计步、专辑封面、电量指示器和覆盖层 View 的释放路径，减少 Context、View 与 Bitmap 残留。
- 所有共享 Hook 与回调边界继续隔离普通兼容异常，但不吞掉 `OutOfMemoryError`。
- 优化网速、充电提示、导航图标、电量指示和透传 Hook 高频路径，减少临时对象与重复更新。
- 设置开关点击后立即显示目标状态，再执行原有保存和重启提示逻辑，改善连续点击时无反馈的问题。
- 模块加载日志加入版本和短 Git SHA；API 102 专属 Hook 能力继续隔离，未接入生产路径。

### 产物信息

- APK：`CustoMIUIzer-A14-r14.16.1.apk`
- 大小：`3369409` bytes
- SHA-256：`F213BA3F939FAA7BD12150D75A538529E9517D9CE865B6611C7A3C93C8370258`
- versionCode / versionName：`192 / r14.16.1`
- 已通过完整离线门禁、Release/R8 构建、v2 签名、zipalign、`debuggable=false`、SDK 与 Xposed 元数据检查。

### 历代核心实现总结

r14 系列建立了独立包名、签名和 HyperOS 1 / Android 14 维护线，完成 Kotlin 迁移、libxposed API 101/102 单 APK 兼容、`system` 作用域恢复、偏好同步、生命周期治理、反射与资源缓存加固，以及状态栏、Launcher、锁屏、控制中心和设置界面的持续优化；细节保留在源码仓库 Git 历史和旧 tags 中，旧 APK 不再保留为 Release 资产。
