# 米客 A14｜CustoMIUIzer A14

简体中文 | [English](README_EN.md)

面向 **HyperOS 1 / Android 14 / arm64-v8a** 的系统界面与交互定制模块，需要 **libxposed API 102 或更高版本**。

[下载正式 APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/267-r14.26.7/CustoMIUIzer-A14-r14.26.7.apk) · [发行说明](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/267-r14.26.7) · [更新记录](CHANGELOG_CN.md)

[Telegram 交流群](https://t.me/customiuizerA14)

## 当前正式版

`r14.26.7` · versionCode `267` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.26.7.apk` (5,997,909 bytes)

SHA-256: `80f32bd50fec08a8a0ffbdfea46878fc2a9a5e0d54a15b883b95d4286824b031`

[源码提交](https://github.com/thetvplus/customiuizer-a14/commit/8a9ce0d8dcbd3c16601b3e543247674677c69f3e): `8a9ce0d8dcbd3c16601b3e543247674677c69f3e`

## 本版变化

自 r14.26.5 起的变更：

- 可信 Wi-Fi 网络改用扫描缓存、系统广播和按需刷新，快速返回页面、系统拒绝扫描或等待超时后遵循请求冷却；可信蓝牙设备移除 15 秒循环，按进入页面、系统状态事件和手动操作获取缓存，空结果也会结束等待。
- 备份的筛选、编码和文件写入移至后台，与恢复共用单次操作入口；页面销毁后结束已开始的文件操作并关闭流，避免重复写入和过期成功提示，保留原备份格式与系统文件选择器。
- 语言刷新在 Fragment 状态已保存时延后到恢复阶段，搜索索引完整生成后再发布；编辑草稿按稳定设置键恢复，文本与数值弹窗在横屏键盘遮挡时支持整体滚动。
- 授予可信网络所需权限后继续打开原列表，Wi-Fi 自动设置添加当前网络时也会继续原操作；目录选择只接受可确认的本地存储目录，取消或无效选择保留原设置。
- 系统凭据确认只根据系统返回结果提示成功，恢复待处理请求时不重复启动；不把普通 Activity 保存状态当作正在认证。
- 前台应用监控不再随网速控制器构造重复安装 Hook；基础初始化和 Application.attach 共用真实目标的安装所有权，音频、闹钟与亮度回调跟随当前实例。修正控制中心可空 Boolean 签名，避免自定义列数时快捷按钮消失；重建时绑定实际左侧适配器。更新后重启设备。
- 截图目录按实际存储卷和 MediaStore 图片目录边界校验，交由系统创建子目录；不支持的自定义目录回退原生位置，保留宿主已有直接路径语义，格式扩展名与 MIME 同步。

## 安装与兼容

1. 下载并安装正式 APK；同签名覆盖安装保留应用数据。
2. 在 Xposed 框架中启用模块并选择所需作用域。
3. 重启相关进程或设备，按设置页提示启用功能。

其他 Android / HyperOS 大版本不在支持范围内。请勿与上游或其他 CustoMIUIzer 派生模块同时启用。

[使用说明与支持](https://github.com/thetvplus/customiuizer-a14/tree/8a9ce0d8dcbd3c16601b3e543247674677c69f3e) · [兼容矩阵](https://github.com/thetvplus/customiuizer-a14/blob/8a9ce0d8dcbd3c16601b3e543247674677c69f3e/COMPATIBILITY.md)

此仓库只维护正式下载说明。开发分支的未发布内容不代表本 APK 的功能或验收结果。
