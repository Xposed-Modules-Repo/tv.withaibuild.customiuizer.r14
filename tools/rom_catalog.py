#!/usr/bin/env python3
"""Persistent, offline ROM file catalog and bounded official DexSQL queries.

No runtime dependencies, source extraction, daemon, or implicit directory scan.
Only `update` walks ROMs; only `prepare` builds official saved caches.
"""
from __future__ import annotations

import argparse
import contextlib
from functools import lru_cache
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sqlite3
import statistics
import struct
import subprocess
import sys
import sysconfig
import tempfile
import time
import xml.etree.ElementTree as ET
import zipfile

SCHEMA = 3
REPO = Path(__file__).resolve().parents[1]
KINDS = ("class", "method", "string", "callers", "callees")
CORE_JARS = {"framework.jar", "services.jar", "miui-framework.jar", "miui-services.jar"}
MANIFEST_LIMIT = 2 * 1024 * 1024
REFERENCE_PURPOSES = ("animation", "visual", "interaction")


class CatalogError(Exception):
    pass


def dump(value):
    # ASCII escapes also preserve unpaired DEX UTF-16 surrogates on every console.
    return json.dumps(value, ensure_ascii=True, separators=(",", ":"))


def run(argv, timeout=120, input_text=None):
    result = subprocess.run([str(a) for a in argv], input=input_text,
                            capture_output=True, encoding="utf-8", errors="replace",
                            timeout=timeout, check=False)
    if result.returncode:
        raise CatalogError(f"{Path(argv[0]).name}: {(result.stderr or result.stdout)[-1600:]}")
    return result


