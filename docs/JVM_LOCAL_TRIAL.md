# JVM 与工具链本地试验

## 当前阶段（241）：升级关联路径收敛

沿用现有分支，从 `54d1a06` 审查升级差异及关联调用链，保留 240 的工具链组合。
在目标 HyperOS 1 的独立 ART 进程中，用实际 R8 APK 复现原方法致命错误被 after 恢复、
前后回调吞掉代理 / 反射包装致命错误；以既有 FatalErrors 边界修复，并保留普通异常恢复与参数传递。
将每次 BeforeHookCallback 创建的空数组改为共享不可变空数组；删除六个未使用但因隔离规则保留的 API 102 占位 ID，实际三个 ID 与隔离规则保留。
真实 Service 102 SDK 的提交失败注入还复现了镜像忽略 false 的问题；修复提交结果检查，
发生未确认提交后，同一连接的乐观缓存不再证明已送达，提示需等新连接的完整同步才清除，维持一次重试预算。

版本名 r14.23.1、versionCode 241，仅本地正式签名 Release；定向测试已通过，完整门禁、产物对照与设备回归结果在本阶段收口后记录。
原始证据保存在忽略目录 `.di-evidence/upgrade-review-20261015/`，不将独立 ART 分配计数等同于真实 Hook 进程 PSS / CPU / 帧率。

## 前序阶段（240）：Kotlin language / API 2.4

沿用 `codex/api102-hook-id-local-20261015`，从 `6b027bc` 继续，不切换或新建分支。
用户反馈 239 试用正常，并授权使用已连接的 HyperOS 1 / Android 14 设备继续验证。
本阶段只将 Kotlin language / API 2.2 → 2.4，保留编译器 / BOM / stdlib 2.4.21、
AGP 9.4.1 / 内置 R8 9.4.24、Gradle 9.8.1、JDK / JVM 25，以及既有 API 101 / 102 隔离和 Hook 行为。
版本名保持 r14.23.1，versionCode 240，仅生成本地签名 Release；不推送或发布。

