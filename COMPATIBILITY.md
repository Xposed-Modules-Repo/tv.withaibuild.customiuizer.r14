# A14 兼容矩阵

## 支持范围

| 项目 | 契约 |
| --- | --- |
| 系统 | HyperOS 1 / Android 14；运行入口要求 SDK 34 |
| APK | `tv.withaibuild.customiuizer.r14`，minSdk / targetSdk 34，`arm64-v8a` |
| API 101 | 最低运行基线，必经签名与初始化不依赖 API 102 专属类型 |
| API 102 | 能力探测后使用隔离桥接；动态作用域仍需框架授权 |
| 其他平台 | Android 13 / 15 / 16、其他 MIUI / HyperOS 大版本不支持 |

较新的 compileSdk 或构建工具不会扩展运行支持范围。跨版本移植需要独立任务和 API、ROM、生命周期、资源差异证据，不维护逐文件上游 parity。

`staticScope=true`。框架需启用模块并授予目标包作用域；四类普通应用选择（状态栏颜色、去除滚动边缘效果、媒体按键、闹钟兼容）按项目兼容策略仅在 Service API 102 及以上请求动态作用域，API 101 保持手动配置。`requestScope` 与对应监听接口在 Service API 101 已存在；该版本限制属于当前验证范围，不表示接口首次引入于 API 102。PermissionController 同时识别 AOSP 与 Google 包名，模块不能安全识别应用自绘授权说明页。

本地 238 试验在真实 Hook API ≥102 时给既有 SystemUI 初始化 Hook 设置 `systemui.init` ID；API 101 走原安装路径。可选 setter 失败在安装前局部降级，不重试已失败的 `intercept`。API 102 调用留在独立且禁止 R8 内联的桥中；未启用自动热重载、显式 Hook 替换或 entry detach。两种 API 的实机结果仍需分别验证，见 [试用说明](docs/API102_LOCAL_TRIAL.md)。

## 证据与限制

| 证据 | 能证明的范围 |
| --- | --- |
| STATIC / SOURCE | 当前代码、配置与契约符合检查 |
| BUILD | 指定提交编译、测试、lint 或 APK 检查通过 |
| ARTIFACT | 指定哈希 ROM 样本中存在类、字段或方法签名 |
| LOG / DEVICE / RUNTIME | 指定 APK、设备、ROM 与操作下的实际路径或行为 |
| CANDIDATE | 待取证猜测，不能作为生产兼容合同 |

已有样本来自 Xiaomi 13 / fuxi、`V816.0.7.0.UMCTWXM`、Android 14。样本哈希和插件 ClassLoader 边界保存在 [ROM 资料](docs/rom-intelligence/README.md)。这些历史资料不证明当前安装 APK、其他机型或全部功能已经验收。API 102 框架的实机结果也不能当作 API 101 框架验收。

## 当前功能边界

- 已下线缺少有效 A14 安装路径的浮动通知不自动隐藏、自动隐藏延迟、简约折叠视图、桌面去除启动动画及通知中心快捷应用；保留日历 / 时钟替换和其他通知功能。六个已隐藏入口也从配置目录移除，清理范围和升级规则见 [功能退役说明](docs/maintenance/A14_FEATURE_RETIREMENTS.md)。
- “修复动画”保留 `SpringAnimator.getSpringForce(FFF)` 路径，移除依赖旧 RectFSpringAnim 字段的分支；不猜测新动画 Bundle 的语义。
- 网速隐藏低速、低速阈值和隐藏 `B/s` 仅用于详细样式 2 / 3。原生样式下设置控件禁用，原值保留；改回详细样式后恢复。切换样式涉及 Hook 的安装 / 退出时仍需重启 SystemUI；已安装详细 Hook 的格式子值通过观察者更新。刷新间隔、文字样式等独立能力保留。
- 国际版 ROM 隐藏“扫描时禁止应用远程检测”，即使旧备份启用也不安装该 Feature。2026-10-11 对 fuxi / `V816.0.7.0.UMCTWXM` 的 Global GuardProvider APK（SHA-256 `1b767096d88e61b51e1f5db928eb963071ba851f47da0f20965ce262e31edeb6`）重跑生产 DexKit 查询无命中。该结果不证明国内版目标不存在；国内版实现保留，仍需查询命中与行为验证。

## 仍需专项取证

- 锁屏超时当前改资源 `config_lockScreenDisplayTimeout`，是否还需要运行期方法取决于目标 ROM；不猜测上游方法。
- 单个 fuxi 样本的缺失目标不代表全部 HyperOS 1 机型均移除同一系统能力；当前下线属于 A14 产品范围取舍，恢复需要新的完整安装链和实机证据。

涉及通知的实机验收应检查实际频道路由、重要性持久化（`mImportance` / `mUserLockedFields`）、六项扩展菜单完整可点，以及默认频道 / Hybrid 回退。状态栏和设置页改动应覆盖重附着、锁屏、配置切换、搜索导航、列表滚动与返回重进。报告具体 APK revision、开关、ROM、操作和日志；缺失项保持未验证。
