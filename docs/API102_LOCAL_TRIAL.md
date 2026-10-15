# API 102 稳定 Hook ID 本地试用

## 239：稳定 ID、回读与 DexSQL 联合取证

沿用当前升级分支，从 `8a8c2f4` 继续。本阶段允许以兼容性探索、API 能力和开发流程为升级理由，
不要求先证明提速或 APK 缩小。版本名仍为 r14.23.1，versionCode 239，仅用于本地试用。

| 既有安装点 | API 102 ID | 行为边界 |
| --- | --- | --- |
| SystemUIInitializer.init(boolean) | `systemui.init` | 保留原初始化回调与安装时序 |
| PluginInstance$PluginFactory.createPlugin 所有声明重载 | `systemui.plugin.create` | 保留原重载选择、插件筛选、lease 与加载器生命周期 |
| 已选择进程中的 Application.attach(Context) | `application.attach` | 使用 Application 实际加载器，保留既有偏好 / 包选择及 post-attach 回调 |

只有实际 Hook API ≥102 进入隔离桥；API 101 使用既有普通安装。
成功设置 ID 后用 API 102 `HookHandle.getId()` 回读，相等才记录 `API 102 stable hook ID installed: <id> (confirmed)`。
setter 的普通失败继续同一 builder 的普通安装；回读不匹配、方法缺失或普通失败只记诊断，
保留已安装 handle，不取消 / 重装。`intercept` 永不重试；致命错误及反射 / 代理包装的致命错误继续传播。
所有可选调用仍受禁止 R8 内联的桥隔离，回调热路径不读取框架 ID，也没有新增 Receiver / Observer / 任务。

DexSQL 当前已在聊天原生接入。已用 `resources/read`、schema、真实 ROM 联合数据集、
定义 / 来源查询及 Smali 核对 SystemUI 初始化与插件入口：定义在宿主 APK，
createPlugin 从 Supplier 取 ClassLoader，再加载 / 构造插件；插件实现仍来自独立插件 APK。
新增 [查询](../tools/dexsql/README.md) 将目标声明、最终 DEX 的 setId / getId / replaceHook 调用数量、
固定 ID 的使用位置与同产物 mapping 串联。它不进入 APK，也不生成未经审查的 Hook。