def sha256(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def stamp(path):
    s = path.stat()
    return (s.st_size, s.st_mtime_ns, s.st_ctime_ns)


@lru_cache(maxsize=1)
def common_repo():
    dotgit = REPO / ".git"
    if dotgit.is_dir():
        return REPO
    if dotgit.is_file():
        text = dotgit.read_text(encoding="utf-8").strip()
        if text.startswith("gitdir: "):
            gitdir = (REPO / text[8:]).resolve()
            common = gitdir / "commondir"
            if common.is_file():
                return (gitdir / common.read_text(encoding="utf-8").strip()).resolve().parent
    try:
        return Path(run(["git", "-C", REPO, "rev-parse", "--path-format=absolute",
                         "--git-common-dir"], timeout=10).stdout.strip()).parent
    except (CatalogError, OSError):
        return REPO


def discover_root(explicit=None):
    if explicit or os.getenv("A14_ROM_ROOT"):
        candidates = [Path(explicit or os.environ["A14_ROM_ROOT"]).expanduser()]
    else:
        candidates = [common_repo().parent / "forA14-ROM-ext"]
        candidates += [p / "forA14-ROM-ext" for p in (Path.cwd(), *Path.cwd().parents)]
        candidates += [Path.home() / "Project/forA14-ROM-ext"]
    found = {p.resolve() for p in candidates if p.is_dir()}
    if len(found) != 1:
        raise CatalogError("ROM root missing or ambiguous; use update --root PATH or A14_ROM_ROOT")
    return found.pop()


def default_index():
    if os.getenv("A14_ROM_INDEX"):
        return Path(os.environ["A14_ROM_INDEX"]).expanduser().resolve()
    return (common_repo().parent / "forA14-ROM-index").resolve()


def outside_sources(index, root):
    if index.is_relative_to(root) or index.is_relative_to(REPO) or index.is_relative_to(common_repo()):
        raise CatalogError("Index must be outside ROM sources and Git worktrees")
    if root.is_relative_to(index):
        raise CatalogError("Index directory must not contain ROM sources")


def connect(index, create=False):
    path = index / "catalog.sqlite3"
    if not create and not path.is_file():
        raise CatalogError("Catalog missing; run update once (queries never scan ROMs)")
    if create:
        index.mkdir(parents=True, exist_ok=True)
    db = sqlite3.connect(path, timeout=10)
    db.row_factory = sqlite3.Row
    db.execute("PRAGMA foreign_keys=ON")
    version = db.execute("PRAGMA user_version").fetchone()[0]
    if version not in (0, 1, 2, SCHEMA):
        db.close()
        raise CatalogError(f"Unsupported catalog schema {version}; preserve it and choose a new index directory")
    if version == 0:
        if not create:
            db.close()
            raise CatalogError("Uninitialized catalog; run update")
        db.executescript("""
            BEGIN IMMEDIATE;
            CREATE TABLE meta(key TEXT PRIMARY KEY, value TEXT NOT NULL);
            CREATE TABLE roms(
              id TEXT PRIMARY KEY, path TEXT NOT NULL, version TEXT, fingerprint TEXT,
              device TEXT, region TEXT, sdk TEXT, partitions TEXT NOT NULL, updated REAL NOT NULL);
            CREATE TABLE artifacts(
              sha256 TEXT PRIMARY KEY, kind TEXT NOT NULL, package TEXT, version TEXT,
              has_dex INTEGER, dex_entries TEXT NOT NULL, error TEXT);
            CREATE TABLE files(
              id INTEGER PRIMARY KEY, rom TEXT NOT NULL REFERENCES roms(id),
              path TEXT NOT NULL, partition TEXT NOT NULL, component TEXT NOT NULL,
              priority INTEGER NOT NULL, sha256 TEXT NOT NULL REFERENCES artifacts(sha256),
              size INTEGER NOT NULL, mtime INTEGER NOT NULL, ctime INTEGER NOT NULL,
              UNIQUE(rom,path));
            CREATE INDEX files_sha ON files(sha256);
            CREATE INDEX files_component ON files(component,rom);
            CREATE TABLE caches(
              sha256 TEXT NOT NULL REFERENCES artifacts(sha256), engine TEXT NOT NULL,
              path TEXT NOT NULL, source TEXT NOT NULL, size INTEGER NOT NULL,
              mtime INTEGER NOT NULL, ctime INTEGER NOT NULL,
              built REAL NOT NULL, build_ms REAL NOT NULL,
              PRIMARY KEY(sha256,engine));
            CREATE TABLE answers(key TEXT PRIMARY KEY, data TEXT NOT NULL, touched REAL NOT NULL);
            PRAGMA user_version=2;
            COMMIT;
        """)
    if version == 1:
        with db:
            db.execute("BEGIN IMMEDIATE")
            db.execute("CREATE TABLE answers(key TEXT PRIMARY KEY, data TEXT NOT NULL, touched REAL NOT NULL)")
            db.execute("PRAGMA user_version=2")
    if version < 3:
        with db:
            db.execute("BEGIN IMMEDIATE")
            db.execute("ALTER TABLE roms ADD COLUMN scope TEXT NOT NULL DEFAULT 'UNCLASSIFIED'")
            db.execute("ALTER TABLE roms ADD COLUMN hyperos TEXT")
            db.execute("ALTER TABLE roms ADD COLUMN identity_files TEXT NOT NULL DEFAULT '[]'")
            db.execute("PRAGMA user_version=3")
    return db


def manifest_attributes(data):
    """Read just the manifest root attributes; never parse DEX or expand sources."""
    if data.lstrip().startswith(b"<"):
        return {key.rsplit("}", 1)[-1]: value for key, value in ET.fromstring(data).attrib.items()}
    def u16(pos):
        return struct.unpack_from("<H", data, pos)[0]
    def u32(pos):
        return struct.unpack_from("<I", data, pos)[0]
    if len(data) < 8 or u16(0) != 3 or u32(4) != len(data):
        raise ValueError("Invalid binary Android Manifest")
    strings = []
    pos = u16(2)
    while pos + 8 <= len(data):
        tag, header, size = u16(pos), u16(pos + 2), u32(pos + 4)
        end = pos + size
        if header < 8 or size < header or end > len(data):
            raise ValueError("Invalid AXML chunk")
        if tag == 1:
            count, flags, start = u32(pos + 8), u32(pos + 16), u32(pos + 20)
            if header < 28 or pos + header + count * 4 > end:
                raise ValueError("Invalid AXML string pool")
            def length(at, utf8):
                n = data[at] if utf8 else u16(at)
                step, mask = (1, 128) if utf8 else (2, 32768)
                if n & mask:
                    n = ((n & (mask - 1)) << (step * 8)) | (data[at + step] if utf8 else u16(at + step))
                    return n, at + 2 * step
                return n, at + step
            for i in range(count):
                at = pos + start + u32(pos + header + i * 4)
                utf8 = bool(flags & 0x100)
                n, at = length(at, utf8)
                if utf8:
                    n, at = length(at, True)
                stop = at + n * (1 if utf8 else 2)
                if at < pos or stop > end:
                    raise ValueError("AXML string outside pool")
                strings.append(data[at:stop].decode("utf-8" if utf8 else "utf-16le", errors="replace"))
        elif tag == 0x102:
            ext = pos + header
            if strings[u32(ext + 4)] == "manifest":
                result = {}
                attr_start, attr_size, count = u16(ext + 8), u16(ext + 10), u16(ext + 12)
                if attr_size < 20 or ext + attr_start + attr_size * count > end:
                    raise ValueError("Invalid Manifest attributes")
                for i in range(count):
                    at = ext + attr_start + i * attr_size
                    name, raw, value = u32(at + 4), u32(at + 8), u32(at + 16)
                    value_type = data[at + 15]
                    if raw != 0xffffffff:
                        result[strings[name]] = strings[raw]
                    elif value_type == 3:
                        result[strings[name]] = strings[value]
                    elif value_type in (16, 17, 18):
                        result[strings[name]] = str(value)
                return result
        pos = end
    raise ValueError("Manifest root not found")


def inspect_artifact(path):
    result = dict(kind=path.suffix[1:].upper(), package=None, version=None,
                  has_dex=None, dex_entries=[], error=None)
    try:
        if path.suffix.lower() == ".dex":
            with path.open("rb") as f:
                magic = f.read(8)
            if not magic.startswith(b"dex\n") or magic[-1:] != b"\0":
                raise ValueError("Invalid DEX magic")
            result.update(has_dex=1, dex_entries=[dict(name=path.name, size=path.stat().st_size)])
        else:
            with zipfile.ZipFile(path) as archive:
                result["dex_entries"] = [dict(name=i.filename, size=i.file_size, crc=i.CRC)
                                         for i in archive.infolist() if i.filename.endswith(".dex")]
                result["has_dex"] = int(bool(result["dex_entries"]))
                if "AndroidManifest.xml" in archive.namelist():
                    if archive.getinfo("AndroidManifest.xml").file_size > MANIFEST_LIMIT:
                        raise ValueError("Manifest exceeds metadata budget")
                    attrs = manifest_attributes(archive.read("AndroidManifest.xml"))
                    result.update(package=attrs.get("package"), version=attrs.get("versionName") or attrs.get("versionCode"))
    except (OSError, ValueError, IndexError, KeyError, struct.error, zipfile.BadZipFile, ET.ParseError) as exc:
        result["error"] = str(exc)
    return result


def classify(path, package):
    name = path.name.lower()
    if package == "com.android.systemui" or name in ("miuisystemui.apk", "systemui.apk"):
        return "systemui", 1
    if package == "com.miui.home" or name == "miuihome.apk":
        return "miuihome", 1
    if package == "com.mi.android.globallauncher" or name in ("pocolauncher.apk", "milauncherglobal.apk"):
        return "poco", 1
    if package == "miui.systemui.plugin" or name == "miuisystemuiplugin.apk":
        return "plugin", 1
    if "framework" in path.parts and path.suffix.lower() == ".jar":
        return "framework", int(name in CORE_JARS)
    return "other", 0


def rom_properties(rom):
    props, origins, identity_files = {}, {}, []
    for name in ("system/system/build.prop", "system/build.prop", "product/etc/build.prop",
                 "vendor/build.prop", "odm/etc/build.prop", "mi_ext/etc/build.prop"):
        file = rom / name
        if file.is_file() and not file.is_symlink():
            identity_files.append([name, *stamp(file)])
            for line in file.read_text(encoding="utf-8", errors="replace").splitlines():
                if "=" in line and not line.lstrip().startswith("#"):
                    key, value = line.split("=", 1)
                    key = key.strip()
                    if key not in props:
                        props[key], origins[key] = value.strip(), name
    fingerprint_key = next((k for k in ("ro.build.fingerprint", "ro.odm.build.fingerprint", "ro.vendor.build.fingerprint") if k in props), None)
    fingerprint = props.get(fingerprint_key)
    version = props.get("ro.build.version.incremental")
    region = next((r for suffix, r in (("CNXM", "CN"), ("TWXM", "TW"), ("IDXM", "ID"),
                                      ("MIXM", "Global"), ("EUXM", "EEA")) if (version or "").endswith(suffix)), None)
    device = props.get("ro.product.odm.device") or props.get("ro.product.vendor.device")
    return (version, fingerprint, device, region, props.get("ro.build.version.sdk")), dict(
        fingerprint_key=fingerprint_key, fingerprint_file=origins.get(fingerprint_key),
        directory_label=rom.name, mod_device=props.get("ro.product.mod_device"),
        hyperos=props.get("ro.mi.os.version.code"), release=props.get("ro.build.version.release"),
        identity_files=identity_files)


def rom_scope(identity, evidence):
    # Metadata only: directory labels, classes or package names never grant production scope.
    if identity[4] == "34" and evidence["hyperos"] == "1" and evidence["release"] == "14":
        return "A14_PRODUCTION"
    return "REFERENCE_ONLY" if identity[4] and evidence["hyperos"] else "UNCLASSIFIED"


def allow_reference(rom, purpose):
    if not rom or purpose not in REFERENCE_PURPOSES:
        raise CatalogError("REFERENCE_ONLY requires an explicit --rom and --reference-purpose animation / visual / interaction")


def authorize(row, purpose=None):
    scope = row.get("scope", "UNCLASSIFIED")
    if scope != "A14_PRODUCTION":
        if scope != "REFERENCE_ONLY" or purpose not in REFERENCE_PURPOSES:
            raise CatalogError(f"{scope}: excluded from A14 production; explicit design-reference scope is required")


def source_files(rom):
    def onerror(error):
        raise error  # An incomplete walk must never delete missing catalog entries.
    for base, dirs, files in os.walk(rom, followlinks=False, onerror=onerror):
        dirs[:] = sorted(d for d in dirs if not linked(Path(base) / d))
        for name in sorted(files):
            path = Path(base) / name
            if path.suffix.lower() in (".apk", ".jar", ".dex") and not path.is_symlink():
                yield path


def linked(path):
    return path.is_symlink() or (hasattr(path, "is_junction") and path.is_junction())


def update(db, root, rehash=False, reference_rom=None, reference_purpose=None):
    if reference_rom or reference_purpose:
        allow_reference(reference_rom, reference_purpose)
    stats = dict(roms=0, files=0, hashed=0, reused=0, metadata_reads=0, errors=0, removed=0)
    roms = sorted(p for p in root.iterdir() if p.is_dir() and not linked(p))
    roms = [p for p in roms if (p / "system").is_dir() and (p / "system/system/build.prop").is_file()
            or (p / "system/build.prop").is_file()]
    if not roms:
        raise CatalogError("No extracted ROMs found; catalog kept unchanged")
    with db:
        db.execute("BEGIN IMMEDIATE")
        seen_roms = set()
        for rom in roms:
            rid = rom.name
            seen_roms.add(rid)
            old = db.execute("SELECT path FROM roms WHERE id=?", (rid,)).fetchone()
            moved = bool(old and old[0] != str(rom))
            partitions = sorted(p.name for p in rom.iterdir() if p.is_dir() and p.name != "config")
            identity, evidence = rom_properties(rom)
            scope = rom_scope(identity, evidence)
            db.execute("INSERT OR REPLACE INTO roms VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                       (rid, str(rom), *identity, dump(partitions), time.time(), scope,
                        evidence["hyperos"], dump(evidence["identity_files"])))
            db.execute("INSERT OR REPLACE INTO meta VALUES(?,?)", ("identity:" + rid, dump(evidence)))
            if scope != "A14_PRODUCTION" and not (scope == "REFERENCE_ONLY" and reference_rom and reference_rom.casefold() in rid.casefold()):
                continue  # Identity-only discovery never inventories or prepares reference content.
            seen = set()
            for path in source_files(rom):
                relative = path.relative_to(rom).as_posix()
                before = stamp(path)
                row = db.execute("SELECT * FROM files WHERE rom=? AND path=?", (rid, relative)).fetchone()
                if row and not rehash and not moved and before == (row["size"], row["mtime"], row["ctime"]):
                    stats["reused"] += 1
                else:
                    digest = sha256(path)
                    stats["hashed"] += 1
                    artifact = db.execute("SELECT * FROM artifacts WHERE sha256=?", (digest,)).fetchone()
                    if not artifact:
                        info = inspect_artifact(path)
                        db.execute("INSERT INTO artifacts VALUES(?,?,?,?,?,?,?)", (digest, info["kind"],
                                   info["package"], info["version"], info["has_dex"], dump(info["dex_entries"]), info["error"]))
                        stats["metadata_reads"] += 1
                        artifact = db.execute("SELECT * FROM artifacts WHERE sha256=?", (digest,)).fetchone()
                    if stamp(path) != before:
                        raise CatalogError(f"Source changed during update: {path}; retry update")
                    component, priority = classify(Path(relative), artifact["package"])
                    db.execute("""INSERT INTO files(rom,path,partition,component,priority,sha256,size,mtime,ctime)
                                  VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT(rom,path) DO UPDATE SET
                                  partition=excluded.partition,component=excluded.component,priority=excluded.priority,
                                  sha256=excluded.sha256,size=excluded.size,mtime=excluded.mtime,ctime=excluded.ctime""",
                               (rid, relative, relative.split("/")[0], component, priority, digest, *before))
                seen.add(relative)
                stats["files"] += 1
            for row in db.execute("SELECT id,path FROM files WHERE rom=?", (rid,)).fetchall():
                if row["path"] not in seen:
                    db.execute("DELETE FROM files WHERE id=?", (row["id"],))
                    stats["removed"] += 1
            stats["roms"] += 1
        for row in db.execute("SELECT id FROM roms").fetchall():
            if row[0] not in seen_roms:
                stats["removed"] += db.execute("DELETE FROM files WHERE rom=?", (row[0],)).rowcount
                db.execute("DELETE FROM roms WHERE id=?", (row[0],))
        db.execute("INSERT OR REPLACE INTO meta VALUES('root',?)", (str(root),))
        stats["errors"] = db.execute("SELECT count(*) FROM artifacts WHERE error IS NOT NULL AND sha256 IN (SELECT sha256 FROM files)").fetchone()[0]
    stats["unique_artifacts"] = db.execute("SELECT count(DISTINCT sha256) FROM files").fetchone()[0]
    return stats


