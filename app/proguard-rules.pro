# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-dontwarn io.github.libxposed.annotation.**
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

# Hook callbacks are loaded only inside LSPosed target processes. Prevent R8 from
# merging them into ordinary app/startup classes, which would make the settings
# app resolve the compileOnly libxposed API during process initialization.
-keep,allowobfuscation class * implements io.github.libxposed.api.XposedInterface$Hooker { *; }

# API 101 must not verify the API 102 setId/getId calls on an entry/common installation
# class. Keep the optional bridge as a separate class and prevent method inlining.
-keep,allowobfuscation class tv.withaibuild.customiuizer.mods.utils.Api102HookBridge { *; }

-keepnames class tv.withaibuild.customiuizer.MainActivity
-keepnames class tv.withaibuild.customiuizer.Credentials
-keepnames class tv.withaibuild.customiuizer.CredentialsShortcut
-keepnames class tv.withaibuild.customiuizer.PrefsProvider
-keepnames class tv.withaibuild.customiuizer.MainApplication
-keepnames class tv.withaibuild.customiuizer.tasker.UnlockSettings
-keepnames class tv.withaibuild.customiuizer.tasker.UnlockReceiver
-keepnames class tv.withaibuild.customiuizer.qs.AutoRotateService

# Obfuscation
-repackageclasses

-dontwarn kotlin.jvm.internal.SourceDebugExtension
-dontwarn android.**
-dontwarn miui.**
-dontnote android.**, miui.**, com.android.**
