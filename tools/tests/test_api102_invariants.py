"""Tests for the API 102 isolation invariants in tools/check-invariants.py."""

import importlib.util
import sys
import types
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
CHECK_INVARIANTS = REPO_ROOT / "tools" / "check-invariants.py"
SOURCE_ROOT = REPO_ROOT / "app" / "src" / "main" / "java"


def _load_check_invariants() -> types.ModuleType:
    spec = importlib.util.spec_from_file_location("check_invariants", CHECK_INVARIANTS)
    module = importlib.util.module_from_spec(spec)
    sys.modules["check_invariants"] = module
    if spec.loader is not None:
        spec.loader.exec_module(module)
    return module


class Api102IsolationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.mod = _load_check_invariants()

    def _fake_path(self, rel: str) -> Path:
        return SOURCE_ROOT / rel

    def test_setId_only_allowed_in_bridge(self):
        text = """
package tv.withaibuild.customiuizer.mods.utils

fun other() {
    builder.setId("x")
}
"""
        findings = self.mod.check_api102_isolation(self._fake_path("tv/withaibuild/customiuizer/mods/Other.kt"), text)
        self.assertEqual(1, len(findings))
        self.assertIn("Api102HookBridge", findings[0].detail)

    def test_setId_allowed_in_bridge(self):
        text = """
package tv.withaibuild.customiuizer.mods.utils

fun bridge(builder: XposedInterface.HookBuilder) = builder.setId("x")
"""
        findings = self.mod.check_api102_isolation(
            self._fake_path("tv/withaibuild/customiuizer/mods/utils/Api102HookBridge.kt"), text
        )
        self.assertEqual(0, len(findings))

    def test_replaceHook_forbidden(self):
        text = """
fun swap(handle: XposedInterface.HookHandle) = handle.replaceHook(hooker)
"""
        findings = self.mod.check_api102_isolation(self._fake_path("tv/withaibuild/customiuizer/mods/Other.kt"), text)
        self.assertEqual(1, len(findings))
        self.assertIn("replaceHook", findings[0].detail)

    def test_hotReload_params_forbidden(self):
        text = """
override fun onHotReloading(param: XposedModuleInterface.HotReloadingParam) {}
override fun onHotReloaded(param: XposedModuleInterface.HotReloadedParam) {}
"""
        findings = self.mod.check_api102_isolation(self._fake_path("tv/withaibuild/customiuizer/mods/Other.kt"), text)
        self.assertEqual(2, len(findings))

    def test_getApiVersion_only_allowed_in_entry(self):
        text = """
fun callback() = getApiVersion()
"""
        findings = self.mod.check_api102_isolation(self._fake_path("tv/withaibuild/customiuizer/mods/Other.kt"), text)
        self.assertEqual(1, len(findings))

    def test_getApiVersion_allowed_in_mainModule(self):
        text = """
XposedApiCapabilities.initialize(getApiVersion())
"""
        findings = self.mod.check_api102_isolation(
            self._fake_path("tv/withaibuild/customiuizer/MainModule.java"), text
        )
        self.assertEqual(0, len(findings))

    def test_installation_bridge_has_capability_guard_and_legacy_fallback(self):
        facade = (SOURCE_ROOT / 'tv/withaibuild/customiuizer/mods/utils/HookInstallerFacade.kt').read_text(encoding='utf-8')
        guarded = facade.index('id != null && XposedApiCapabilities.supportsStableHookId()')
        bridge = facade.index('Api102HookBridge.interceptWithStableId')
        self.assertLess(guarded, bridge)
        self.assertIn('XposedHelpers.findAndHookMethod(hookClass, methodName', facade[bridge:])
        java = (SOURCE_ROOT / 'tv/withaibuild/customiuizer/mods/utils/XposedHelpers.java').read_text(encoding='utf-8')
        self.assertNotIn('Api102HookBridge', java)

    def test_r8_keeps_optional_bridge_out_of_api101_callers(self):
        rules = (REPO_ROOT / 'app/proguard-rules.pro').read_text(encoding='utf-8')
        self.assertRegex(rules, r'(?m)^-keep,allowobfuscation class tv\.withaibuild\.customiuizer\.mods\.utils\.Api102HookBridge \{ \*; \}')

    def test_systemui_hook_id_does_not_enable_hot_reload(self):
        coordinator = (SOURCE_ROOT / 'tv/withaibuild/customiuizer/mods/utils/SystemUiBootstrapCoordinator.kt').read_text(encoding='utf-8')
        self.assertIn('ModuleHelper.findAndHookMethodWithId(', coordinator)
        self.assertIn('Api102HookBridge.STABLE_ID_SYSTEMUI_INIT', coordinator)
        metadata = (REPO_ROOT / 'app/src/main/resources/META-INF/xposed/module.prop').read_text(encoding='utf-8')
        self.assertIn('minApiVersion=101', metadata)
        self.assertNotIn('autoHotReload=true', metadata)


if __name__ == "__main__":
    unittest.main()
