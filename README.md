# 米客 A14｜CustoMIUIzer A14

简体中文 | [English](README_EN.md)

面向 **HyperOS 1 / Android 14 / arm64-v8a** 的系统界面与交互定制模块，需要 **libxposed API 102 或更高版本**。

[下载正式 APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/281-r14.26.11/CustoMIUIzer-A14-r14.26.11.apk) · [发行说明](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/281-r14.26.11) · [更新记录](CHANGELOG_CN.md)

[Telegram 交流群](https://t.me/customiuizerA14)

## 当前正式版

`r14.26.11` · versionCode `281` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.26.11.apk` (5,989,293 bytes)

SHA-256: `4a2d34d1e6ffd42c895f8def321932b1a0972a4ee12c51dc084afd7bca752410`

[匿名源码快照](https://github.com/thetvplus/customiuizer-a14/commit/052e5cf98764acfb052e6f0567a215a71bc2230b): `052e5cf98764acfb052e6f0567a215a71bc2230b`

## 本版变化

自 r14.26.9 起的变更：

- 重整动态岛设置：媒体、消息通知、实时活动和系统状态提示分组呈现；媒体显示与返回共用手选应用名单，消息按所选应用显示，避免两份媒体名单冲突。保留已有设置、备份和搜索定位。
- 上岛效果统一为「无」「瞬收」「吸入」：无保留桌面原生返回，瞬收不捕获页面，吸入沿用原生窗口弹簧与安全捕获。移除「简化动效」及旧效果范围，继续遵循系统动画和省电状态。
- 调整岛体承接与回弹、居中和左右位置的边界及微调；岛上切歌时为同一媒体会话提供有限加载保留，减少切歌期间媒体岛提前消失。
- 为不含 WindowElement 的旧版 HyperOS 1 桌面补齐手势返回上岛与吸入，保留原生返回、中断恢复和安全窗口限制。原生交接丢失时有限等待后恢复媒体岛，迟到页面捕获不再提前终止返回进度。更新后重启设备。
- 修复启用 Dock 背景后，最近任务或返回桌面可能触发系统平滑圆角原生崩溃的问题；保留原有背景和布局。修复最近任务隐藏 Dock 后的残留模糊区域。
- Dock 纯色面板不再创建模糊；磨砂玻璃复用原生材质，在布局稳定后同步边界、透明度与可见性，随系统模糊及省电状态回退，静止时停止逐帧同步。
- 修复全局窗口模糊开关修改后未及时生效的问题，保留系统开发者、省电及热状态限制；最近任务模糊比例按实际调用隔离，动态岛局部过渡不再受全局窗口模糊开关牵连。
- 音乐波形保留五段实测频率映射与轻微显示补偿，平滑抬起和回落；音乐与录音采样最多 10 Hz，显示仍随屏幕帧插值，静音稳定后停帧。
- 统一设置行、分组、搜索和动作编辑器的间距与圆角，修正单行垂直居中和长当前值排版；搜索增加输入引导、准备及无结果提示，菜单子项直接切换，保留页面动画与系统动态取色。
- 设置联动复用未变化的行与分组，搜索索引按语言复用；修正解锁快捷方式的目标应用，统一键盘竖屏和横屏底部间距名称并保留原值，明确仅支持 Gboard。
- Duo 飞行模式图标按实际图形居中，自动尺寸统一使用像素计算，保留现有布局与用户字号配置。

## 安装与兼容

1. 下载并安装正式 APK；同签名覆盖安装保留应用数据。
2. 在 Xposed 框架中启用模块并选择所需作用域。
3. 重启相关进程或设备，按设置页提示启用功能。

其他 Android / HyperOS 大版本不在支持范围内。请勿与上游或其他 CustoMIUIzer 派生模块同时启用。

[使用说明与支持](https://github.com/thetvplus/customiuizer-a14/tree/052e5cf98764acfb052e6f0567a215a71bc2230b) · [兼容矩阵](https://github.com/thetvplus/customiuizer-a14/blob/052e5cf98764acfb052e6f0567a215a71bc2230b/COMPATIBILITY.md)

此仓库只维护正式下载说明。开发分支的未发布内容不代表本 APK 的功能或验收结果。

原构建源码标识：`eea6315fe8ed309575f5d0c97e62df42de935dd9`。签名 APK 与原始 metadata 未修改；快照仅整理历史及文档隐私。
