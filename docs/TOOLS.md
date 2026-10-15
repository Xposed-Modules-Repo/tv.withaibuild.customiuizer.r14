# 工具分类与调用依据

本次清理检查了跟踪文件中的直接调用、Python import / 动态加载、Gradle 任务、CI、配置、测试与文档。没有普通文本引用只说明缺少仓库内调用证据；离线诊断工具还需核对输入、输出与仍有效的维护用途。

| 类别 | 文件 / 分组 | 保留或清理依据 |
| --- | --- | --- |
| 1. 当前构建 / CI / 核心测试必须保留 | `verify.py`、`eol_check.py`、`check-invariants.py`、`audit-feature-semantics.py`、`check_observer_key_contract.py`、`hook_body_prefmap_scan.py`、`check_hotpath_alloc_budget.py`、`check_main_source_cleanliness.py` | `verify.py` 的 fast / full 实际调用；偏好、API、Feature、生命周期、编码和热路径门禁。 |
| 1 | `generate_preference_artifacts.py` | `app/build.gradle.kts` 的 `generatePreferenceArtifacts` 输入，生成设置分页、搜索和偏好目录；删除会破坏构建与功能。 |
| 1 | `ci_scope.py`、`ci_install_android_sdk.sh`、`check_ci_portability.py`、`ci_contract_scan.py`、`catalog_contract_probe.py`、`audit_hook_ownership.py`、`check_release_metadata.py` | `a14-ci.yml` 直接调用，或 `brutal_test_config.json` 的 hermetic commands / kill gates 调用。 |
| 1 | `brutal_test_runner.py`、`brutal_test_policy.py`、`brutal_gate_protocol.py`、`brutal_a14_mutators.py`、`brutal_a14_contract_scan.py`、`brutal_test_config.json` | CI 使用 hermeticity / determinism / required mutation；runner import protocol / policy，并动态加载 mutators，config 声明 apply checks。没有独立 kill gate 的候选继续单列，不能当成已验证覆盖。 |
| 1 | `source_hazard_scan.py` | config 的 fatal mutation 独立 kill gate；observer 检查器及当前架构 / 工具测试也复用其解析器。 |
| 1 | `extract_process_matrix.py` 与 `docs/rom-intelligence/A14_PROCESS_MATRIX.*`、`A14_PROCESS_EXCEPTIONS_GENERATED.md` | config 的 determinism command / outputs；catalog gate 和测试消费矩阵。生成物不是一次性历史报告。 |
| 1 | `apk_semantic_diff.py` | Full CI 两次无缓存 develop APK 的内容可重复性门禁；字节大小比较不能替代它。 |
| 1 | `requirements.txt`、`__init__.py`、`tools/tests/`、`HOTPATH_ALLOC_BASELINE.json`、`HOOK_BODY_PREFMAP_BASELINE.json` | 固定 PyYAML 用于解析真实 CI YAML；包导入、unittest discovery 和两项当前预算需要这些文件。历史批次命名的测试仍断言当前 production subject，不能按文件名删除。 |
| 2. 有明确维护 / 兼容性价值 | `apk_size_report.py`、`apk_size_delta.py` | 前者测量 APK 并比较两个 baseline 的预算；后者处理显式多变体 manifest、双源码 SHA、校验与 Markdown 报告，输入和职责不同。 |
| 2 | `build_debug_apk.py`、`build_revision.py`、`verify_apk_provenance.py` | 诊断构建限制 tracked clean HEAD，校验 AGP output metadata、variant / version、内嵌 revision 并输出 SHA-256；裸 assemble 命令没有完整替代这些保障。provenance verifier 也用于正式发布核验。 |
| 2 | `rom_inventory.py`、`rom-contract-scan.py`、`rom_target_diff.py` 与 ROM 测试 fixtures | 分别生成样本 provenance / ABI 清单、扫描 smali 必需契约、分类目标变化；输入和输出不同，现有测试覆盖各自行为。保留离线 HyperOS 1 / Android 14 兼容取证能力。 |
| 2 | `tools/dexsql/` 查询集与 [开发指引](rom-intelligence/DEXSQL.md) | 可选主机取证，补充字符串 / 调用 / 字段 / Smali；直接使用官方 CLI / MCP，不添加 APK 或 CI 依赖，不替代上述门禁。 |
| 2 | `legacy_semantic_compare.py` | [继承功能契约](maintenance/LEGACY_SEMANTIC_CALIBRATION.md) 的可重建差异入口；比较上游与 A14 的偏好、数组和 getter 默认值，当前 schema 验证器没有替代跨仓比较。结果需人工判断已批准的 A14 差异。 |
| 2 | `SOURCE_HAZARD_BASELINE.json` | `source_hazard_scan.py` 的默认 baseline；普通扫描按 fingerprint 区分历史与新命中，严格 mutation 模式不用它。它不是漏洞数量或实机结果。 |
| 2 | `scripts/verify.ps1`、`scripts/check-signing-config.ps1`、`scripts/test-check-signing-config.ps1` | Windows 验证入口和不输出秘密的外部签名 preflight / 回归。签名工具不能用普通 Gradle 构建替代。 |
| 3. 功能重复，已合并 | 旧 APK baseline 预算比较入口 | 指标差、compressed Top 20、退出码与绝对 APK / DEX 预算已合入 `apk_size_report.py --compare`；新增回归覆盖上升 / 下降、预算边界、ZIP 归因与条目增删。 |
| 4. 完整替代，已删除 | `tools/compare_apk_size.py` | 调用图没有构建 / CI / import 依赖，原有单独 CLI 功能已由上一行完整承接；不是仅凭无引用删除。 |
| 5. 暂时无法确认，保留 | `analyze_lsposed_log.py` | 支持当前包名、进程与 LSPosed 日志 / ZIP 离线分析，但缺少仓库调用、自动化 fixtures 和近期真实日志验收。没有等效替代的证据，保留待有样本时验证。 |

工具移除不改变 Android APK runtime 依赖。收口运行完整 Python 工具测试；修改生成器、CI 或独立 mutation 时还需对应 determinism / mutation 与构建门禁，详见 [TESTING.md](TESTING.md)。