def select_files(db, rom=None, component=None, priority=False, reference_purpose=None):
    if reference_purpose:
        allow_reference(rom, reference_purpose)
    target_scope = "REFERENCE_ONLY" if reference_purpose else "A14_PRODUCTION"
    if rom and not reference_purpose:
        hidden = db.execute("SELECT id,scope FROM roms WHERE instr(lower(id),lower(?))>0 AND scope != 'A14_PRODUCTION'", (rom,)).fetchall()
        if hidden:
            raise CatalogError(f"{hidden[0]['id']}: {hidden[0]['scope']} excluded from production; explicit design-reference access required")
    clauses, args = ["r.scope=?"], [target_scope]
    if rom:
        clauses.append("instr(lower(f.rom),lower(?))>0")
        args.append(rom)
    if component == "launcher":
        clauses.append("f.component IN ('miuihome','poco')")
    elif component in ("systemui", "miuihome", "poco", "plugin", "framework", "other"):
        clauses.append("f.component=?")
        args.append(component)
    elif component:
        clauses.append("(f.component=? OR instr(lower(f.path),lower(?))>0 OR instr(lower(coalesce(a.package,'')),lower(?))>0)")
        args += [component, component, component]
    if priority:
        clauses.append("f.priority=1")
    return [dict(r) for r in db.execute("""SELECT f.*, r.path AS rom_path,r.scope,r.identity_files, a.package,a.version,
             a.has_dex,a.dex_entries,a.error FROM files f JOIN roms r ON r.id=f.rom
             JOIN artifacts a ON a.sha256=f.sha256 WHERE """ + " AND ".join(clauses) + " ORDER BY f.rom,f.path", args)]


