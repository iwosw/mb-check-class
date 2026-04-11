# Technology Stack

**Project:** Villager Recruits stabilization and 1.21.1 migration prep
**Researched:** 2026-04-05

## Recommended Stack

### Core Framework
| Technology | Version | Purpose | Why | Confidence |
|------------|---------|---------|-----|------------|
| Minecraft Forge | `1.20.1-47.4.10` on stabilization branch | Runtime mod API, registries, lifecycle, networking, GameTest support | Keep the branch on its current Forge line while hardening behavior. This avoids mixing stabilization work with platform churn. Forge docs explicitly support `runGameTestServer`, dedicated server testing, `SavedData`, and `SimpleChannel`, which are exactly the brownfield seams this mod relies on. | HIGH |
| ForgeGradle | pinned `6.x` exact release, not `6.+` | Dev workspace, reobf, runs, mappings, packaging | Stay on FG6 for 1.20.1, but stop using dynamic plugin versions. Gradle explicitly warns about dynamic versions destabilizing builds; brownfield migration prep needs reproducibility more than convenience. | HIGH |
| Mojang official mappings | `official` for current branch | Human-readable names with minimum mapping churn | The repo already uses `official`. Keep that for stabilization so test-writing and refactors do not get buried under remap noise. ForgeGradle documents `official` as the built-in modern mapping set; this is the least disruptive path into 1.21.1. | HIGH |
| Parchment mappings via Librarian | optional, only after tests exist | Extra parameter names / javadocs during heavy refactors | Parchment is useful, but it is not the first move. For this project, add it only if missing parameter names are materially slowing refactors. Otherwise it creates avoidable diff churn right before a version migration. | MEDIUM |

### Database
| Technology | Version | Purpose | Why | Confidence |
|------------|---------|---------|-----|------------|
| Minecraft `SavedData` + NBT | Forge/Minecraft built-in | Persistent world/faction/group/team state | This mod already uses `SavedData`, and Forge documents it as the right per-level persistence mechanism. The stabilization focus should be on wrapping and testing it, not replacing it. For migration prep, isolate serialization logic behind adapters/helpers so 1.21.1 persistence changes are localized. | HIGH |
| Codec-oriented serialization helpers | incremental adoption on touched systems | Reduce brittle hand-written tag code in high-risk managers | Forge’s data storage docs now emphasize codecs in modern versions. Do not rewrite all persistence now, but introduce codecs or codec-like translators where tests expose fragile NBT code. This lowers future migration and save-compat risk. | MEDIUM |

### Infrastructure
| Technology | Version | Purpose | Why | Confidence |
|------------|---------|---------|-----|------------|
| Java toolchains | Java 17 required now; add Java 21 prep lane | Reproducible local/CI JDK selection | Forge 1.20.x docs require Java 17; Forge 1.21.x docs require Java 21. Use Gradle toolchains so the current branch stays correct while a non-blocking migration-prep lane can verify extracted pure-Java code against 21 later. | HIGH |
| Gradle Wrapper | keep wrapper pinned; current repo is `8.8` | Reproducible builds across dev and CI | Keep a checked-in wrapper and pin exact versions. Do not let IDE/JAVA_HOME drive behavior. Gradle toolchain docs explicitly recommend toolchains over environment drift. | HIGH |
| Version catalog | `gradle/libs.versions.toml` | Centralize plugin/test/dependency versions | This repo has many dependencies and optional compat jars. A catalog makes brownfield upgrades safer and smaller. It is especially valuable when splitting current runtime/test/publishing concerns before 1.21.1. | HIGH |
| Dependency locking | `gradle.lockfile` + `buildscript-gradle.lockfile` | Freeze resolved dependency graph | Required because the current build uses dynamic versions and many third-party repos. Locking prevents surprise breakage while doing stabilization and lets migration diffs show intentional changes only. | HIGH |
| Dependency verification | `gradle/verification-metadata.xml` | Supply-chain protection for public mod repos | This mod resolves from Forge, Sponge, CurseMaven, Maven Central, and others. Gradle explicitly recommends verification for compromised artifact defense. That matters more than usual in mod builds with niche repos. | HIGH |
| Repository management in `settings.gradle` with content filters | current Gradle best practice | Control where dependencies/plugins resolve from | Move repos out of ad hoc project blocks and constrain CurseMaven/other niche repos to expected groups only. This improves reliability and reduces accidental dependency source drift during migration prep. | HIGH |
| GitHub Actions CI | `actions/setup-java@v4` + `gradle/actions/setup-gradle@v4` | Build/test automation, cache, artifact retention | GitHub’s Gradle guidance and Gradle’s own action are the standard hosted-CI path. Use CI to run `build`, unit tests, GameTest server runs, and archive logs/reports from failures. | HIGH |

