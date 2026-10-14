# JVM 与工具链本地试验

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

## 当前依赖阶段（237）

237 的验证复用既有流程：Fast / Full、Python 工具测试与签名 Release / R8 analyzer / lintRelease；确认实际解析 DexKit 2.3.0、arm64 ELF 与 JNI 入口保留、16 KiB ZIP 对齐、SDK / Xposed 元数据、资源 ID / 值及新增平台 API。证据保存到忽略目录 `.di-evidence/dexkit23-local-20261015/`，APK 与验收说明保存到 `release-output/r14.23.1-dexkit23-local-237/`。本轮验证升级可构建与静态兼容，不重做优化收益基准。

237 阶段实际结果：Fast / Full、2591 JVM 测试（跳过 2）和 507 Python 测试（跳过 5）零失败；单次 clean Release / R8 analyzer / lintRelease、原证书 V2 签名与 16 KiB 对齐检查通过。APK 为 3,799,501 字节，DEX 为 2,016,556 字节，native 为 394,984 字节。资源 ID / 值、业务 assets、项目保留成员和服务加载接口 / provider 保持；原有 34 个 JNI 导出保留，新库共 35 个，没有新增 native 系统导入。两个新增 Java 平台方法均自 API 1 可用。本阶段尚未执行 native 查询或重复 Release 验证，实机验收仍待用户试用。

目标设备验收由用户进行，至少覆盖安装与模块启用、所有设置分类和子页、关于页返回、偏好保存、System UI / Launcher / system_server 的既有 Hook。237 还需覆盖截图保存位置功能，以及国内 ROM 的远程防诈检测拦截功能（国际版隐藏该功能）。静态门禁和 Android 14 界面模型不能代替目标 HyperOS 1 实机与 native 查询，也不能证明内存或 CPU 已改善。
