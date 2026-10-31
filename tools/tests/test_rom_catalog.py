"""Offline host regressions; synthesized metadata, no vendor ROM or APK build."""
import contextlib
import io
import json
import os
from pathlib import Path
import sqlite3
import struct
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import rom_catalog as c


def archive(path, package="com.android.systemui", payload=b"not parsed as DEX"):
    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, "w") as z:
        z.writestr("AndroidManifest.xml", f'<manifest package="{package}" xmlns:android="http://schemas.android.com/apk/res/android" android:versionName="1.0"/>')
        z.writestr("classes.dex", payload)


def axml(utf8):
    strings = ["manifest", "package", "com.example.测试", "versionCode"]
    data, offsets = b"", []
    for text in strings:
        offsets.append(len(data))
        encoded = text.encode("utf-8" if utf8 else "utf-16le")
        data += (bytes([len(text), len(encoded)]) + encoded + b"\0" if utf8 else
                 struct.pack("<H", len(text)) + encoded + b"\0\0")
    pool_size = 28 + 4 * len(strings) + len(data)
    pool = struct.pack("<HHIIIIII", 1, 28, pool_size, len(strings), 0, 256 if utf8 else 0,
                       28 + 4 * len(strings), 0) + struct.pack("<4I", *offsets) + data
    attrs = struct.pack("<IIIHBBI", 0xffffffff, 1, 2, 8, 0, 3, 2)
    attrs += struct.pack("<IIIHBBI", 0xffffffff, 3, 0xffffffff, 8, 0, 16, 42)
    element = struct.pack("<HHIII", 0x102, 16, 36 + len(attrs), 1, 0xffffffff)
    element += struct.pack("<IIHHHHHH", 0xffffffff, 0, 20, 20, 2, 0, 0, 0) + attrs
    return struct.pack("<HHI", 3, 8, 8 + len(pool) + len(element)) + pool + element


class DexsqlDiscoveryTest(unittest.TestCase):
    def discover(self, suffix, explicit=False):
        with tempfile.TemporaryDirectory() as temporary:
            base = Path(temporary)
            for version in ("v0.1.0", "v0.2.0"):
                folder = base / "DexSQL" / version
                folder.mkdir(parents=True)
                for binary in ("dexsql", "dexsql.exe"):
                    (folder / binary).write_bytes(b"synthetic executable; never executed")
            override = base / "chosen-tool"
            override.write_bytes(b"explicit fixture")
            with patch.dict(os.environ, {"LOCALAPPDATA": str(base), "CODEX_HOME": str(base / "config"),
                                        "DEXSQL_EXE": ""}), \
                    patch.object(c.Path, "home", return_value=base), \
                    patch.object(c.shutil, "which", return_value=None), \
                    patch.object(c.sysconfig, "get_config_var", return_value=suffix):
                actual = c.find_dexsql(str(override) if explicit else None)
            expected = override if explicit else base / "DexSQL" / "v0.2.0" / ("dexsql" + suffix)
            self.assertEqual(actual, expected.resolve())

    def test_version_discovery_uses_native_executable_suffix(self):
        for suffix in ("", ".exe"):
            with self.subTest(suffix=suffix):
                self.discover(suffix)

    def test_explicit_executable_precedes_versioned_lookup(self):
        self.discover(".exe", explicit=True)


class FakeEngine:
    key = "engine-one"

    def __init__(self, values=None, fail=False):
        self.calls, self.builds, self.fail = 0, 0, fail
        self.values = values or [["classes.dex", "Lexample/Type;"]]

    def execute(self, argv):
        self.builds += 1
        if self.fail:
            raise c.CatalogError("interrupted build")
        out = Path(argv[argv.index("--output") + 1])
        (out / "cache.dxc2").write_bytes(b"official engine test stand-in")

    def query(self, path, sql, cached=True):
        self.calls += 1
        return "| a14_row |\n" + "\n".join(
            "| " + ":".join("V" + cell.encode().hex() for cell in row) + " |" for row in self.values)


class CatalogTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.base = Path(self.tmp.name)
        self.root, self.index = self.base / "roms", self.base / "index"
        self.file = self.rom("fuxi-cn") / "system_ext/priv-app/MiuiSystemUI/MiuiSystemUI.apk"
        archive(self.file)
        self.db = c.connect(self.index, create=True)
        self.addCleanup(self.db.close)

    def rom(self, name):
        rom = self.root / name
        prop = rom / "system/system/build.prop"
        prop.parent.mkdir(parents=True, exist_ok=True)
        prop.write_text("ro.build.version.sdk=34\nro.build.version.release=14\nro.mi.os.version.code=1\nro.build.version.incremental=V816.0.19.0.UMCCNXM\n")
        return rom

    def rows(self):
        return c.select_files(self.db, component="systemui")

    def test_incremental_inventory_deduplicates_without_parsing_dex(self):
        other = self.rom("fuxi-tw") / "product/MiuiSystemUI.apk"
        other.parent.mkdir(parents=True)
        other.write_bytes(self.file.read_bytes())
        original = self.file.read_bytes()
        first = c.update(self.db, self.root)
        self.assertEqual((first["files"], first["metadata_reads"], first["unique_artifacts"]), (2, 1, 1))
        with patch.object(c, "sha256", side_effect=AssertionError("no rehash")), patch.object(c, "inspect_artifact", side_effect=AssertionError("no parsing")):
            second = c.update(self.db, self.root)
        self.assertEqual((second["hashed"], second["reused"]), (0, 2))
        self.assertEqual(self.file.read_bytes(), original)
        self.assertEqual(self.rows()[0]["package"], "com.android.systemui")
        self.assertEqual(c.update(self.db, self.root, rehash=True)["hashed"], 2)

    def test_changed_file_invalidates_and_deletion_removes_location(self):
        c.update(self.db, self.root)
        old = self.rows()[0]
        archive(self.file, payload=b"changed content and length")
        with self.assertRaisesRegex(c.CatalogError, "stale"):
            c.source(old)
        stats = c.update(self.db, self.root)
        self.assertEqual(stats["hashed"], 1)
        self.assertNotEqual(old["sha256"], self.rows()[0]["sha256"])
        self.file.unlink()
        self.assertEqual(c.update(self.db, self.root)["removed"], 1)

    def test_walk_failure_rolls_back_and_missing_root_never_clears(self):
        c.update(self.db, self.root)
        with patch.object(c, "source_files", side_effect=OSError("unreadable partition")):
            with self.assertRaises(OSError):
                c.update(self.db, self.root)
        self.assertEqual(len(self.rows()), 1)
        empty = self.base / "empty"
        empty.mkdir()
        with self.assertRaises(c.CatalogError):
            c.update(self.db, empty)
        self.assertEqual(len(self.rows()), 1)

    def test_roles_do_not_include_systemui_package_prefixes_or_overlays(self):
        archive(self.root / "fuxi-cn/product/Accessibility.apk", "com.android.systemui.accessibility")
        archive(self.root / "fuxi-cn/product/MiuiSystemUIOverlay.apk", "com.miui.systemui.overlay")
        archive(self.root / "fuxi-cn/mi_ext/product/MiLauncherGlobal.apk", "com.mi.android.globallauncher")
        c.update(self.db, self.root)
        self.assertEqual(len(self.rows()), 1)
        self.assertEqual(c.select_files(self.db, component="launcher")[0]["component"], "poco")

    def test_duplicate_roms_share_one_cache_and_one_query(self):
        duplicate = self.rom("fuxi-tw") / "system_ext/MiuiSystemUI.apk"
        duplicate.parent.mkdir(parents=True)
        duplicate.write_bytes(self.file.read_bytes())
        c.update(self.db, self.root)
        engine = FakeEngine()
        self.assertEqual(c.prepare(self.db, self.index, self.rows(), engine)["built"], 1)
        self.assertEqual(engine.builds, 1)
        result = c.search(self.db, self.rows(), engine, "class", "Type")
        self.assertEqual((result["dexsql_calls"], len(result["results"])), (1, 1))
        self.assertEqual(len(result["results"][0]["sources"]), 2)
        result = c.search(self.db, self.rows(), engine, "class", "Type")
        self.assertEqual((result["dexsql_calls"], result["answer_hits"]), (0, 1))

    def test_cache_damage_engine_change_and_interrupted_rebuild(self):
        c.update(self.db, self.root)
        engine = FakeEngine()
        rows = self.rows()
        c.prepare(self.db, self.index, rows, engine)
        cached = c.cache_for(self.db, rows[0], engine)
        original = cached.read_bytes()
        engine.fail = True
        with self.assertRaises(c.CatalogError):
            c.prepare(self.db, self.index, rows, engine, rebuild=True)
        self.assertEqual(cached.read_bytes(), original)
        engine.key = "new-engine"
        with self.assertRaisesRegex(c.CatalogError, "Cache missing"):
            c.cache_for(self.db, rows[0], engine)
        engine.key = "engine-one"
        cached.write_bytes(b"damaged")
        with self.assertRaisesRegex(c.CatalogError, "modified"):
            c.cache_for(self.db, rows[0], engine)

    def test_concurrent_source_mutation_cannot_publish_an_answer(self):
        c.update(self.db, self.root)
        engine = FakeEngine()
        c.prepare(self.db, self.index, self.rows(), engine)
        query = engine.query
        def mutate(*args, **kwargs):
            result = query(*args, **kwargs)
            archive(self.file, payload=b"changed during execution")
            return result
        engine.query = mutate
        with self.assertRaisesRegex(c.CatalogError, "stale"):
            c.search(self.db, self.rows(), engine, "class", "Type")
        self.assertEqual(self.db.execute("SELECT count(*) FROM answers").fetchone()[0], 0)

    def test_v1_migration_retains_catalog_and_engine_identity_is_reused(self):
        c.update(self.db, self.root)
        self.db.execute("DROP TABLE answers")
        for column in ("scope", "hyperos", "identity_files"):
            self.db.execute("ALTER TABLE roms DROP COLUMN " + column)
        self.db.execute("PRAGMA user_version=1")
        self.db.commit()
        self.db.close()
        self.db = c.connect(self.index)
        self.addCleanup(self.db.close)
        self.assertEqual(len(self.rows()), 0)  # Legacy records fail closed until metadata is refreshed.
        c.update(self.db, self.root)
        self.assertEqual(len(self.rows()), 1)
        self.assertEqual(self.db.execute("PRAGMA user_version").fetchone()[0], c.SCHEMA)
        executable = self.base / "fake-dexsql.exe"
        executable.write_bytes(b"first engine")
        with patch.object(c, "find_dexsql", return_value=executable), patch.object(c, "run") as execute:
            execute.return_value.stdout = "dexsql test"
            first = c.Engine(db=self.db)
            self.assertEqual(execute.call_count, 1)
            self.assertEqual(c.Engine(db=self.db).key, first.key)
            self.assertEqual(execute.call_count, 1)
            executable.write_bytes(b"second engine with different content")
            self.assertNotEqual(c.Engine(db=self.db).key, first.key)
            self.assertEqual(execute.call_count, 2)

    def test_answer_invalidates_by_hash_engine_query_and_can_be_bypassed(self):
        c.update(self.db, self.root)
        engine = FakeEngine()
        rows = self.rows()
        c.prepare(self.db, self.index, rows, engine)
        c.search(self.db, rows, engine, "class", "Type")
        self.assertEqual(c.search(self.db, rows, engine, "class", "Type")["dexsql_calls"], 0)
        self.assertEqual(c.search(self.db, rows, engine, "class", "Type", fresh=True)["dexsql_calls"], 1)
        self.assertEqual(c.search(self.db, rows, engine, "class", "Other")["dexsql_calls"], 1)
        archive(self.file, payload=b"now changed")
        with self.assertRaisesRegex(c.CatalogError, "stale"):
            c.search(self.db, rows, engine, "class", "Type")
        c.update(self.db, self.root)
        with self.assertRaisesRegex(c.CatalogError, "Cache missing"):
            c.search(self.db, self.rows(), engine, "class", "Type")
        engine.key = "second-engine"
        with self.assertRaisesRegex(c.CatalogError, "Cache missing"):
            c.search(self.db, self.rows(), engine, "class", "Type")

    def test_answer_storage_and_pages_are_bounded(self):
        c.update(self.db, self.root)
        engine = FakeEngine([["classes.dex", "LA;"], ["classes2.dex", "LB;"]])
        c.prepare(self.db, self.index, self.rows(), engine)
        result = c.search(self.db, self.rows(), engine, "class", "L", limit=1)
        self.assertEqual(result["results"][0]["next_offset"], 1)
        self.assertEqual(result["results"][0]["rows"], [["classes.dex", "LA;"]])
        for i in range(130):
            c.search(self.db, self.rows(), engine, "class", str(i))
        self.assertEqual(self.db.execute("SELECT count(*) FROM answers").fetchone()[0], 128)
        engine.values = [["classes.dex", "L" + "x" * 70000]]
        c.search(self.db, self.rows(), engine, "class", "oversize")
        self.assertEqual(self.db.execute("SELECT count(*) FROM answers").fetchone()[0], 128)

    def test_status_and_locate_never_discover_or_scan(self):
        c.update(self.db, self.root)
        with patch.object(c, "discover_root", side_effect=AssertionError("discovery")), patch.object(c, "source_files", side_effect=AssertionError("scan")), patch.object(c, "inspect_artifact", side_effect=AssertionError("parse")):
            for command in (["status"], ["locate", "--component", "systemui"]):
                with contextlib.redirect_stdout(io.StringIO()) as output:
                    self.assertEqual(c.main(["--index-dir", str(self.index), *command]), 0)
                self.assertTrue(json.loads(output.getvalue()))

    def test_non_dex_and_invalid_archive_keep_unknown_status(self):
        resource = self.root / "fuxi-cn/product/Resource.apk"
        resource.parent.mkdir(parents=True)
        with zipfile.ZipFile(resource, "w") as z:
            z.writestr("AndroidManifest.xml", '<manifest package="com.example.resource"/>')
        invalid = self.root / "fuxi-cn/product/Invalid.jar"
        invalid.write_bytes(b"not an archive")
        c.update(self.db, self.root)
        resource_row = c.select_files(self.db, component="Resource.apk")[0]
        invalid_row = c.select_files(self.db, component="Invalid.jar")[0]
        self.assertEqual(resource_row["has_dex"], 0)
        self.assertIsNone(invalid_row["has_dex"])
        result = c.search(self.db, [invalid_row], FakeEngine(), "class", "Type")
        self.assertEqual(result["skipped"][0]["status"], "metadata_error")

    def test_root_movement_rehashes_and_preserves_rom_identity(self):
        c.update(self.db, self.root)
        new = self.base / "relocated"
        self.root.rename(new)
        result = c.update(self.db, new)
        self.assertEqual(result["hashed"], 1)
        self.assertTrue(c.source(self.rows()[0]).is_relative_to(new))

    def test_index_must_stay_outside_source_and_repository(self):
        with self.assertRaises(c.CatalogError):
            c.outside_sources(self.root / "index", self.root)
        with self.assertRaises(c.CatalogError):
            c.outside_sources(c.REPO / "index", self.root)

    def reference(self, name="misleading-fuxi-cn-A14", sdk="36", hyperos="3", device="dada"):
        root = self.rom(name)
        (root / "system/system/build.prop").write_text(
            f"ro.build.version.sdk={sdk}\nro.build.version.release=16\nro.mi.os.version.code={hyperos}\nro.product.odm.device={device}\n")
        path = root / "system_ext/priv-app/MiuiSystemUI/MiuiSystemUI.apk"
        archive(path)
        return root

    def test_reference_identity_only_default_and_explicit_name_does_not_grant_access(self):
        ref = self.reference()
        with patch.object(c, "source_files", wraps=c.source_files) as walk:
            c.update(self.db, self.root)
        self.assertNotIn(ref, [a.args[0] for a in walk.call_args_list])
        self.assertEqual(self.db.execute("SELECT scope FROM roms WHERE id=?", (ref.name,)).fetchone()[0], "REFERENCE_ONLY")
        self.assertEqual(len(self.rows()), 1)
        with self.assertRaisesRegex(c.CatalogError, "REFERENCE_ONLY"):
            c.select_files(self.db, ref.name, "systemui")
        with self.assertRaises(c.CatalogError):
            c.select_files(self.db, None, "systemui", reference_purpose="animation")

    def test_reference_rows_cannot_be_prepared_or_queried_without_purpose_even_if_duplicate(self):
        ref = self.reference()
        ref_file = ref / "system_ext/priv-app/MiuiSystemUI/MiuiSystemUI.apk"
        ref_file.write_bytes(self.file.read_bytes())
        c.update(self.db, self.root, reference_rom=ref.name, reference_purpose="animation")
        rows = c.select_files(self.db, ref.name, "systemui", reference_purpose="animation")
        engine = FakeEngine()
        with self.assertRaises(c.CatalogError):
            c.prepare(self.db, self.index, rows, engine)
        c.prepare(self.db, self.index, rows, engine, reference_purpose="animation")
        with self.assertRaises(c.CatalogError):
            c.search(self.db, rows, engine, "class", "Type")
        result = c.search(self.db, rows, engine, "class", "Type", reference_purpose="animation")
        self.assertEqual(result["results"][0]["scope"], "REFERENCE_ONLY")
        c.prepare(self.db, self.index, self.rows(), engine)
        production = c.search(self.db, self.rows(), engine, "class", "Type")
        self.assertEqual(production["answer_hits"], 0)
        self.assertEqual(len(c.select_files(self.db, component="systemui")), 1)

    def test_changed_identity_rejects_warm_answer_and_unclassified_fails_closed(self):
        c.update(self.db, self.root)
        engine = FakeEngine()
        rows = self.rows()
        c.prepare(self.db, self.index, rows, engine)
        c.search(self.db, rows, engine, "class", "Type")
        prop = self.root / "fuxi-cn/system/system/build.prop"
        prop.write_text("ro.build.version.sdk=36\nro.mi.os.version.code=3\n")
        with self.assertRaisesRegex(c.CatalogError, "identity"):
            c.search(self.db, rows, engine, "class", "Type")
        c.update(self.db, self.root)
        self.assertEqual(self.rows(), [])
        prop.write_text("ro.build.version.sdk=34\n")
        c.update(self.db, self.root)
        with self.assertRaisesRegex(c.CatalogError, "UNCLASSIFIED"):
            c.select_files(self.db, "fuxi-cn")


