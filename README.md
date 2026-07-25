# 米客 A14（CustoMIUIzer A14）

**米客 A14** 是面向 **HyperOS 1 / Android 14** 的独立维护版系统定制 Xposed 模块，基于 [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) 的功能参考，采用独立包名 `tv.withaibuild.customiuizer.r14` 与版本线。

## 主要定制范围

- **状态栏**：图标、文本、电池样式、步数、日期、点击解锁、锁屏超时、电池/设备温度等。
- **控制中心与音量**：音量计时器、控制中心样式、亮度/音量条、自动亮度控制等。
- **锁屏**：充电信息、锁屏手势、通知、快捷方式等。
- **桌面**：隐私文件夹、桌面手势、图标与动画等。
- **系统交互**：导航栏、自定义动作、系统动画、秒针显示、天气查询、手电筒等。
- 本模块保持 **libxposed API 101**，不对 Android 15/16 做兼容承诺。

## 使用要求

- 仅支持 **Android 14（SDK 34）** 与 **`arm64-v8a`** 设备。
- 需要 **LSPosed / Vector** 等兼容 **libxposed API 101** 的框架。
- 不要与上游 `name.monwf.customiuizer` 或其他同源分支同时启用，否则可能产生重复 Hook。

## 链接

- 源码与完整更新日志：https://github.com/tomthenpc/customiuizer-a14
- 主仓库 release 页（含详细说明与构建产物）：https://github.com/tomthenpc/customiuizer-a14/releases