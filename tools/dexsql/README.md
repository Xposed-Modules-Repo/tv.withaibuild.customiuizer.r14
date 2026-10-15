# 可选 DexSQL 查询

用于开发期 APK / DEX 取证，不参与 Gradle、APK runtime 或默认 CI。
安装、MCP 操作、证据边界及实测结果见 [DexSQL 指引](../../docs/rom-intelligence/DEXSQL.md)。

每个 `.sql` 文件是一条可直接传给官方 CLI 或 MCP `query.sql` 的语句：

| 文件 | 输入 | 用途 |
| --- | --- | --- |
| `screenshot_candidates.sql` | MIUIScreenshot APK | 当前 DexKit 子串、排除包和第 5 个参数约束；生产入口要求唯一候选。 |
| `screenshot_callers.sql` | MIUIScreenshot APK | 字符串目标的直接调用者；仍需用 candidates 查询筛选、用 decode 复核。 |
| `guardprovider_candidates.sql` | GuardProvider APK | 同一 DEX、同一方法同时使用两个子串；零结果只约束输入样本。 |
| `volume_contract.sql` | SystemUI 宿主 + plugin APK | 查询音量按钮声明，配合 `dex_files` 确认来自哪个 APK。 |
| `api102_set_id_calls.sql` | 最终 R8 模块 APK | 查 `HookBuilder.setId` 的调用者；结合该 APK 的 mapping 复核隔离。 |
| `bootstrap_contract.sql` | SystemUI 宿主及插件 APK | 查询既有初始化 / 插件创建的声明与来源；boot framework 缺失仍是 UNKNOWN。 |
| `api102_bridge_calls.sql` | 最终 R8 模块 APK | 统计 setId / getId / replaceHook 的 invoke，逐项核对桥隔离与禁止调用。 |
| `api102_hook_ids.sql` | 最终 R8 模块 APK | 找到三个固定 ID 的指令使用位置；需结合 mapping 和设备回读。 |

修改生产匹配条件或 API 102 入口时同步审查对应查询。查询结果是 ARTIFACT 证据，
不能直接生成生产 Hook、推断实际 ClassLoader 或标记设备功能验收。
SQL 保留 `file_id` 连接键与确定排序；不要将字符串子串搜索改成精确相等。