def source(row):
    for name, *expected in json.loads(row.get("identity_files", "[]")):
        if list(stamp(Path(row["rom_path"]) / name)) != expected:
            raise CatalogError("ROM identity metadata changed; run update before using saved scope")
    path = Path(row["rom_path"]) / row["path"]
    if not path.resolve().is_relative_to(Path(row["rom_path"]).resolve()):
        raise CatalogError("Source path escapes ROM")
    if stamp(path) != (row["size"], row["mtime"], row["ctime"]):
        raise CatalogError(f"Catalog stale: {row['rom']}:{row['path']}; run update")
    return path


def find_dexsql(explicit=None):
    candidates = [explicit or os.getenv("DEXSQL_EXE"), shutil.which("dexsql")]
    config = Path(os.getenv("CODEX_HOME", Path.home() / ".codex")) / "config.toml"
    if config.is_file():
        import tomllib
        entry = tomllib.loads(config.read_text(encoding="utf-8-sig")).get("mcp_servers", {}).get("dexsql", {})
        candidates.append(entry.get("command"))
    for base in [Path(os.getenv("LOCALAPPDATA", Path.home())) / "DexSQL", Path.home() / ".local/share/dexsql"]:
        if base.is_dir():
            versions = sorted(base.iterdir(), key=lambda p: tuple(int(n) for n in re.findall(r"\d+", p.name)), reverse=True)
            executable = "dexsql" + (sysconfig.get_config_var("EXE") or "")
            candidates += [str(p / executable) for p in versions]
    for candidate in candidates:
        if candidate and Path(candidate).is_file():
            return Path(candidate).resolve()
    raise CatalogError("DexSQL not found; set DEXSQL_EXE to the existing official executable")