### Supporting Libraries
| Library | Version | Purpose | When to Use | Confidence |
|---------|---------|---------|-------------|------------|
| Forge GameTest framework | built into Forge 1.20.1 | In-game behavioral tests for AI, combat, commands, persistence, packet-triggered flows | Use for anything requiring a real level/tick loop: formations, attack behavior, persistence save/load, command side effects, menu/network round trips. This should be the primary integration test layer. | HIGH |
| JUnit Jupiter | `5.14.x` preferred on this branch | Pure JVM tests for extracted logic | Use for deterministic logic extracted out of entities/managers: command validation, selectors, serializers, formation math, diplomacy rules, path choice heuristics, packet encode/decode. Prefer 5.14.x now because it is mature and avoids introducing another major-version migration during stabilization. | MEDIUM |
| Mockito + `mockito-junit-jupiter` | `5.x` | Narrow mocking in JVM tests | Use sparingly at seams you own: facades, adapters, collaborators extracted from Forge-heavy code. Do **not** try to mock half of Minecraft; that produces fake confidence and brittle tests. | MEDIUM |
| JaCoCo | `0.8.14` via Gradle plugin defaults | Unit-test coverage reporting | Use for JVM-unit coverage only. It is useful for tracking extracted logic and ensuring new seams stay tested, but it should not be treated as authoritative for GameTest-driven gameplay coverage. | HIGH |

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Mappings | `official` | Parchment immediately | Parchment is useful, but doing a mapping switch before stabilization adds diff noise and review overhead right before a 1.21.1 port. Add it only if refactor throughput is suffering. |
| Test strategy | Forge GameTest + JUnit | Manual-only QA on dev runs | Forge explicitly supports GameTests and dedicated-server validation. Manual-only testing is too weak for a large NPC/AI/network mod. |
| Unit test style | extracted pure-Java seams + light mocks | deep mocking of Forge/Minecraft internals | Mockito itself warns against mocking everything. For Forge mods, deep mocks usually encode fake engine behavior and collapse during ports. |
| Build dependency policy | exact versions + lockfiles | dynamic versions like `6.+` / `5.+` | Gradle documents dynamic versions as a reproducibility risk. They are the opposite of what a stabilization branch needs. |
| Repository policy | settings-level repos + content filters | broad repos in `build.gradle` including `jcenter()` | `jcenter()` is deprecated/removed from modern Gradle flows and JCenter is read-only. Broad repo usage also increases supply-chain and resolution ambiguity. |
| Publishing tooling | isolate publishing from core verification build | keep CurseGradle in the main maintenance path | Publishing plugins do not help stabilization/testing and often break unrelated work. Keep release automation separate from the verification stack. |
| Cross-loader abstraction | stay Forge-native while carving internal seams | adopt Architectury/multiloader now | This project is not doing a cross-loader rewrite; that would explode scope. Internal adapter seams are enough for near-term 1.21.1 prep. |

## Installation

```bash
# Core brownfield test stack
./gradlew dependencies --write-locks
./gradlew --write-verification-metadata sha256 help

# Add test dependencies (representative)
# testImplementation("org.junit.jupiter:junit-jupiter:5.14.x")
# testImplementation("org.mockito:mockito-core:5.x")
# testImplementation("org.mockito:mockito-junit-jupiter:5.x")

# Core verification tasks to wire into CI
./gradlew test
./gradlew runGameTestServer
./gradlew build
```

