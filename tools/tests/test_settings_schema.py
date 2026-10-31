import json
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

from tools import compose_preferences as compose
from tools.settings_schema import ANDROID_NS, AUTO_NS, element_view, read_definition


class SettingsSchemaTests(unittest.TestCase):
    def read(self, node):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "page.json"
            path.write_text(json.dumps(node), encoding="utf-8")
            return read_definition(path)

    def test_rejects_unknown_control_types_and_namespaces(self):
        for node in (
            {"type": "unknown"},
            {"type": "screen", "attributes": {"unknown:key": "a"}},
            {"type": "screen", "children": [{"type": "toggle", "attributes": {"key": "a"}}]},
            {"type": "screen", "unknown": True},
        ):
            with self.subTest(node=node), self.assertRaises(ValueError):
                self.read(node)

    def test_preserves_dependency_visibility_and_exact_default_type_inputs(self):
        node = self.read({"type": "screen", "children": [{
            "type": "choice", "attributes": {
                "android:key": "pref_key_example", "android:defaultValue": "01",
                "android:dependency": "pref_key_enabled", "app:isPreferenceVisible": "false",
            },
        }]})
        root = element_view(node)
        child = root[0]
        self.assertEqual("01", child.get(f"{{{ANDROID_NS}}}defaultValue"))
        self.assertEqual("false", child.get(f"{{{AUTO_NS}}}isPreferenceVisible"))
        body = "\n".join(compose._page_body(root))
        self.assertIn('setDefaultValue("01")', body)
        self.assertIn('isVisible = false', body)
        self.assertGreater(body.index('dependency = "pref_key_enabled"'), body.index("addPreference(row1)"))
        self.assertLess(body.index("manager.setPreferences(root)"), body.index('dependency = "pref_key_enabled"'))
        self.assertGreater(body.index("root.onDetached()"), body.index('dependency = "pref_key_enabled"'))

    def test_generator_fails_instead_of_silently_dropping_a_new_control_attribute(self):
        root = ET.Element("PreferenceScreen")
        ET.SubElement(root, "tv.withaibuild.customiuizer.prefs.CheckBoxPreferenceEx", {
            f"{{{AUTO_NS}}}unhandledContract": "true",
        })
        with self.assertRaisesRegex(ValueError, "Untranslated"):
            compose._page_body(root)

    def test_resource_references_remain_compile_time_checked_and_literals_are_escaped(self):
        self.assertEqual("context.getText(R.string.example)", compose.text("@string/example"))
        self.assertEqual('"price \\$5\\nnext"', compose.text("price $5\nnext"))


if __name__ == "__main__":
    unittest.main()
