# Changelog

本文件仅记录 LSPosed 模块仓库中的用户可见变化。

## r14.15.3

* 恢复此前误删的 `system` 作用域，修复 `system_server` Hook 未加载及相关系统级功能静默失效；
* 加固 Global Actions Receiver 的异常隔离、信任验证和有序广播处理；
* 完善 Receiver / Observer 生命周期及并发注册处理；
* 改进 Hook 加载诊断和兼容信息记录；
* 状态栏网速粗体保留 SystemUI 当前字体家族；
* 新增双排网速行距 `70%–130%` 和相关本地化提示；
* 修复设置文本样式继承和 About 页面文字换行；
* 保持 HyperOS 1 / Android 14、`arm64-v8a` 和 libxposed API 101/102 兼容。

### APK

* 文件：`CustoMIUIzer-A14-r14.15.3.apk`
* 大小：`3107265` bytes
* SHA-256：`F7AB34722B0193DD8C97DF0146C968E5A6064655AD497061E902CD1545375E7E`
* 签名证书 SHA-256：`C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`
* versionCode / versionName：`191 / r14.15.3`

### 验证说明

本版本已完成 APK 构建、正式签名、zipalign、包信息和 Xposed 元数据基础检查，并确认 `scope.list` 包含 `system` 与 `android`；未执行完整测试套件和全功能实机回归。