## Brownfield Guidance for Villager Recruits

- **Do first:** pin ForgeGradle/plugin versions, remove `jcenter()`, move repositories to `settings.gradle`, add version catalog, lockfiles, and verification metadata.
- **Then:** introduce two test layers only: **JUnit for extracted pure logic** and **Forge GameTests for game behavior**.
- **Do not** start by rewriting the whole mod into services. First carve seams around the riskiest existing subsystems: `SavedData` managers, packet encode/decode/dispatch, command handlers, formation math, combat decision helpers, and pathfinding policy objects.
- **Do not** try to make the whole 1.20.1 mod run under Java 21 immediately. Keep branch truth on Java 17, but make extracted non-Forge logic 21-clean where practical.
- **For 1.21.1 prep:** centralize version-sensitive code now: networking registration, menu/screen buffer handling, persistence codecs/NBT translators, mixin targets, and any direct vanilla internals touched by pathfinding.

## What NOT to Use

- **Do not use `jcenter()`**: Gradle deprecated it, JCenter is read-only, and it adds avoidable resolution risk.
- **Do not keep dynamic dependency/plugin versions** on the stabilization branch.
- **Do not rely on manual in-client testing as primary verification** for AI/network/persistence behavior.
- **Do not over-invest in heavyweight static-analysis rollout first** (`ErrorProne`, `NullAway`, big Checkstyle rewrites). For this brownfield mod, tests and seam extraction will pay off sooner with less noise.
- **Do not expand platform scope** with Architectury/multiloader or loader-port abstractions during this milestone.

## Migration Implications for Minecraft 1.21.1

1. **Java baseline changes from 17 to 21** according to Forge docs, so toolchains must be explicit now.
2. **Keeping `official` mappings** minimizes one whole class of churn during the eventual port.
3. **GameTests written now survive the port better than manual QA notes** and give immediate regression checks on formations, combat, and persistence.
4. **Version catalogs + lockfiles + verification metadata** make the actual 1.21.1 dependency bump auditable instead of chaotic.
5. **Testing extracted pure-Java policy code** reduces the amount of logic that must be revalidated only inside Minecraft after the port.

## Sources

- Forge getting started 1.20.x (Java 17, dedicated server testing): https://docs.minecraftforge.net/en/1.20.x/gettingstarted/
- Forge getting started 1.21.x (Java 21): https://docs.minecraftforge.net/en/1.21.x/gettingstarted/
- Forge GameTest docs: https://docs.minecraftforge.net/en/1.20.x/misc/gametest/
- Forge `SavedData` docs: https://docs.minecraftforge.net/en/1.20.x/datastorage/saveddata/
- Forge `SimpleChannel` / SimpleImpl docs: https://docs.minecraftforge.net/en/1.20.x/networking/simpleimpl/
- ForgeGradle 6 configuration and mappings docs: https://docs.minecraftforge.net/en/fg-6.x/configuration/
- Parchment getting started: https://parchmentmc.org/docs/getting-started
- Gradle toolchains: https://docs.gradle.org/current/userguide/toolchains.html
- Gradle dependency locking: https://docs.gradle.org/current/userguide/dependency_locking.html
- Gradle dependency verification: https://docs.gradle.org/current/userguide/dependency_verification.html
- Gradle version catalogs: https://docs.gradle.org/current/userguide/version_catalogs.html
- Gradle dependency best practices / repository filtering: https://docs.gradle.org/current/userguide/best_practices_dependencies.html
- Gradle JaCoCo plugin: https://docs.gradle.org/current/userguide/jacoco_plugin.html
- Gradle JCenter shutdown guidance: https://blog.gradle.org/jcenter-shutdown
- GitHub Actions: building/testing Java with Gradle: https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-gradle
- JUnit user guide: https://junit.org/junit5/docs/current/user-guide/
- Mockito project docs: https://site.mockito.org/
