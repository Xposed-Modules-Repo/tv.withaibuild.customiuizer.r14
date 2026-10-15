# JVM 与工具链

r14.23.3 将原开发分支已分阶段验证的组合纳入正式源码。238–241 试用包的逐次记录保存在 Git 历史；
主机基准、独立 ART 和真实系统进程验收仍分别报告，不把升级本身视为性能收益。

## 当前组合

| 组件 | 版本 / 配置 |
| --- | --- |
| 构建 JDK、Java toolchain | 25 |
| Java source / target、Kotlin jvmTarget | 25 |
| Kotlin compiler、BOM / stdlib | 2.4.21 |
| Kotlin language / API | 2.4 |
| Gradle / AGP | 9.8.1 / 9.4.1 |
| AGP 内置 R8 / D8 | 9.4.24 |
| AndroidX AppCompat / Fragment | 1.8.0 / 1.9.1 |
| AndroidX Preference / Palette | 1.2.1 / 1.0.0 |
| DexKit | 2.3.0 |
| libxposed API / Service | 102.0.0 / 102.0.0 |
| kotlinx.coroutines / Commons Lang | 1.11.0 / 3.21.0 |

2026-10-15 核对官方 Maven 元数据及稳定发行说明，以上直接依赖无需继续抬版本。
AGP 的 KGP 覆盖和主机安全依赖约束仍由实际解析图验证，不能仅因 AGP 升级就移除。
Gradle 的宿主 Kotlin 标准库与 APK BOM 属于不同 classpath；不强行覆写 Gradle 自带依赖。

[Kotlin 稳定版本](https://kotlinlang.org/docs/releases.html)、
[Gradle 发行版](https://gradle.org/releases/)、
[AGP 兼容范围](https://developer.android.com/build/releases/agp-9-4-0-release-notes)、
[AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat)、
[Fragment](https://developer.android.com/jetpack/androidx/releases/fragment)。

## 运行与构建边界

compileSdk 37、Build Tools 36.0.0 只影响编译；minSdk / targetSdk 仍为 34、arm64-v8a，
只支持 HyperOS 1 / Android 14。JVM class 25 由当前 R8/D8 转为 DEX，不表示 Android 支持桌面 JDK 25 API。
新增平台调用仍需按 Android 14 检查。原生 API 102 规则见 [API 说明](API102_LOCAL_TRIAL.md)。

保留资源收缩、AAPT2 compact、可追溯的构建来源及正式签名。Preference 生成器由公开 Variant API 接入；
不要重做已通过的相同源码双构建，工具链变化按影响验证最终 APK、mapping、入口、资源和 native。
未证明 ART CPU、进程 PSS 或帧率改善时，不在发行说明中宣称这些收益。
