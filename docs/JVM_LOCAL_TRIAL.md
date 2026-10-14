# JVM 与工具链本地试验

目标 21 试验从公开版 `1b620121c317cda018798837864751967f01b2b1` 开始，提交为 `39ac5e02cd54f25fa5940c12698273607704487a`；目标 25 试验提交为 `2420550bdadc640440c9152cc109e48ee0efb44c`。用户反馈两者试用无明显问题后，授权继续升级构建工具/运行库，从目标 25 提交创建本地分支 `codex/jvm25-toolchain-local-20261014`。产物只保存到本地，用现有正式证书签名的 Release 配置供用户试用；版本名保持 `r14.23.1`，versionCode 从 235 增至 236，公开发行版仍为 233。

## 试验范围

- 主机构建 JVM、Java toolchain 和 Java source / target、Kotlin JVM target 均保持 25。
- AGP 9.3.3 → 9.4.1，使用其内置 R8；Kotlin stdlib / BOM 2.3.21 → 2.4.21。已有 Kotlin 编译器 2.4.21、language / API 2.2 和 Gradle 9.8.1 保持不变，避免同时改变源语言语义。
- 保留已有 R8、资源收缩与 AAPT2 compact 处理，不改变 SDK 34 运行边界、包名、ABI、Xposed 入口、API 101 / 102 隔离或运行源码。
- 输出目标 25 表示编译中间 class 格式；手机执行的是经过 D8 / R8 转换的 DEX，由 Android 14 ART 运行。它不会赋予 Android 14 JDK 25 的全部库 API。

参考：[Android Java 版本](https://developer.android.com/build/jdks)、[Kotlin 版本与 stdlib 支持周期](https://kotlinlang.org/docs/releases.html)、[AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes)、[9.4.1 修复记录](https://developer.android.com/studio/releases/fixed-bugs/studio/2026.1.4)。官方 Maven 元数据确认 9.4.1 是当前最新稳定 AGP；Gradle 9.8.1 和 Build Tools 36.0.0 满足要求，AGP 默认 KGP 仍为 2.2.10，继续显式固定编译器 2.4.21。

Kotlin 编译器、协程、AppCompat、Fragment、Preference 与 Commons Lang 已是当前稳定最新版。DexKit 2.3.0 更新 native 查询缓存及引用索引；本轮保留 2.2.0，待真实 ROM 查询和桥生命周期验证后独立评估，不把 JVM 测试当作 native 查询证据。

## 验证要求

执行现有 Fast / Full、Python 工具测试、Release / R8 构建、签名与来源检查。确认 Java 和 Kotlin 中间 class 的 major version 为 69。使用相同源码、版本和 revision，对比旧 AGP/stdlib 组合与新组合，检查资源 ID、AAPT2 compact 结果、native/Xposed 元数据和新增平台 API。双次无缓存新组合构建检查 APK / mapping 可重复性。

原始日志、构建退出码与耗时、APK / DEX 对照和重复构建结果保存在忽略目录 `.di-evidence/toolchain-20261014/`；本地试用包与验收说明保存在 `release-output/r14.23.1-toolchain-local-236/`，不将 APK、mapping 或签名材料提交 Git。旧组合对照通过该忽略目录内的 Gradle init script 覆盖插件和运行库版本，其余输入保持相同；它仅作为测量对照，不是默认构建配置。此前目标 21/25 的证据和试用包保留在原有本地目录。

目标设备验收由用户进行，至少覆盖安装与模块启用、所有设置分类和子页、关于页返回、偏好保存、System UI / Launcher / system_server 的既有 Hook。静态门禁和 Android 14 界面模型不能代替目标 HyperOS 1 实机，也不能证明内存或 CPU 已改善。
