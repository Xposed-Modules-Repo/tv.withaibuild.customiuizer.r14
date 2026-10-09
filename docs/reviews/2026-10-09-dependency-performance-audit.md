# r14.22.6 dependency and performance audit

Baseline: r14.22.5 / 228, source `6617afb8757cf113c35959d53250240104a7a89f`.
Scope: HyperOS 1 / Android 14, arm64-v8a, API 101 minimum runtime, API 102 isolation,
JDK 25 build runtime and JVM 17 output. No runtime infrastructure or UI redesign.

## Changes selected

- Wallpaper callbacks copy arguments only when the requested color differs. The
  original method still runs once; return values and fatal exceptions propagate.
- Temperature formatting reads and formats only the selected values.
- Backup decoding checks CRC over the bounded original payload, avoiding a full
  payload copy. Export filters its existing owned map after migration; preference
  snapshots still copy mutable sets. Format, strict decoding and CRC boundaries stay.
- Search reuses a fixed whitespace pattern and avoids an extra token list; locale
  lists allocate their final arrays directly. Matching and locale ordering stay.
- Stable libxposed resolves exclusively from Maven Central in both repository modes.
  Remove unused local/JitPack precedence and verify API, Service and Interface 102
  AAR/POM/module SHA-256 values using Gradle's native dependency verification.
  Verification is deliberately scoped to libxposed; other groups retain their
  existing policy. This is not a claim that every dependency has a pinned checksum.
- Constrain the existing transitive Fragment KTX dependency to 1.5.4, matching the
  already selected Fragment. Do not add another UI framework or migrate fragments.
- Compact the existing optimized binary resource table with the pinned AGP's own
  AAPT2, before signing. R8's proto-to-binary rebuild discarded the link-time flag,
  so the operation runs on its declared output. Preserve all resource names/IDs.
- Exclude `DebugProbesKt.bin`, the unused JVM coroutine-agent replacement payload.
  Android coroutine support and its consumer rules remain.

## Toolchain and dependency decisions

| Component | Selected | Finding / decision |
| --- | --- | --- |
| Gradle | 9.6.1 | Wrapper/distribution checksums match upstream; retain. |
| AGP / D8 / R8 | 9.3.3 / bundled | Retain the existing patched toolchain and full R8/resource shrinking. |
| Kotlin compiler / KGP | 2.4.20 | Actual compiler resolved separately from APK runtime. Language/API stay 2.2, JVM target 17. |
| Kotlin runtime | BOM + stdlib 2.3.21 | One APK stdlib version; compiler/runtime differences are separate configurations. Retain. |
| JDK | 25.0.4.1 locally; CI Temurin 25 | Host/toolchain remain 25. Java/Kotlin class output remains 17. |
| Android SDK / Build Tools | compile 37 / 36.0.0 | Required by current dependencies/toolchain; min/target remain 34. |
| libxposed API / Service / Interface | 102.0.0 | Canonical upstream artifacts and metadata are now verified; API remains compileOnly in the APK. |
| AndroidX AppCompat / resources | 1.8.0 | Already aligned; retain. |
| AndroidX Preference / Palette | 1.2.1 / 1.0.0 | Stable existing versions; retain maintenance Preference UI and stable Palette. |
| AndroidX Fragment / KTX | 1.5.4 / 1.5.4 | Correct prior 1.5.4 / 1.3.6 companion drift without a major UI upgrade. |
| AndroidX Core / KTX | 1.13.0 / 1.13.0 | Runtime pair already aligned; retain. |
| Coroutines Android / test | 1.11.0 | App/test aligned. Compiler/Gradle internal versions are isolated from the APK. |
| Commons Lang | 3.21.0 | ClassUtils and MemberUtils bridge are used; retain rather than duplicate reflection machinery. |
| DexKit / FlatBuffers | 2.2.0 / 23.5.26 | Keep after actual-ROM comparison below. |
| cxx / JUnit | 1.2.0 / 4.13.2 | cxx is native build/Prefab input; JUnit is test-only. Neither warrants runtime replacement. |

