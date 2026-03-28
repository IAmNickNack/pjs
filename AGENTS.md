# AGENTS.md

## What this repo is
- PJs is a JVM hardware abstraction layer: application code targets `Port`/`Pin`/`SerialPort` APIs, while transport-specific adapters live in provider modules (`README.md`, `docs/PORTS.md`, `docs/SERIAL.md`).
- Core API and contracts are in `pjs-core`; shared utility/logging helpers are in `pjs-utils`.
- Real IO is selected at runtime by `DeviceRegistryLoader` implementations (mock, gRPC, FFM, Pi4J), discovered via Java `ServiceLoader` (`pjs-core/src/main/java/io/github/iamnicknack/pjs/model/device/DeviceRegistryLoader.java`).

## Build topology you need to respect
- This is a composite Gradle build: root includes `build-logic`, `providers`, and `sandbox` (`settings.gradle.kts`).
- Provider and sandbox builds substitute published coordinates with local projects for iterative dev (`providers/settings.gradle.kts`, `sandbox/settings.gradle.kts`).
- Convention plugins live in `build-logic/src/main/kotlin/buildlogic/**`; do not hand-roll per-module build config when a convention plugin already exists.

## Architecture and boundaries
- `pjs-core` exports abstraction and model packages and `uses DeviceRegistryLoader` in `module-info.java`.
- Provider modules contribute implementations. Example explicit JPMS provider registration:
  - `providers/network/pjs-grpc/pjs-grpc-device/src/main/java/module-info.java`
  - `providers/pjs-pi4j-device/src/main/java/module-info.java`
- Some providers rely on service descriptors instead of `provides` clauses (example: `providers/pjs-mock-device/src/main/resources/META-INF/services/io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader`).
- Network servers resolve registries from config and fall back to mock (`providers/network/pjs-network-common/src/main/kotlin/io/github/iamnicknack/pjs/server/ConfigurableDeviceRegistryProvider.kt`).

## Runtime configuration conventions
- Provider selection is property-driven (`pjs.mode`): `mock`, `grpc`, `ffm`, `pi4j`.
- Remote transports use proxy props `pjs.proxy.host` / `pjs.proxy.port` (gRPC loader), while Pi4J loader maps `pjs.grpc.*` to `pi4j.grpc.*` when present.
- Optional logging decoration is done by wrapping registries (`LoggingDeviceRegistry`) rather than changing device APIs.

## Build and test workflows (repo-root commands)
- Full build: `./gradlew build`
- All tests: `./gradlew test`
- Target module tests: `./gradlew :pjs-core:test` or `./gradlew :pjs-grpc-device:test`
- Build server distributions (documented):
  - `./gradlew :pjs-grpc-server:installDist`
  - `./gradlew :pjs-http-server:installDist`
- Print effective project version: `./gradlew printVersion`

## Code patterns to follow in edits
- Keep module boundaries explicit: when adding exported APIs or service providers, update the relevant `module-info.java`.
- Reuse config/building style: builders for device configs (`GpioPortConfig.builder()`, `SpiConfig.builder()`, etc.), registry-centric device creation.
- Test style is mixed by language: Java tests commonly use AssertJ; Kotlin tests often use `kotlin.test`/AssertK + JUnit 5 dynamic tests.
- Java/Kotlin mixed modules using JPMS require `buildlogic.kotlin-java-module-system` and `javaModuleSystem.moduleName = "..."` (example in `providers/network/pjs-grpc/pjs-grpc-device/build.gradle.kts`).

## Practical guardrails for agents
- Prefer local project dependencies already wired by substitution over adding new external coordinates.
- Keep Java toolchain assumptions at 25 (`buildlogic/java-core.gradle.kts`, `buildlogic/kotlin-core.gradle.kts`).
- For provider behavior changes, validate both loader selection (`isLoadable`) and property key compatibility.

