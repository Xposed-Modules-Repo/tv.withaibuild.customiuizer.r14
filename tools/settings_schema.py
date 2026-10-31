"""Read the settings data shared by Compose generation and repository audits.

The element view is for existing static analyzers, never Android UI inflation.
"""

from __future__ import annotations

import json
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID_NS = "http://schemas.android.com/apk/res/android"
AUTO_NS = "http://schemas.android.com/apk/res-auto"
TYPES = {
    "screen": "PreferenceScreen",
    "section": "tv.withaibuild.customiuizer.prefs.PreferenceCategoryEx",
    "action": "tv.withaibuild.customiuizer.prefs.PreferenceEx",
    "toggle": "tv.withaibuild.customiuizer.prefs.CheckBoxPreferenceEx",
    "choice": "tv.withaibuild.customiuizer.prefs.ListPreferenceEx",
    "slider": "tv.withaibuild.customiuizer.prefs.SeekBarPreference",
    "color": "tv.withaibuild.customiuizer.prefs.ColorPreferenceEx",
    "text": "tv.withaibuild.customiuizer.prefs.EditTextPreferenceEx",
    "multi": "androidx.preference.MultiSelectListPreference",
}
NAMESPACES = {"android": ANDROID_NS, "app": AUTO_NS}


def read_definition(path: Path) -> dict:
    node = json.loads(path.read_text(encoding="utf-8"))

    def validate(value: dict) -> None:
        if not isinstance(value, dict) or value.get("type") not in TYPES:
            raise ValueError(f"Invalid settings node in {path}")
        if set(value) - {"type", "attributes", "children"}:
            raise ValueError(f"Unknown settings fields in {path}")
        for name, attribute in value.get("attributes", {}).items():
            prefix, _, key = name.partition(":")
            if prefix not in NAMESPACES or not key or not isinstance(attribute, str):
                raise ValueError(f"Invalid setting attribute {name!r} in {path}")
        for child in value.get("children", []):
            validate(child)

    validate(node)
    if node["type"] != "screen":
        raise ValueError(f"Settings page must have a screen root: {path}")
    return node


def element_view(node: dict) -> ET.Element:
    attributes = {}
    for name, value in node.get("attributes", {}).items():
        namespace, key = name.split(":", 1)
        attributes[f"{{{NAMESPACES[namespace]}}}{key}"] = value
    element = ET.Element(TYPES[node["type"]], attributes)
    element.extend(element_view(child) for child in node.get("children", []))
    return element


def source_directory(resource_xml: Path) -> Path:
    """Resolve the repository definition directory from a resource XML directory."""
    # app/src/main/res/xml -> app/settings; temporary XML test fixtures stay local.
    candidate = resource_xml.parents[3] / "settings" if len(resource_xml.parents) >= 4 else resource_xml
    return candidate if candidate.is_dir() else resource_xml


def preference_sources(directory: Path) -> list[Path]:
    directory = source_directory(directory)
    definitions = sorted(directory.glob("prefs_*.json"))
    return definitions or sorted(directory.glob("prefs_*.xml"))


def read_element(path: Path) -> ET.Element:
    if path.suffix == ".json":
        return element_view(read_definition(path))
    candidate = source_directory(path.parent) / (path.stem + ".json")
    if candidate.is_file():
        return element_view(read_definition(candidate))
    # XML is accepted only for isolated analyzer fixtures and external legacy input.
    return ET.parse(path).getroot()
