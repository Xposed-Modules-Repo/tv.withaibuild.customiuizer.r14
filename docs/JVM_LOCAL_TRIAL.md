# JVM 输出目标本地试验

目标 21 试验从公开版 `1b620121c317cda018798837864751967f01b2b1` 开始，提交为 `39ac5e02cd54f25fa5940c12698273607704487a`。用户反馈试用无明显问题后，明确授权继续目标 25 试验，创建本地分支 `codex/jdk25-jvm25-local-20261014`。产物只保存到本地，用现有正式证书签名的 Release 配置供用户试用；版本名保持 `r14.23.1`，versionCode 从 234 增至 235，公开发行版仍为 233。

## 试验范围

- 主机构建 JVM 和 Java 编译工具链继续使用 JDK 25；Java source / target 与 Kotlin JVM target 从 21 同步提高到 25。
- 已有 Kotlin 编译器 2.4.21、Kotlin language / API 2.2、stdlib / BOM 2.3.21、AGP 9.3.3 和 Gradle 9.8.1 保持不变，便于单独衡量输出目标的影响。
- 保留已有 R8、资源收缩与 AAPT2 compact 处理，不改变 SDK 34 运行边界、包名、ABI、Xposed 入口、API 101 / 102 隔离或运行源码。
- 输出目标 25 表示编译中间 class 格式；手机执行的是经过 D8 / R8 转换的 DEX，由 Android 14 ART 运行。它不会赋予 Android 14 JDK 25 的全部库 API。

参考：[Android Java 版本](https://developer.android.com/build/jdks)、[Kotlin 编译选项](https://kotlinlang.org/docs/gradle-compiler-options.html)、[AGP 9.3 / R8](https://developer.android.com/build/releases/agp-9-3-0-release-notes)。

## 验证要求

执行现有 Fast / Full、Python 工具测试、Release / R8 构建、签名与来源检查。确认 Java 和 Kotlin 中间 class 的 major version 为 69，并比较同源码、同依赖、同版本与同 revision 下的目标 21 / 25 产物。双次无缓存目标 25 构建检查 APK / mapping 可重复性。

原始日志、构建退出码与耗时、APK / DEX 对照和重复构建结果保存在忽略目录 `.di-evidence/jvm25-local-20261014/`；本地试用包与验收说明保存在 `release-output/r14.23.1-jvm25-local-235/`，不将 APK、mapping 或签名材料提交 Git。目标 21 对照通过该忽略目录内的 Gradle init script 同步覆盖 Java / Kotlin 输出目标，其余输入保持相同；它仅作为测量对照，不是默认构建配置。此前目标 21 的证据与试用包仍保留在原有本地目录。

目标设备验收由用户进行，至少覆盖安装与模块启用、所有设置分类和子页、关于页返回、偏好保存、System UI / Launcher / system_server 的既有 Hook。静态门禁和 Android 14 界面模型不能代替目标 HyperOS 1 实机，也不能证明内存或 CPU 已改善。
