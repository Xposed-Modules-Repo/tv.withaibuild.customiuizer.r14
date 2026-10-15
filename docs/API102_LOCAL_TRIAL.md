# API 102 稳定 Hook ID 本地试用

从用户反馈无明显问题的 237 阶段提交 `451b2d3819a8605053c1e03369670985965499b6` 创建 `codex/api102-hook-id-local-20261015`。r14.23.1 / 238，仅生成既有正式证书签名的本地 Release，不推送、合并或发布。

项目此前已编译到 API 102，动态作用域请求已按项目策略在 Service API ≥102 启用；Hook ID 桥此前没有生产调用。本阶段只为既有 `SystemUIInitializer.init(boolean)` 安装点接入 ID `systemui.init`，不增加业务 Hook，不修改回调、安装时序、偏好、Receiver/Observer 或 FeatureRegistry。

依据 [官方 API 102 发布源码](https://repo.maven.apache.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0-sources.jar)：同模块、同 executable 的同 ID Hook 会原子替换旧实例，执行中的调用使用原链快照。ID 不替代项目已有的一次安装和所有者管理。本阶段不声明自动热重载，不调用显式替换或 detach；这些能力需要独立生命周期设计与验证。

运行时仍从模块入口的真实 `getApiVersion()` 生成能力快照。API 101 走原 `XposedHelpers.findAndHookMethod`，不会调用 ID 桥；102 只在安装冷路径配置 ID，继续使用原优先级、PASSTHROUGH 模式与回调。setter 普通/缺失 API 错误降级到同 builder 的普通安装，致命错误传播；`intercept` 失败不重试。R8 keep 规则防止桥被内联到 API 101 公共路径；编译后的 API 102 方法引用位置也需检查。

专项测试覆盖 101/102 安装、优先级与回调、unhook、缺失可选方法、普通错误、致命/包装致命错误以及安装失败不重试。收口执行 Fast/Full、Python 工具测试、签名 Release/R8 analyzer/lintRelease，以及 APK 的版本来源、签名、SDK/ABI、入口、资源和最终 DEX 隔离检查。

其他输入保持上一阶段：JDK/toolchain/Java/Kotlin 输出目标 25，AGP 9.4.1、Gradle 9.8.1、Kotlin 编译器/stdlib 2.4.21、language/API 2.2、DexKit 2.3.0；包名、SDK 34、arm64、minApiVersion=101、targetApiVersion=102、staticScope=true 保持。

实机试用：覆盖安装后重启，检查 SystemUI 启动、状态栏/控制中心与设置应用。API 102 成功安装时仅在冷路径记录 `API 102 stable hook ID installed: systemui.init`；setter 不可用时记录降级原因。若没有该日志，不能把普通运行无问题视为新能力已启用。API 101 回退的主机测试不替代 API 101 框架实机验收；不宣称运行性能提升。

原始证据在忽略目录 `.di-evidence/api102-hook-id-local-20261015/`，APK 与验收记录在 `release-output/r14.23.1-api102-local-238/`。