KGP's published fully tested AGP range ends at 9.3.1, two patch versions below the
existing 9.3.3. Record this coverage boundary; retain the patched, locally verified
AGP rather than downgrade it. Gradle's newer version suggestion alone is not a
measured build-speed benefit. See [Kotlin compatibility](https://kotlinlang.org/docs/gradle-configure-project.html),
[AGP release notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes),
[Gradle verification](https://docs.gradle.org/current/userguide/dependency_verification.html),
and [Fragment releases](https://developer.android.com/jetpack/androidx/releases/fragment).

The baseline had 74 external runtime components, no unresolved dependencies and no
duplicate class definitions across dependency modules. Ordinary Gradle conflict
selection was distinguished from actual family drift and duplicate APK classes.
Cached libxposed artifacts differed from Central's packaging/coordinates, while
class sets, public signatures and changed-class instructions matched. This was a
provenance/reproducibility defect, not evidence of malicious code or an observed ABI failure.
Official [API metadata](https://repo.maven.apache.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0.module)
and [Service metadata](https://repo.maven.apache.org/maven2/io/github/libxposed/service/102.0.0/service-102.0.0.module)
define the selected artifacts.

Seven apparently redundant transitive families (LocalBroadcastManager, AsyncLayoutInflater,
Print, DocumentFile, SwipeRefreshLayout, ViewPager and Window) retained zero class
definitions in the baseline R8 APK. Excluding them has no demonstrated DEX size gain.
Startup/ProfileInstaller have actual manifest/profile inputs and are retained.
Deprecated activity-result/fragment APIs and existing lint warnings are deferred
to a separate UI migration; they are not rewritten as a performance optimization.

## DexKit 2.3.0 comparison and rejection for this release

Use the target Xiaomi 13 / fuxi, HyperOS `V816.0.7.0.UMCTWXM`, Android 14 ART.
Run three interleaved independent processes per version and ROM APK, each doing
21 create/query/close cycles, including repeated warm queries and a negative query.
The screenshot query returns the same exact method descriptor in both versions.
The Global GuardProvider query returns no match in both versions; this is recorded
as an existing ROM coverage limit, not a successful feature lookup.
All 252 native create/query/close cycles finish without an exception or native crash.

| Median, three processes | 2.2.0 | 2.3.0 |
| --- | ---: | ---: |
| Screenshot first create + cold/warm/negative queries + close | 154.01 ms | 320.58 ms |
| Global GuardProvider same cycle | 50.32 ms | 126.83 ms |
| Screenshot PSS change after 20 further cycles | -362 KB | +674 KB |
| Global GuardProvider PSS change after 20 further cycles | +285 KB | +2,656 KB |
| arm64 native library bytes | 381,024 | 394,984 |

These samples include JIT/runtime noise and do not prove a leak or host-process
memory improvement. They provide no stable overall efficiency benefit sufficient
to adopt [DexKit 2.3.0](https://github.com/LuckyPray/DexKit/releases/tag/2.3.0)
in this conservative release, despite upstream optimization and stability fixes.
New experimental query APIs are not used.

## Performance evidence and limits

Actual before/after production callback/formatter bytecode passed 994 equal-result
checks on target ART, including call counts, input ownership and fatal errors.
In the standalone warmed loop, default/unchanged wallpaper callbacks avoided the
previous approximately 32-byte argument-array allocation; rewriting still copies
and incurs an additional comparison. CPU-only formatting with both sources supplied
reduced allocation from about 1,301 to 652 bytes/call. Do not generalize these
numbers to every callback, whole-device PSS, CPU, energy or frame rate.

Host JVM measurements of the actual before/after code showed approximately 2 MiB
less allocation per 2 MiB backup decode and 68.9% less average allocation across six
search inputs. These are host allocation measurements, not Android app measurements.

Existing bounded ClassLoader-scoped reflection caches and monitor/receiver ownership
remain. No resident jobs, additional caches, synchronous Binder calls or hot-path
I/O are introduced. API 101 compatibility checks are static; the connected Vector
framework reports API 102 via its status CLI, so this device is not API 101 framework acceptance.

## JVM 21 / 25 trials and decision

The user permits a larger APK when the higher target gives a clear runtime benefit.
Trial all three targets with the same JDK 25, dependencies, sources, revision and
R8 configuration. JVM tests, signed develop builds and fatal lint pass at 17, 21
and 25. The 21 and 25 final DEX files are byte-identical; each adds 2,148 DEX bytes
and three method references over 17. This size change alone is not a rejection.

On target ART, run three interleaved independent processes per target, with nine
cases and five timed batches each. All 994 behavior checks per process match.
Increasing formatter warm-up and batch iterations from 10,000 to 100,000 changes
the allocation advantage's direction; CPU-only median CPU difference falls from
approximately 23% to 2.8%. The final R8 monitor and decimal-format helper instruction
streams are identical at 17 and 25 after removing DEX addresses/reference indices.
The earlier standalone unminified measurements therefore do not establish a final
APK improvement. Retain output target 17 because a clear stable runtime benefit
was not demonstrated; keep the JDK 25 build runtime. Cold build durations are not
treated as a controlled build-speed benchmark. Revisit higher output targets when
a concrete language feature or repeatable final-APK benefit warrants them.

## APK composition and measured size reduction

The official r14.22.5 APK is 3,885,184 bytes. Its stored DEX is 1,980,048 bytes,
binary resource table 962,848 bytes, compressed resource files 451,087 bytes and
arm64 DexKit library 381,024 bytes. The other ZIP payload, profiles and ZIP/signature
overhead account for the remaining 110,177 bytes.

Controlled r14.22.6 signed develop builds before/after table compaction are
3,900,774 and 3,749,866 bytes: exactly 150,908 bytes saved. The final binary table
is 811,940 bytes, with unchanged DEX and all remaining resource payloads. Full AAPT2
resource dumps are identical. The separately removed coroutine-agent payload is
774 compressed bytes / 1,728 uncompressed bytes, plus its ZIP entry overhead.
These controlled develop sizes are not the published formal release size; the
exact-main signed release is measured and reported separately at publication.

Android 14's native resource reader resolves the compact encoding. A target-device
probe compares 2,733 IDs across ten languages and day/night configurations: 54,660
reads, including 39,860 resolved values, have equal names, types, values, densities
and missing-entry behavior. See [Android 14 resource table format](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/libs/androidfw/include/androidfw/ResourceTypes.h).
The pinned internal AGP task integration requires review when upgrading AGP.

Keep stored DEX/native entries for direct mapping. The native library is already
stripped; unwind tables are required for native exceptions. Test-provider assets
have actual consumers. Dynamic signal resources, Hook callbacks, JNI and preference
reflection require existing R8/consumer rules. Removing those or the already-shrunk
transitive classes offers no safe proven benefit in this release.

Raw ROM samples, snapshots, benchmarks and build/signing evidence stay in ignored
local output. Release acceptance requires the repository full gate, Python tools
tests, exact-main signed APK checks, device smoke and GitHub Full CI; a benchmark
alone is not release acceptance.
