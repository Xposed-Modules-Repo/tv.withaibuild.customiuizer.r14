# API 102 原生安装

r14.23.3 起，`minApiVersion=102`、`targetApiVersion=102`、`staticScope=true`。
API 101 不再支持。此前 238–241 试验的兼容桥、能力位和安装回退已移除；阶段记录保存在 Git 历史。

## 安装与生命周期

`HookInstallation` 直接调用原生 HookBuilder，供方法、构造器及基础设施 ID 安装使用。
优先级和 `PASSTHROUGH` 模式保持一致；业务回调仍由 `HookerClassHelper` 管理参数改写、提前返回、
原方法异常及 after 恢复，致命错误必须传播。该适配器承担业务语义，不能因最低 API 升级而删除。

仅为既有三个基础设施安装点使用固定 ID：

| 安装点 | ID |
| --- | --- |
| SystemUIInitializer.init(boolean) | `systemui.init` |
| PluginFactory.createPlugin 的所有声明重载 | `systemui.plugin.create` |
| 已选择目标进程的 Application.attach(Context) | `application.attach` |

ID 由模块和 executable 隔离，同 ID 可用于不同重载。ID 不代替一次安装状态和所有者管理。
设置 ID 失败时停止该次安装，不安装无 ID 回退；`intercept` 永不重试。
安装后回读 ID：相等才记录 `<id> (confirmed)`；普通回读失败只记诊断，保留 handle，致命错误传播。

不启用自动热重载、显式 replaceHook 或 detach。它们需要独立的旧代任务、JNI、Receiver / Observer、
系统持有对象和 ClassLoader 交接设计。参考 [官方 API 102 源码](https://repo.maven.apache.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0-sources.jar)。

## Service 与 R8

动态作用域由 `XposedServiceManager` 直接使用 Service SDK。每次选择只读一次 scope Binder 快照，
仅请求尚未授权的包；批准仍由框架决定。异步失败提示只持有 Application Context。
偏好镜像继续核对真实提交结果，并保留每个连接的一次重试预算。

删除旧桥的禁止内联规则；保留 Hooker 与设置进程的 compileOnly 类加载边界、入口和 JNI 规则。
最终 APK 检查 `setId/getId`、禁用热重载与替换、ServiceLoader 和入口；
[DexSQL](rom-intelligence/DEXSQL.md) 可作产物核对，不能代替 API 102 框架的安装日志与设备验收。