根据 [官方 102.0.0 源码](https://repo.maven.apache.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0-sources.jar)，
setId 的原子替换以模块和 executable 为边界，同一 ID 可用于不同重载，但不能替代本项目的一次安装状态。
`replaceHook`、自动热重载与 detach 暂不启用：热重载不会重放包生命周期，必须先完成旧代线程、JNI、
Receiver / Observer、系统持有对象和 native 生命周期交接。暂缓依据是兼容与生命周期要求，不是缺少性能收益。

回归覆盖 API 101 零可选调用、API 102 重载 / 优先级 / 模式 / 回调保留、ID 回读、
普通回退、部分安装失败不重试、致命异常传播和既有插件释放测试。
本阶段原始数据在 `.di-evidence/api102-dexsql-integration-20261015/`。
连接设备仍安装 238；日志未找到可确认的 API 102 ID 记录，不能把设备连接或普通使用正常当成 239 实机验收。

已通过 Full（含 lintDebug），JUnit 2,609 项零失败 / 错误、2 项跳过；Python 工具 512 项零失败、5 项跳过。
设备 Vector 2.2 (3112) 的 framework DEX 也已读取并由原生 MCP 验证：SHA-256 为
`fc5ae6b445a9b35216474176ced3e2e874067e1ccb4b8a146d88747b02cfaea1`，声明 setId / getId / replaceHook，
getApiVersion 方法体返回 102。它是框架产物证据，仍不能代替新 APK 的 Hook 安装回读。

签名 Release 来自 `0c0702738a1ad1669a1537e70e95662465199e4c`，clean、关闭构建 / 配置缓存和 Kotlin 增量编译，
Release / R8 analyzer / lintRelease 用时 185.34 秒。原证书 V2 单签名、16 KiB ZIP 对齐、版本来源、
SDK 34 / arm64 与 ZIP CRC 通过。APK 为 3,799,501 字节，与 238 相同；DEX 为 2,022,424 字节，增加 1,020 字节。
资源表仍为 804,712 字节、native 394,984 字节；完整资源 / ID / 值、Xposed 元数据、业务 assets 和 ServiceLoader 语义一致。

原生 DexSQL MCP 与 SDK dexdump 独立确认：setId / getId 各一处 invoke，均在映射回 Api102HookBridge 的 `Lr3;`；
没有显式 replaceHook / 热重载类型或调用。三个固定 ID 分别在插件安装、SystemUI 初始化和应用入口的最终 DEX 中使用；
两个公共安装器中保留能力位判断与普通安装回退，已逐方法 decode 复核。
本地包在 `release-output/r14.23.1-api102-dexsql-local-239/`，SHA-256：
`f4cc59ce73e70419e6c18e7a603703cc7e7bf915b3d7052a8090346f5ab066d0`。
该包尚未覆盖安装到设备，未做重复 Release 或 API 101 / 102 新包实机验收，不宣称运行时性能提升。

## 238：首次稳定 ID 试验记录

从用户反馈无明显问题的 237 阶段提交 `451b2d3819a8605053c1e03369670985965499b6` 创建 `codex/api102-hook-id-local-20261015`。r14.23.1 / 238，仅生成既有正式证书签名的本地 Release，不推送、合并或发布。

项目此前已编译到 API 102，动态作用域请求已按项目策略在 Service API ≥102 启用；Hook ID 桥此前没有生产调用。本阶段只为既有 `SystemUIInitializer.init(boolean)` 安装点接入 ID `systemui.init`，不增加业务 Hook，不修改回调、安装时序、偏好、Receiver/Observer 或 FeatureRegistry。

依据 [官方 API 102 发布源码](https://repo.maven.apache.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0-sources.jar)：同模块、同 executable 的同 ID Hook 会原子替换旧实例，执行中的调用使用原链快照。ID 不替代项目已有的一次安装和所有者管理。本阶段不声明自动热重载，不调用显式替换或 detach；这些能力需要独立生命周期设计与验证。

运行时仍从模块入口的真实 `getApiVersion()` 生成能力快照。API 101 走原 `XposedHelpers.findAndHookMethod`，不会调用 ID 桥；102 只在安装冷路径配置 ID，继续使用原优先级、PASSTHROUGH 模式与回调。setter 普通/缺失 API 错误降级到同 builder 的普通安装，致命错误传播；`intercept` 失败不重试。R8 keep 规则防止桥被内联到 API 101 公共路径；编译后的 API 102 方法引用位置也需检查。

专项测试覆盖 101/102 安装、优先级与回调、unhook、缺失可选方法、普通错误、致命/包装致命错误以及安装失败不重试。收口执行 Fast/Full、Python 工具测试、签名 Release/R8 analyzer/lintRelease，以及 APK 的版本来源、签名、SDK/ABI、入口、资源和最终 DEX 隔离检查。

其他输入保持上一阶段：JDK/toolchain/Java/Kotlin 输出目标 25，AGP 9.4.1、Gradle 9.8.1、Kotlin 编译器/stdlib 2.4.21、language/API 2.2、DexKit 2.3.0；包名、SDK 34、arm64、minApiVersion=101、targetApiVersion=102、staticScope=true 保持。

实机试用：覆盖安装后重启，检查 SystemUI 启动、状态栏/控制中心与设置应用。API 102 成功安装时仅在冷路径记录 `API 102 stable hook ID installed: systemui.init`；setter 不可用时记录降级原因。若没有该日志，不能把普通运行无问题视为新能力已启用。API 101 回退的主机测试不替代 API 101 框架实机验收；不宣称运行性能提升。

原始证据在忽略目录 `.di-evidence/api102-hook-id-local-20261015/`，APK 与验收记录在 `release-output/r14.23.1-api102-local-238/`。

本轮结果：Full、2598 项 JVM 测试（跳过 2，含 7 项安装回归）和 510 项 Python 测试（跳过 5）零失败；签名 Release、R8 analyzer、lintRelease 与 16 KiB ZIP 对齐检查通过。最终 DEX 只有一处 `setId` 调用，位于独立桥类，未引入热重载类型或显式替换/detach 调用。资源、ID/值、native、业务 assets、既有项目保留成员与反混淆后的 ServiceLoader 元数据保持。APK 仍为 3,799,501 字节，DEX 较 237 增加 4,848 字节；未做本构建的实机或重复 Release 验证。
