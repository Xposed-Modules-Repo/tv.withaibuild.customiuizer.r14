"""Regression checks for the exact A14 Control Center owner-release hook."""

import importlib.util
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = REPO_ROOT / "app/src/main/java"
CONTROL_CENTER_PATH = SOURCE_ROOT / "tv/withaibuild/customiuizer/mods/utils/ControlCenterPluginRuntime.kt"
STATUS_BAR_PATH = SOURCE_ROOT / "tv/withaibuild/customiuizer/mods/SystemUIControlCenterHooks.kt"
HOOK_INSTALL = (
    'ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.ControlCenterWindowViewImpl", '
    'classLoader, "destroy", destroyHook)'
)
VALID_CALLBACK = """
val destroyHook = object : MethodHook() {
    override fun before(param: BeforeHookCallback) {
        if (!lease.active) return
        val thisObject = param.getThisObject() as? View ?: return
        ModuleHelper.guarded {
            controlCenterMachine.clear(System.identityHashCode(thisObject))
        }
    }
}
"""


class GestureOwnerCleanupInvariantsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        spec = importlib.util.spec_from_file_location("gesture_cleanup_invariants", REPO_ROOT / "tools/check-invariants.py")
        cls.mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.mod)

    def check_control_center(self, text):
        return self.mod.check_gesture_detach_cleanup(CONTROL_CENTER_PATH, text)

    def assert_rejected(self, text):
        findings = self.check_control_center(text)
        self.assertEqual(1, len(findings))
        self.assertEqual("gesture-detach-cleanup", findings[0].rule)

    def test_current_production_source_has_owner_release(self):
        self.assertEqual([], self.check_control_center(CONTROL_CENTER_PATH.read_text(encoding="utf-8")))

    def test_registered_destroy_clears_its_callback_owner(self):
        self.assertEqual([], self.check_control_center(VALID_CALLBACK + HOOK_INSTALL))

    def test_destroy_without_clear_is_rejected(self):
        self.assert_rejected(VALID_CALLBACK.replace("controlCenterMachine.clear(System.identityHashCode(thisObject))", "") + HOOK_INSTALL)

    def test_clear_of_another_owner_is_rejected(self):
        self.assert_rejected(VALID_CALLBACK.replace("identityHashCode(thisObject)", "identityHashCode(otherView)") + HOOK_INSTALL)

    def test_whole_machine_clear_is_rejected(self):
        self.assert_rejected(VALID_CALLBACK.replace("clear(System.identityHashCode(thisObject))", "clear()") + HOOK_INSTALL)

    def test_clear_in_an_unregistered_callback_is_rejected(self):
        self.assert_rejected(VALID_CALLBACK + HOOK_INSTALL.replace("destroyHook)", "anotherHook)"))

    def test_clear_outside_destroy_callback_is_rejected(self):
        without_clear = VALID_CALLBACK.replace("controlCenterMachine.clear(System.identityHashCode(thisObject))", "")
        self.assert_rejected(without_clear + HOOK_INSTALL + "\ncontrolCenterMachine.clear(System.identityHashCode(thisObject))")

    def test_obsolete_inherited_detach_target_is_rejected(self):
        self.assert_rejected(VALID_CALLBACK + HOOK_INSTALL.replace('"destroy"', '"onDetachedFromWindow"'))

    def test_additional_obsolete_detach_target_is_rejected(self):
        self.assert_rejected(VALID_CALLBACK + HOOK_INSTALL + "\n" + HOOK_INSTALL.replace('"destroy"', '"onDetachedFromWindow"'))

    def test_commented_clear_cannot_satisfy_release(self):
        self.assert_rejected(VALID_CALLBACK.replace("controlCenterMachine.clear", "// controlCenterMachine.clear") + HOOK_INSTALL)

    def test_status_bar_still_requires_detach(self):
        valid = '"onDetachedFromWindow" statusBarMachine.clear(System.identityHashCode(thisObject))'
        self.assertEqual([], self.mod.check_gesture_detach_cleanup(STATUS_BAR_PATH, valid))
        findings = self.mod.check_gesture_detach_cleanup(STATUS_BAR_PATH, valid.replace('"onDetachedFromWindow"', '"destroy"'))
        self.assertEqual(1, len(findings))


if __name__ == "__main__":
    unittest.main()