class Engine:
    def __init__(self, explicit=None, db=None):
        self.exe = find_dexsql(explicit)
        signature = [str(self.exe), *stamp(self.exe)]
        saved = db.execute("SELECT value FROM meta WHERE key=?", ("engine:" + str(self.exe),)).fetchone() if db else None
        identity = json.loads(saved[0]) if saved else None
        if identity and identity["signature"] == signature:
            self.version, self.key = identity["version"], identity["sha256"]
        else:
            self.version = run([self.exe, "--version"], timeout=10).stdout.strip()
            self.key = sha256(self.exe)  # Version alone cannot identify locally replaced binaries.
            if [str(self.exe), *stamp(self.exe)] != signature:
                raise CatalogError("DexSQL changed while identifying it; retry with the stable executable")
            if db:
                with db:
                    db.execute("INSERT OR REPLACE INTO meta VALUES(?,?)", ("engine:" + str(self.exe), dump(
                        dict(signature=signature, version=self.version, sha256=self.key))))
        self.calls = 0

    def execute(self, argv):
        self.calls += 1
        return run([self.exe, "--threads", "4", *argv], timeout=180)

    def query(self, path, sql, cached=True):
        return self.execute(["query-cache", "--cache-file", path, "--sql=" + sql] if cached else
                            ["query", "--input", path, "--sql=" + sql]).stdout


def cache_for(db, row, engine):
    result = db.execute("SELECT * FROM caches WHERE sha256=? AND engine=?", (row["sha256"], engine.key)).fetchone()
    if not result:
        raise CatalogError(f"Cache missing for {row['rom']}:{row['path']}; run prepare with the same selectors")
    path = Path(result["path"])
    if not path.is_file() or stamp(path) != (result["size"], result["mtime"], result["ctime"]):
        raise CatalogError("Cache missing or modified; run prepare --rebuild")
    # Official query-cache additionally revalidates the recorded source, including duplicates.
    if not Path(result["source"]).is_file():
        raise CatalogError("Cache source moved/deleted; run prepare to bind an available identical source")
    source_scopes = [r["scope"] for r in db.execute("SELECT path,scope FROM roms")
                     if Path(result["source"]).is_relative_to(Path(r["path"]))]
    if not source_scopes or (row["scope"] == "A14_PRODUCTION" and "A14_PRODUCTION" not in source_scopes):
        raise CatalogError("Cache provenance is outside the selected scope; prepare to bind a source within scope")
    return path


def prepare(db, index, rows, engine, rebuild=False, reference_purpose=None):
    stats = dict(built=0, reused=0, no_dex=0, selected=len(rows), build_ms=0)
    done = set()
    with db:
        db.execute("BEGIN IMMEDIATE")
        for row in rows:
            authorize(row, reference_purpose)
            path = source(row)
            if not row["has_dex"]:
                stats["no_dex"] += 1
                continue
            digest = row["sha256"]
            if digest in done:
                continue
            done.add(digest)
            if not rebuild:
                try:
                    cached = cache_for(db, row, engine)
                    # Validate with the engine as well as the catalog: never use trusted-input.
                    engine.query(cached, "SELECT count(*) FROM dex_files")
                    stats["reused"] += 1
                    continue
                except CatalogError:
                    pass
            if sha256(path) != digest:
                raise CatalogError(f"Hash differs from catalog: {path}; run update")
            directory = index / "dexsql" / engine.key / digest
            if not directory.resolve().is_relative_to(index.resolve()):
                raise CatalogError("Cache directory escapes the index directory")
            directory.mkdir(parents=True, exist_ok=True)
            started = time.perf_counter()
            with tempfile.TemporaryDirectory(prefix="build-", dir=directory) as temporary:
                engine.execute(["prepare-cache", "--input", path, "--output", temporary])
                if sha256(path) != digest:
                    raise CatalogError(f"Source changed while preparing: {path}")
                container = Path(temporary) / "cache.dxc2"
                engine.query(container, "SELECT count(*) FROM dex_files")
                destination = directory / "cache.dxc2"
                os.replace(container, destination)
            elapsed = (time.perf_counter() - started) * 1000
            db.execute("INSERT OR REPLACE INTO caches VALUES(?,?,?,?,?,?,?,?,?)", (digest, engine.key,
                       str(destination), str(path), *stamp(destination), time.time(), elapsed))
            stats["built"] += 1
            stats["build_ms"] += elapsed
    return stats


def literal(text):
    if "\0" in text:
        raise CatalogError("NUL in query is unsupported")
    return "'" + text.replace("'", "''") + "'"