2026-10-15 再次核对官方 Maven 元数据，上述编译器、AGP、协程 1.11.0、libxposed API / Service 102.0.0
已是各自最新稳定版本；Kotlin 2.5 Beta / AGP 9.5 alpha 不混入本轮。
语言级别此前独立停留在 2.2，本轮按官方 [2.3 迁移说明](https://kotlinlang.org/docs/compatibility-guide-23.html)
和 [2.4 迁移说明](https://kotlinlang.org/docs/compatibility-guide-24.html) 验证更严格的类型检查、
注解目标默认规则和元数据变化；不引入为迁移而重写的框架或新运行依赖。

收口包括 Fast / Full、工具测试、clean Release / R8 analyzer / lintRelease、签名与来源检查、
中间 class / 最终 DEX 与资源对照，以及安装后的设置导航、偏好保存和 API 102 安装回读。
原始证据在忽略目录 `.di-evidence/kotlin24-local-20261015/`。

240 验证结果：Fast / Full（含 lintDebug）、2,609 项 JVM 测试（跳过 2）和 512 项 Python 测试（跳过 5）
零失败，无需修改业务源码。2,077 个中间 class 均为 major 69，2,063 个 Kotlin 类的 metadata 版本由
2.2.0 升至 2.4.0。抽查确认协程 DebugMetadata v2、容器桥方法的 CLASS 级空值注解变化；
成员清单仅有一个私有 SAM 辅助方法的返回类型擦除为 Object，LambdaMetafactory 引用同步变化。
R8 后 DEX 在明确归一化备份头版本常量、来源日志、校验值及 R8 map ID 后字节一致；
没有新增 Android / Java 平台引用，不将中间 class 的变化等同于 ART 运行成本变化。

签名 Release 来自 `276f522c5f87eb1cc09efec761589243fc7d09d7`。两次 clean、关闭构建 / 配置缓存及 Kotlin 增量的
Release / R8 analyzer / lintRelease 分别用时 192.44 / 184.63 秒，APK 与 mapping 各自字节一致。
与 239 相比，APK 仍为 3,799,501 字节，DEX 2,022,424 字节，resources.arsc 804,712 字节，native 394,984 字节；
资源 / ID / 值、native、Xposed 元数据、业务 assets 和 ServiceLoader 语义保持，R8 配置不变。
原证书 V2 单签名、来源、ZIP CRC、16 KiB ZIP 对齐、SDK 34 / arm64 检查通过。
原生 DexSQL MCP 确认类 / 方法 / 字段定义数量及注解保留清单保持，setId / getId 各一处调用仍隔离在桥内；
两个公共安装器的能力位与 API 101 普通回退已逐方法解码复核。

已在 Xiaomi 13 / fuxi、HyperOS 1 `V816.0.7.0.UMCTWXM` / Android 14、Vector 2.2 (3112) 上覆盖安装并重启。
安装包哈希与本地产物一致；三个 API 102 稳定 ID 均有 confirmed 回读，SystemUI / Launcher / system_server
安装汇总无 failed。实机检查四个分类、各一个设置子页、关于页返回、桌面与控制中心；
入口位置偏好在应用进程重启后保留，测试后全部 402 项原偏好及临时唤醒设置已恢复。
本次观察窗口内未发现目标进程崩溃，SystemUI / Launcher 的 PID 保持；这不等于全功能长期验收，
也未覆盖 API 101 框架、国内 ROM 的全部功能或 ART 性能基准。

本地包：`release-output/r14.23.1-kotlin24-local-240/CustoMIUIzer-A14-r14.23.1-kotlin24-local-240.apk`，
SHA-256：`bb065ebbb80db7aace4f1e173ac41a548cadbe26fa1eac2da43b69d5134a0df6`。
没有推送、合并或发布；不宣称性能收益。

## 前序阶段（234—237）

目标 21 试验从公开版 `1b620121c317cda018798837864751967f01b2b1` 开始，提交为 `39ac5e02cd54f25fa5940c12698273607704487a`；目标 25 试验提交为 `2420550bdadc640440c9152cc109e48ee0efb44c`。用户反馈两者及工具链试用包 236 无明显问题后，授权继续分阶段升级测试，不要求优化收益。本阶段从 `66f2fae13e32b5f65180be48753564a1acac7bb8` 创建本地分支 `codex/dexkit23-local-20261015`，仅升级 DexKit 2.2.0 → 2.3.0，保留既有查询与桥生命周期。产物只保存到本地，用现有正式证书签名的 Release 配置供用户试用；版本名保持 `r14.23.1`，versionCode 增至 237。

## 已完成的工具链阶段（236）

- 主机构建 JVM、Java toolchain 和 Java source / target、Kotlin JVM target 均保持 25。
- AGP 9.3.3 → 9.4.1，使用其内置 R8；Kotlin stdlib / BOM 2.3.21 → 2.4.21。已有 Kotlin 编译器 2.4.21、language / API 2.2 和 Gradle 9.8.1 保持不变，避免同时改变源语言语义。
- 保留已有 R8、资源收缩与 AAPT2 compact 处理，不改变 SDK 34 运行边界、包名、ABI、Xposed 入口、API 101 / 102 隔离或运行源码。
- 输出目标 25 表示编译中间 class 格式；手机执行的是经过 D8 / R8 转换的 DEX，由 Android 14 ART 运行。它不会赋予 Android 14 JDK 25 的全部库 API。

参考：[Android Java 版本](https://developer.android.com/build/jdks)、[Kotlin 版本与 stdlib 支持周期](https://kotlinlang.org/docs/releases.html)、[AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes)、[9.4.1 修复记录](https://developer.android.com/studio/releases/fixed-bugs/studio/2026.1.4)。官方 Maven 元数据确认 9.4.1 是当前最新稳定 AGP；Gradle 9.8.1 和 Build Tools 36.0.0 满足要求，AGP 默认 KGP 仍为 2.2.10，继续显式固定编译器 2.4.21。

2026-10-15 复核官方元数据，AGP 9.4.1、Gradle 9.8.1、Kotlin 编译器 2.4.21 仍为最新稳定版，本阶段保持这些版本以及 Kotlin language/API 2.2。[DexKit 2.3.0](https://github.com/LuckyPray/DexKit/releases/tag/2.3.0) 更新 native 查询缓存、引用索引和 DEX 加载；本阶段单独试用，JVM 测试不视为 native 查询证据。AGP alpha / Kotlin Beta 与语言版本提升留给独立后续阶段，避免混入本轮依赖试验。

## 验证要求

执行现有 Fast / Full、Python 工具测试、Release / R8 构建、签名与来源检查。确认 Java 和 Kotlin 中间 class 的 major version 为 69。使用相同源码、版本和 revision，对比旧 AGP/stdlib 组合与新组合，检查资源 ID、AAPT2 compact 结果、native/Xposed 元数据和新增平台 API。双次无缓存新组合构建检查 APK / mapping 可重复性。

原始日志、构建退出码与耗时、APK / DEX 对照和重复构建结果保存在忽略目录 `.di-evidence/toolchain-20261014/`；本地试用包与验收说明保存在 `release-output/r14.23.1-toolchain-local-236/`，不将 APK、mapping 或签名材料提交 Git。旧组合对照由同提交的 `git archive` 创建隔离副本，仅修改版本目录中的 AGP 和 Kotlin BOM / stdlib 版本，保持编译器及其余构建输入相同；它仅作为测量对照，不是默认构建配置。此前目标 21/25 的证据和试用包保留在原有本地目录。

236 阶段实际结果：R8 9.3.28 → 9.4.24；相同输入的 APK 均为 3,769,157 字节，DEX 2,012,564 → 2,012,372 字节（减少 192 字节），没有可见 APK 瘦身收益。资源 ID / 值、资源文件、Manifest、native、业务 assets 和保留成员集合一致，平台 API 引用没有变化。两次禁用缓存与增量的 clean 新组合构建，APK 和 mapping 分别字节一致。Fast / Full、2591 JVM 测试（跳过 2，包含 7 项设置导航界面模型）及 507 Python 测试（跳过 5）零失败，Release / R8 analyzer / lintRelease、正式证书 V2 签名与 16 KiB 对齐检查通过。实际构建耗时和完整数据见本地验收说明；没有实机性能测量，不宣称运行更快。

## 依赖阶段（237）

237 的验证复用既有流程：Fast / Full、Python 工具测试与签名 Release / R8 analyzer / lintRelease；确认实际解析 DexKit 2.3.0、arm64 ELF 与 JNI 入口保留、16 KiB ZIP 对齐、SDK / Xposed 元数据、资源 ID / 值及新增平台 API。证据保存到忽略目录 `.di-evidence/dexkit23-local-20261015/`，APK 与验收说明保存到 `release-output/r14.23.1-dexkit23-local-237/`。本轮验证升级可构建与静态兼容，不重做优化收益基准。

237 阶段实际结果：Fast / Full、2591 JVM 测试（跳过 2）和 507 Python 测试（跳过 5）零失败；单次 clean Release / R8 analyzer / lintRelease、原证书 V2 签名与 16 KiB 对齐检查通过。APK 为 3,799,501 字节，DEX 为 2,016,556 字节，native 为 394,984 字节。资源 ID / 值、业务 assets、项目保留成员和服务加载接口 / provider 保持；原有 34 个 JNI 导出保留，新库共 35 个，没有新增 native 系统导入。两个新增 Java 平台方法均自 API 1 可用。本阶段尚未执行 native 查询或重复 Release 验证，实机验收仍待用户试用。

目标设备验收由用户进行，至少覆盖安装与模块启用、所有设置分类和子页、关于页返回、偏好保存、System UI / Launcher / system_server 的既有 Hook。237 还需覆盖截图保存位置功能，以及国内 ROM 的远程防诈检测拦截功能（国际版隐藏该功能）。静态门禁和 Android 14 界面模型不能代替目标 HyperOS 1 实机与 native 查询，也不能证明内存或 CPU 已改善。