class MetadataAndTransportTest(unittest.TestCase):
    def test_binary_manifest_utf8_utf16_and_integer_version(self):
        for utf8 in (True, False):
            parsed = c.manifest_attributes(axml(utf8))
            self.assertEqual(parsed, {"package": "com.example.测试", "versionCode": "42"})
        with self.assertRaises(ValueError):
            c.manifest_attributes(axml(True)[:-2])

    def test_mutf8_roundtrip_preserves_delimiters_null_and_supplementary_text(self):
        text = "甲|\n\0😀"
        encoded = text.encode("utf-16le")
        mutf = encoded.decode("utf-16le").replace("😀", "\ud83d\ude00").encode("utf-8", "surrogatepass").replace(b"\0", b"\xc0\x80")
        self.assertEqual(c.parse_rows("| a14_row |\n| V" + mutf.hex() + " |"), [[text]])
        self.assertEqual(c.parse_rows("| a14_row |\n+---+"), [])
        with self.assertRaises(c.CatalogError):
            c.parse_rows("diagnostic only")

    def test_queries_preserve_definition_and_multidex_boundaries(self):
        method = c.search_sql("method", "Lfoo;->it's()V", 21, 20, exact=True)
        self.assertIn("class_methods", method)
        self.assertIn("method_descriptor", method)
        self.assertIn("c.file_id=d.file_id", method)
        self.assertIn("it''s", method)
        self.assertIn("OFFSET 20", method)
        calls = c.search_sql("callers", "callee", 21)
        self.assertIn("instr(c.callee", calls)
        self.assertNotIn("c.offset", calls)

    def test_unknown_schema_is_preserved(self):
        with tempfile.TemporaryDirectory() as temporary:
            index = Path(temporary)
            with contextlib.closing(sqlite3.connect(index / "catalog.sqlite3")) as db:
                db.execute("PRAGMA user_version=99")
            with self.assertRaises(c.CatalogError):
                c.connect(index, create=True)
            with contextlib.closing(sqlite3.connect(index / "catalog.sqlite3")) as db:
                self.assertEqual(db.execute("PRAGMA user_version").fetchone()[0], 99)


if __name__ == "__main__":
    unittest.main()
