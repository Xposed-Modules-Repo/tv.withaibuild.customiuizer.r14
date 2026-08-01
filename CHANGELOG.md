# Changelog

简体中文 | [English](CHANGELOG_EN.md)

## r14.16.1 — 2026-08-01

- 按目标进程和功能开关安装模块功能，避免关闭功能仍创建无关 Hook、Receiver、Observer 或任务，并阻止同一进程重复安装 Feature。
- 修复启动早期偏好快照、安装失败状态和反射缓存边界，降低功能开关未生效、重复安装和缓存无界增长风险。
- 完善 Receiver、Observer、天气、计步、专辑封面、电量指示器和覆盖层 View 的释放路径，减少 Context、View 与 Bitmap 残留。
- 所有共享 Hook 与回调边界继续隔离普通兼容异常，但不吞掉 `OutOfMemoryError`。
- 优化网速、充电提示、导航图标、电量指示和透传 Hook 高频路径，减少临时对象与重复更新。
- 设置开关点击后立即显示目标状态，再执行原有保存和重启提示逻辑，改善连续点击时无反馈的问题。
- 模块加载日志加入版本和短 Git SHA；API 102 专属 Hook 能力继续隔离，未接入生产路径。

### 产物与验证

- APK：`CustoMIUIzer-A14-r14.16.1.apk`
- 大小：`3369409` bytes
- SHA-256：`F213BA3F939FAA7BD12150D75A538529E9517D9CE865B6611C7A3C93C8370258`
- 签名证书 SHA-256：`C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`
- versionCode / versionName：`192 / r14.16.1`
- 已通过完整离线门禁、Release/R8 构建、v2 签名、zipalign、`debuggable=false`、SDK 与 Xposed 元数据检查。
- 新增变化尚未完成全部功能逐项实机行为验证。

### 历代核心实现总结

r14 系列建立了独立包名、签名和 HyperOS 1 / Android 14 维护线，完成 Kotlin 迁移、libxposed API 101/102 单 APK 兼容、`system` 作用域恢复、偏好同步、生命周期治理、反射与资源缓存加固，以及状态栏、Launcher、锁屏、控制中心和设置界面的持续优化；细节保留在源码仓库 Git 历史和旧 tags 中，旧 APK 不再保留为 Release 资产。