def search_sql(kind, needle, limit, offset=0, exact=False, transport=True):
    # Hex is an unambiguous transport over the official CLI's text table. It
    # preserves pipes/newlines/NUL and avoids parsing arbitrary vendor strings.
    def where(column):
        return f"{column}={literal(needle)}" if exact else f"instr({column},{literal(needle)})>0"
    if kind == "class":
        columns, table, condition = ["d.entry_name", "c.descriptor"], "classes c", where("c.descriptor")
    elif kind == "method":
        columns, table, condition = ["d.entry_name", "c.method_descriptor"], "class_methods c", where("c.method_descriptor")
    elif kind == "string":
        columns, table, condition = ["d.entry_name", "c.method", "substr(c.value,1,240)", "length(c.value)"], "method_strings c", where("c.value")
    else:
        columns, table = ["d.entry_name", "c.method", "c.callee"], "method_calls c"
        condition = where("c.callee" if kind == "callers" else "c.method")
    names = [f"v{i}" for i in range(len(columns))]
    projection = "||':'||".join(f"'V'||hex(CAST({c} AS TEXT))" for c in names) + " AS a14_row" if transport else ",".join(names)
    unique = ",".join(f"{c} AS {name}" for c, name in zip(columns, names))
    return (f"SELECT {projection} FROM (SELECT DISTINCT {unique} FROM {table} "
            f"JOIN dex_files d ON c.file_id=d.file_id WHERE {condition}) "
            f"ORDER BY {','.join(names)} LIMIT {limit} OFFSET {offset}")


def decode_hex(value):
    text = bytes.fromhex(value).replace(b"\xc0\x80", b"\0").decode("utf-8", "surrogatepass")
    return text.encode("utf-16le", "surrogatepass").decode("utf-16le", "surrogatepass")


def parse_rows(output):
    rows, header = [], False
    for line in output.splitlines():
        if line.startswith("|"):
            payload = line.strip().strip("|").strip()
            if payload == "a14_row":
                header = True
                continue
            if not re.fullmatch(r"V[0-9A-Fa-f]*(?::V[0-9A-Fa-f]*)*", payload):
                raise CatalogError("Unexpected DexSQL CLI output; inspect installed schema/version")
            rows.append([decode_hex(cell[1:]) for cell in payload.split(":")])
    if not header:
        raise CatalogError("DexSQL result header missing; do not interpret this as an empty result")
    return rows


def search(db, rows, engine, kind, needle, limit=20, offset=0, exact=False, fresh=False, reference_purpose=None):
    sql = search_sql(kind, needle, limit + 1, offset, exact)
    groups, no_dex, queried, calls, hits = {}, [], {}, 0, 0
    for row in rows:
        authorize(row, reference_purpose)
        source(row)
        origin = dict(rom=row["rom"], path=row["path"], sha256=row["sha256"], scope=row["scope"])
        if not row["has_dex"]:
            no_dex.append({**origin, "status": "metadata_error" if row["error"] else "no_dex"})
            continue
        if row["sha256"] not in queried:
            key = hashlib.sha256(dump([row["scope"], reference_purpose, row["sha256"], engine.key, sql]).encode()).hexdigest()
            answer = None if fresh else db.execute("SELECT data FROM answers WHERE key=?", (key,)).fetchone()
            if answer:
                queried[row["sha256"]] = json.loads(answer[0])
                hits += 1
            else:
                output = engine.query(cache_for(db, row, engine), sql)
                queried[row["sha256"]] = parse_rows(output)
                source(row)  # Refuse concurrent input changes before publishing results.
                calls += 1
                data = dump(queried[row["sha256"]])
                if len(data.encode()) <= 65536:
                    with db:
                        db.execute("INSERT OR REPLACE INTO answers VALUES(?,?,?)", (key, data, time.time()))
                        db.execute("DELETE FROM answers WHERE key NOT IN (SELECT key FROM answers ORDER BY touched DESC,key LIMIT 128)")
        matches = queried[row["sha256"]]
        group = groups.setdefault((row["scope"], row["sha256"]), dict(sha256=row["sha256"], scope=row["scope"], evidence="DEX_STATIC", sources=[],
            rows=matches[:limit], next_offset=offset + limit if len(matches) > limit else None))
        group["sources"].append({k: origin[k] for k in ("rom", "path")})
        source(row)
    columns = ["dex", "descriptor"] if kind in ("class", "method") else (
        ["dex", "method", "value_prefix", "value_length"] if kind == "string" else ["dex", "caller", "callee"])
    result = dict(kind=kind, columns=columns, results=list(groups.values()), dexsql_calls=calls, answer_hits=hits)
    if no_dex:
        result["skipped"] = no_dex
    return result


