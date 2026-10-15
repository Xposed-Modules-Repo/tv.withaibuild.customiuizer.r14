# DexSQL 实测评估（2026-10-15）

结论：纳入可选开发工具，保留现有运行时 DexKit 和 ROM 门禁。
直接沿用 `codex/api102-hook-id-local-20261015`，起点 `57c92de`；未切分支、回退或更改升级配置。
当前 JVM 25、AGP 9.4.1、Kotlin 2.4.21、DexKit 2.3.0、API 101 / 102 隔离策略保持。

## 工具与接入

官方 latest 实际返回 [v0.1.0](https://github.com/LSPosed/dexsql-release/releases/tag/v0.1.0)，
`prerelease=false`，发布于 2026-10-15。Windows x64 可执行文件 21,365,760 字节，SHA-256：
`f354ebcf715de9a66ef8ebe08b2b36807ab58e25cd5a1c0812e497fc1fcfc448`，与发布 API digest 一致。
安装在用户目录，保留同版本 LICENSE / LICENSE-AUXILIARY，并备份 Codex 配置后登记 stdio MCP。

独立 stdio probe 真实完成 initialize、tools/list、resources/list/read、open/query/decode、
Manifest / 资源查询及 close。12 个工具可见，返回 schema 与内嵌文档一致。
本次聊天的原生工具列表未热加载 DexSQL；本轮调用通过临时 stdio 客户端完成，
不能把登记成功描述为当前聊天已有原生 DexSQL 工具。下次连接需以客户端工具列表核实。

[官方文档](https://dexsql.lsposed.org/docs/) 描述直接读取 DEX、复用数据集和 SQL 关系查询。
本机观察到 MCP 服务另启 worker 进程；仅测父进程会严重低估内存。
引擎闭源且目前是初始公开版本，无法审计其内部解析 / 优化实现，也没有长期接口稳定性证据。
因此不设为必需 CI，不替代独立解析器，不承诺其他 APK / DEX 格式也有相同结果。

## 样本与准确性

设备两次以上稳定返回 `device`。本轮 fingerprint：
`Xiaomi/fuxi_global/fuxi:14/UKQ1.230804.001/V816.0.7.0.UMCTWXM:user/release-keys`。
截图、插件和 Global GuardProvider 原件由 ADB 读取；宿主使用历史原件，重新与手机 SHA-256 核对一致。
GuardProvider 通过 `pm list packages -f -u` 找到保留在 product 的 APK；这不证明当前用户已安装或运行该包。

| 输入 | 字节 | SHA-256 | 类 / 方法定义 / 字段定义 |
| --- | ---: | --- | --- |
| MIUIScreenshot | 8,325,095 | `78f1cb8afd6e231f9701e3432a753fc5910876fb2a25c3da8a6ed60a3ff07ad0` | 6,353 / 40,675 / 63,421 |
| Global GuardProvider | 1,618,253 | `1b767096d88e61b51e1f5db928eb963071ba851f47da0f20965ce262e31edeb6` | 2,267 / 10,951 / 4,556 |
| MIUISystemUIPlugin | 18,737,582 | `3dafd9e068ebee7e88344ae1c7d146c7e2d41e79b5c52b7736cd3e58be0cc999` | 9,333 / 61,642 / 168,438 |
| MiuiSystemUI | 54,423,617 | `5d8f2fe0b65d8a1a947b4280f8053b524f8c5de73f48a74f8792d415ae76e513` | 19,926 / 88,748 / 180,223 |
| 本地模块 238 | 3,799,501 | `e19c8cf25192ac95750d76c47372ee6be3122b97ec282efbc1cdce9b5c61a386` | 2,388 / 10,404 / 6,625 |

上述声明数量逐项与 `rom_inventory.py` 独立解析一致。方法 / 字段定义数不同于 DEX ID 引用数。

- **截图混淆目标**：SQL 与手机 DexKit 均返回唯一方法
  `Ln5/d$a;->e(Landroid/content/Context;Landroid/graphics/Bitmap;Landroid/net/Uri;Ljava/io/OutputStream;Landroid/graphics/Bitmap$CompressFormat;Lcom/miui/screenshot/l0;ZZ)Z`。
  参数数量 8，第 5 个是 CompressFormat，满足生产防护条件。
  SQL 精确相等搜索为零，子串搜索为一：完整常量是
  `saveBitmapToUri: external storage is not mounted`。这是查询语义差异，不能误报为目标消失。
- **调用 / 字段 / Smali**：该目标有两个直接调用者 `Ln5/d$a;->d(...)Z`、`Ln5/d;->c(...)Z`，
  SDK 36 dexdump 独立确认两条 invoke。decode 返回方法体、分支和 catch；字段交叉引用可见 JPEG 等读取。
- **插件边界**：宿主四个 DEX + 插件三个 DEX 联合查询，音量按钮两个方法声明和四个字段声明均落在插件
  `classes.dex`，不是宿主。`mIsZen` 的构造写入及 updateState / setIcon 读取也可定位。
  与历史合同相符，不能据此代替实际 plugin ClassLoader 的运行验证。
- **GuardProvider**：两个子串同时位于同一方法的查询为零；手机 DexKit 三轮也均为零。
  只确认当前 Global 样本，不推断国内 ROM、其他版本或功能运行结果。
- **API 102**：本地 238 APK 只有 `Lr3;->n(...)` 调用 setId；同产物 mapping 将 `r3` 映射到 Api102HookBridge，
  与已有 SDK dexdump 隔离证据一致。237 APK 查询为零。这是最终 DEX 对照，不是 API 101 / 102 实机验收。
- **APK / 协议边界**：238 Manifest 版本、包名及压缩资源的 app_name ID / 文本与 AAPT2 一致。
  原始 DEX 查询与 APK 查询相同；查询和 Smali 分页续读成功；非法 SQL / 损坏 DEX 被拒绝，原数据集仍可用；
  无 DEX 的测试 JAR 返回 `has_dex=false`，没有将其当成“目标不存在”。
- **查询回归**：用 JDK 25 的 javac 生成 Java 17 测试字节码，再由 SDK 36 D8 生成三个独立 DEX。
  两个 DEX 的 `method_idx` 都为 2，各自只含一个 GuardProvider 子串，联合查询正确返回零；
  加入同方法双子串后只命中唯一有效方法。排除包、错误的第 5 参数类型及同 DEX 不同方法分散子串均没有误命中。
  这些生成 fixture 只验证 SQL 条件，不验证生产 JVM 25 或 ROM 兼容性。

没有发现需要修改生产 Hook 的新缺陷。当前只有一个可信目标 ROM 版本，
未验证跨 ROM 的重命名 / 签名迁移；上述 237 / 238 对照也不能代替跨 ROM 测试。

## 耗时与资源

Windows 主机，默认线程数，不清 OS 文件缓存；每个样本三个新服务进程、每个数据集七次重复查询。
下表加载 / 首次查询为三轮中位数，重复查询为 21 次 RPC 中位数；包含 MCP JSON / stdio 往返。
加载使用 MCP 完整准备；不能与 CLI 按需加载直接比较。
内存每 20 ms 采样整个后代进程树；表内是每轮采样峰值的中位数，短峰可能漏采，RSS 求和可能重复计算共享页。

| DexSQL 输入 / 查询 | open RPC ms | 首次查询 ms | 重复查询 ms | 峰值 RSS MiB / 私有提交 MiB |
| --- | ---: | ---: | ---: | ---: |
| Screenshot / 参数约束 | 61.23 | 9.57 | 4.10 | 43.1 / 29.8 |
| Global GuardProvider / 双子串 | 25.85 | 6.45 | 3.17 | 32.9 / 12.4 |
| Plugin / 声明合同 | 57.20 | 4.49 | 1.69 | 57.7 / 44.4 |

服务启动到 initialize 约 8 ms，另计。close_dataset 后 worker 退出，观察到只剩服务父进程，
不据短时采样宣称长期没有泄漏。

手机上复用已有 `DexKitRomProbe.java` / DexKit 2.3.0 DEX harness，独立 dalvikvm64 进程，
SDK 34，每轮一次创建、首次查询、五次重复、负查询、关闭，再做 20 次创建 / 查询 / 关闭，三个新进程。
没有安装 / 替换模块，没有修改手机偏好。两种工具的结果集一致：截图一项、Global GuardProvider 零项。

| DexKit 手机输入 | create 中位 ms | 首次查询中位 ms | 首轮结束 PSS KiB（三轮） | 再做 20 轮后 PSS KiB（三轮） |
| --- | ---: | ---: | --- | --- |
| Screenshot | 34.48 | 19.30 | 50,742 / 49,033 / 49,234 | 51,234 / 49,763 / 49,905 |
| Global GuardProvider | 3.48 | 6.02 | 49,356 / 49,685 / 49,386 | 52,050 / 52,186 / 52,118 |

PSS 来自 `/proc/self/smaps_rollup`，包含 ART / JIT 和测量噪声；这不是模块宿主 PSS。
手机 ARM64 DexKit 与 Windows DexSQL 的硬件、完整准备范围、内存口径和启动边界不同，
不能据此排名“谁快几倍 / 更省内存”。未测 Hook 热路径、模块 CPU、应用启动或帧率改善。

同一截图 SQL 的主机 CLI 三轮中位：原始 APK 34.92 ms，持久缓存 47.01 ms；结果完全一致。
缓存创建约 59.29 ms、占 5,993,520 字节，默认仍校验来源。该样本没有缓存提速收益，
因此优先复用 MCP 数据集，不新增持久缓存管理层。

## 实施与放弃

- 增加五条可复用 SQL 和 [日常指引](DEXSQL.md)，连接现有 ROM 文档、工具分类与 Agent 入口。
  查询有 DEX 连接键、声明 / 引用区分和确定排序，已在上述真实样本执行。
- 不增加 Python MCP 框架、SQL→Hook 自动生成器、数据库服务或必需依赖；正式 MCP 已具备所需生命周期和限额。
- 不删 rom_inventory / contract / diff / JADX：它们的 provenance、离线门禁、人工确认分类和源码反编译职责仍存在。
- 不改 APK、运行时 DexKit、版本、签名或发行渠道。安装和取证脚本、原件、缓存、Smali、测量 JSON 均留在忽略目录
  `.di-evidence/dexsql-evaluation-20261015/`；本地探针 `probe.py`、`deep_probe.py`、`edge_probe.py`、`fixture_probe.py`、`run_device.py` 保留供复测。

## 仓库验证

- `python tools/verify.py full`：退出 0，JDK 25 编译、静态契约、JVM 测试和 lintDebug 通过；Gradle 3 分 33 秒。
  JUnit XML 合计 2,598 项，零失败 / 错误、2 项跳过。
- `python -m unittest discover -s tools/tests -p "test_*.py"`：510 项通过，5 项按既有条件跳过，约 96 秒。
- 五条 SQL 的真实样本查询、三 DEX 生成 fixture、CLI / MCP 结果和上述独立对照均完成。
  新文档 / SQL 的 UTF-8、LF、尾随空白和本地链接检查通过，`git diff --check` 通过。
- 本次只有开发查询与文档改动，复用已存在的 237 / 238 Release APK 进行 DEX 对照，没有重新生成或发布 APK。

这是一组已复核的开发取证能力，不构成全 ROM / 全功能 / 模块运行性能的验证通过标记。