def benchmark(db, rows, engine, kind, needle, repeats, exact=False):
    if len(rows) != 1:
        raise CatalogError("Benchmark must select exactly one file using --rom and --component")
    row = rows[0]
    authorize(row)
    path, cache = source(row), cache_for(db, row, engine)
    sql = search_sql(kind, needle, 21, exact=exact)
    timings = {"raw": [], "cache": []}
    outputs = {}
    for i in range(repeats):
        # Alternate order. OS caches are deliberately not flushed.
        for mode in (("raw", "cache") if i % 2 == 0 else ("cache", "raw")):
            start = time.perf_counter()
            output = engine.query(cache if mode == "cache" else path, sql, mode == "cache")
            timings[mode].append((time.perf_counter() - start) * 1000)
            decoded = parse_rows(output)
            if outputs and decoded != next(iter(outputs.values())):
                raise CatalogError("Raw/cache benchmark result mismatch")
            outputs[mode] = decoded
    raw_ms, cache_ms = (statistics.median(timings[k]) for k in ("raw", "cache"))
    search(db, rows, engine, kind, needle, exact=exact)
    memo_ms = []
    for _ in range(repeats):
        started = time.perf_counter()
        compact = dump(search(db, rows, engine, kind, needle, exact=exact))
        memo_ms.append((time.perf_counter() - started) * 1000)
    # This compares the same results' CLI/JSON representations, not model billing.
    raw_text = engine.query(path, search_sql(kind, needle, 20, exact=exact, transport=False), cached=False)
    build_ms = db.execute("SELECT build_ms FROM caches WHERE sha256=? AND engine=?", (row["sha256"], engine.key)).fetchone()[0]
    return dict(kind=kind, needle=needle, sha256=row["sha256"], engine=engine.version,
                timings_ms=timings, median_ms=dict(raw=raw_ms, cache=cache_ms),
                speedup=raw_ms / cache_ms, answer_lookup_ms=memo_ms, build_ms=build_ms,
                amortize_queries=round(build_ms / (raw_ms-cache_ms), 1) if raw_ms > cache_ms else None,
                result_rows=len(outputs["cache"]), equal=True, calls_per_query=dict(raw=1, cache=1, answer=0),
                output_bytes=dict(cli=len(raw_text.encode()), compact_json=len(compact.encode())),
                token_proxy="UTF-8 output bytes only; actual model tokens/billing not measured",
                note="Raw/cache include DexSQL process launch, alternating order, warm OS cache; answer lookup excludes Python/engine startup; raw uses lazy query, not --full")


def doctor(engine):
    help_text = run([engine.exe, "--help"], timeout=10).stdout
    requests = [dict(jsonrpc="2.0", id=1, method="initialize", params=dict(
        protocolVersion="2024-11-05", capabilities={}, clientInfo=dict(name="a14-rom-catalog", version="1"))),
        dict(jsonrpc="2.0", method="notifications/initialized"),
        dict(jsonrpc="2.0", id=2, method="tools/list", params={})]
    protocol = run([engine.exe, "mcp"], timeout=20, input_text="".join(dump(r) + "\n" for r in requests))
    messages = [json.loads(line) for line in protocol.stdout.splitlines() if line.strip()]
    listing = next((r.get("result", {}).get("tools", []) for r in messages if r.get("id") == 2), [])
    names = [t["name"] for t in listing]
    if not {"open_dataset", "query", "decode"}.issubset(names):
        raise CatalogError("MCP probe did not return the required tools")
    return dict(executable=str(engine.exe), version=engine.version, sha256=engine.key,
                prepare_cache="prepare-cache" in help_text, query_cache="query-cache" in help_text,
                mcp_protocol="verified via short-lived stdio probe", tools=names,
                client_connection="not inferred from protocol verification; use loaded MCP tools when available")


def parser():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--index-dir", type=Path, default=default_index())
    p.add_argument("--dexsql", help="Existing official executable (or DEXSQL_EXE / Codex config / local install)")
    sub = p.add_subparsers(dest="command", required=True)
    sub.add_parser("doctor", help="Check official CLI capabilities and MCP protocol; no ROM scan")
    sub.add_parser("status", help="Read saved coverage and ROM identity; no scan or DEX parse")
    u = sub.add_parser("update", help="Explicit incremental file/manifest inventory; no DEX parse")
    u.add_argument("--root", type=Path)
    u.add_argument("--rehash", action="store_true", help="Rehash even unchanged file stats")
    u.add_argument("--reference-rom", help="Also inventory this explicitly selected reference ROM")
    u.add_argument("--reference-purpose", choices=REFERENCE_PURPOSES)
    for name in ("locate", "prepare", "search", "compare", "benchmark"):
        s = sub.add_parser(name)
        s.add_argument("--rom", help="Case-insensitive substring of the discovered ROM directory name")
        if name != "benchmark":
            s.add_argument("--reference-purpose", choices=REFERENCE_PURPOSES, help="Explicit reference-only research; requires --rom")
        if name == "compare":
            against = s.add_mutually_exclusive_group()
            against.add_argument("--reference-rom", help="Explicit cross-version comparison; requires --reference-purpose, keeps scopes separate")
            against.add_argument("--against-rom", help="Compare only one additional A14 production ROM")
        s.add_argument("--component", required=name in ("search", "compare", "benchmark"),
                       help="Role: systemui / launcher / miuihome / poco / plugin / framework; or path/package substring")
        if name == "prepare":
            s.add_argument("--priority", action="store_true", help="Only core hosts, plugins and four core framework JARs")
            s.add_argument("--rebuild", action="store_true")
        elif name == "locate":
            s.add_argument("--limit", type=int, default=20)
            s.add_argument("--offset", type=int, default=0)
        else:
            s.add_argument("kind", choices=KINDS)
            s.add_argument("needle")
            s.add_argument("--exact", action="store_true")
            if name == "benchmark":
                s.add_argument("--repeats", type=int, default=3)
            else:
                s.add_argument("--limit", type=int, default=20, help="Rows per source, maximum 100")
                s.add_argument("--offset", type=int, default=0)
                s.add_argument("--fresh", action="store_true", help="Bypass bounded answer reuse and revalidate through official query-cache")
    return p


def main(argv=None):
    args = parser().parse_args(argv)
    started = time.perf_counter()
    try:
        if not 1 <= getattr(args, "limit", 20) <= 100 or not 0 <= getattr(args, "offset", 0) <= 100000:
            raise CatalogError("limit must be 1..100 and offset 0..100000")
        if not 1 <= getattr(args, "repeats", 3) <= 9:
            raise CatalogError("repeats must be 1..9")
        index = args.index_dir.expanduser().resolve()
        if args.command == "doctor":
            result = doctor(Engine(args.dexsql))
        else:
            root = discover_root(args.root) if args.command == "update" else None
            if root:
                outside_sources(index, root)
            with contextlib.closing(connect(index, create=bool(root))) as db:
                if not root:
                    saved = db.execute("SELECT value FROM meta WHERE key='root'").fetchone()
                    if not saved:
                        raise CatalogError("Catalog has no source root; run update")
                    outside_sources(index, Path(saved[0]))
                if args.command == "update":
                    result = update(db, root, args.rehash, args.reference_rom, args.reference_purpose)
                elif args.command == "status":
                    identities = [dict(r) for r in db.execute(
                        "SELECT id,version,fingerprint,device,region,sdk,hyperos,scope,partitions,updated FROM roms ORDER BY id")]
                    for identity in identities:
                        identity["partitions"] = json.loads(identity["partitions"])
                        evidence = db.execute("SELECT value FROM meta WHERE key=?", ("identity:" + identity["id"],)).fetchone()
                        identity["identity_source"] = json.loads(evidence[0]) if evidence else None
                    result = dict(index_dir=str(index), roms=identities,
                        files=db.execute("SELECT count(*) FROM files").fetchone()[0],
                        unique_artifacts=db.execute("SELECT count(DISTINCT sha256) FROM files").fetchone()[0],
                        caches=db.execute("SELECT count(*) FROM caches").fetchone()[0],
                        answers=db.execute("SELECT count(*) FROM answers").fetchone()[0],
                        metadata_errors=db.execute("SELECT count(*) FROM files JOIN artifacts USING(sha256) WHERE error IS NOT NULL").fetchone()[0],
                        cache_bytes=db.execute("SELECT coalesce(sum(size),0) FROM caches").fetchone()[0],
                        coverage=[dict(r) for r in db.execute("SELECT component,count(*) AS files,sum(has_dex) AS with_dex FROM files JOIN artifacts USING(sha256) GROUP BY component ORDER BY component")])
                else:
                    purpose = getattr(args, "reference_purpose", None)
                    against = getattr(args, "reference_rom", None)
                    rows = select_files(db, args.rom, args.component, getattr(args, "priority", False), None if against else purpose)
                    if against:
                        allow_reference(against, purpose)
                        rows += select_files(db, against, args.component, reference_purpose=purpose)
                    if getattr(args, "against_rom", None):
                        if purpose or not args.rom:
                            raise CatalogError("--against-rom requires explicit production --rom and no reference purpose")
                        rows += select_files(db, args.against_rom, args.component)
                    if not rows:
                        raise CatalogError("No catalog files match the selectors; use status / locate")
                    if args.command == "locate":
                        selected = rows[args.offset:args.offset + args.limit]
                        result = dict(total=len(rows), next_offset=args.offset + args.limit if len(rows) > args.offset + args.limit else None,
                                      files=[{k: r[k] for k in ("rom", "path", "scope", "component", "package", "version", "sha256", "size", "has_dex", "error")} | {
                                          "dex_count": len(json.loads(r["dex_entries"])),
                                          "absolute_path": str(Path(r["rom_path"]) / r["path"]),
                                          "cache_files": [c[0] for c in db.execute("SELECT path FROM caches WHERE sha256=? ORDER BY built DESC", (r["sha256"],))]} for r in selected])
                    elif args.command == "prepare":
                        if not args.priority and not args.component:
                            raise CatalogError("prepare requires --priority or --component to prevent indiscriminate expansion")
                        result = prepare(db, index, rows, Engine(args.dexsql, db), args.rebuild, purpose)
                    elif args.command == "benchmark":
                        result = benchmark(db, rows, Engine(args.dexsql, db), args.kind, args.needle, args.repeats, args.exact)
                    else:
                        if len(rows) > 64:
                            raise CatalogError("More than 64 source files selected; narrow --rom / --component")
                        result = search(db, rows, Engine(args.dexsql, db), args.kind, args.needle, args.limit, args.offset, args.exact, args.fresh, purpose)
                        if args.command == "compare":
                            # Same descriptor/evidence rows, never compare dataset-local indices.
                            result["comparison"] = [dict(sources=g["sources"],scope=g["scope"],evidence=g["evidence"],
                                sha256=g["sha256"], rows=g["rows"], complete=g["next_offset"] is None,next_offset=g["next_offset"])
                                for g in result.pop("results")]
                            result["scope"] = "ARTIFACT rows only; no reflection/runtime/removal conclusion; follow every incomplete page"
        result["wall_ms"] = round((time.perf_counter() - started) * 1000, 3)
        print(dump(result))
        return 0
    except (CatalogError, OSError, sqlite3.Error, subprocess.TimeoutExpired, ValueError) as exc:
        print(dump(dict(error=str(exc))), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
