# Testing Patterns

**Analysis Date:** 2026-04-11

## Test Framework

**Runner:**
- JUnit 5 via `testImplementation platform('org.junit:junit-bom:5.10.2')` and `testImplementation 'org.junit.jupiter:junit-jupiter'` in `build.gradle` lines 162-164.
- Config: `build.gradle` lines 202-204 (`test { useJUnitPlatform() }`).
- Forge GameTest is also active through the custom `gametest` source set in `build.gradle` lines 61-74 and the `gameTestServer` run in lines 111-124.

**Assertion Library:**
- `org.junit.jupiter.api.Assertions` for unit-style tests under `src/test/java/`, `recruits/src/test/java/`, and root merge smoke tests in `src/test/java/com/talhanation/workers/`.
- `GameTestHelper` success/failure semantics for GameTests under `recruits/src/gametest/java/com/talhanation/recruits/gametest/`.

**Run Commands:**
```bash
./gradlew test                 # Run JUnit tests from `src/test/java` and `recruits/src/test/java`
./gradlew runGameTestServer    # Run Forge GameTests from `recruits/src/gametest/java`
./gradlew check                # Run assemble + test + runGameTestServer per `build.gradle`
```

## Test File Organization

**Location:**
- Unit tests are split across merged root and preserved recruits sources:
  - `src/test/java/com/talhanation/workers/` for merge-runtime workers smoke coverage.
  - `recruits/src/test/java/com/talhanation/recruits/` for the established recruits unit suite.
- GameTests live in `recruits/src/gametest/java/com/talhanation/recruits/gametest/` with structures in `recruits/src/gametest/resources/data/bannermod/structures/`.
- `build.gradle` includes `src/test/resources` and `recruits/src/test/resources` in the `test` source set, but no test resource files were detected there during this scan.

**Naming:**
- Use `*Test.java` for JUnit classes, for example `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java` and `src/test/java/com/talhanation/workers/WorkersRuntimeLegacyIdMigrationTest.java`.
- Use `*GameTests.java` for Forge GameTest classes, for example `recruits/src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java` and `recruits/src/gametest/java/com/talhanation/recruits/gametest/HarnessSmokeGameTests.java`.
- Use `*Fixtures.java`, `*Assertions.java`, and `*Support.java` for reusable helpers under `recruits/src/test/java/com/talhanation/recruits/testsupport/` and `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/`.

**Structure:**
```text
src/test/java/com/talhanation/workers/*.java
recruits/src/test/java/com/talhanation/recruits/**/**Test.java
recruits/src/test/java/com/talhanation/recruits/testsupport/*.{java}
recruits/src/gametest/java/com/talhanation/recruits/gametest/**/*GameTests.java
recruits/src/gametest/java/com/talhanation/recruits/gametest/support/*.{java}
recruits/src/gametest/resources/data/bannermod/structures/*.nbt
```

## Test Structure

**Suite Organization:**
```java
class RecruitsNetworkRegistrarTest {

    @Test
    void packetRegistrationOrderRemainsStable() {
        RecruitsNetworkRegistrar registrar = new RecruitsNetworkRegistrar();

        List<NetworkBootstrapSeams.MessageRegistration> registrations = registrar.orderedMessageTypes();

        assertEquals(102, registrations.size());
        assertEquals(MessageAggro.class, registrations.get(0).messageClass());
    }
}
```
- Pattern source: `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java`.

**Patterns:**
- Tests favor small, behavior-named methods with arrange/act/assert implied by blank lines instead of explicit comments. See `recruits/src/test/java/com/talhanation/recruits/pathfinding/AsyncPathProcessorTest.java` and `src/test/java/com/talhanation/workers/BuilderBuildProgressSmokeTest.java`.
- Setup is usually inline inside each test. No `@BeforeEach`, `@BeforeAll`, parameterized tests, or shared lifecycle annotations were detected in repository tests.
- Assertions stay direct and concrete. Most tests use `assertEquals`, `assertTrue`, and `assertFalse`; failure messages are rare in JUnit tests but common in GameTests via thrown `IllegalArgumentException`.
- Root merge smoke tests are intentionally narrow and verify compatibility seams rather than full entity behavior, as shown in `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java` and `src/test/java/com/talhanation/workers/WorkersRuntimeLegacyIdMigrationTest.java`.

## Mocking

**Framework:** None detected

**Patterns:**
```java
MessageMovement message = RecruitsFixtures.sampleMovementMessage();
MessageMovement restored = MessageCodecAssertions.assertMovementRoundTrip(message);

assertEquals(RecruitsFixtures.MOVEMENT_PLAYER_UUID, readField(restored, "player_uuid"));
```
- Pattern source: `recruits/src/test/java/com/talhanation/recruits/network/MessageMovementCodecTest.java` plus helpers in `recruits/src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java` and `recruits/src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java`.

**What to Mock:**
- Do not introduce a mocking layer unless absolutely necessary. Current tests avoid Mockito entirely and instead use fixtures, reflection, round-trip helpers, and real packet serialization.
- Prefer seam classes and deterministic fixtures for isolated logic, such as `NetworkBootstrapSeams` in `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java`.

**What NOT to Mock:**
- Do not mock packet buffers when codec behavior is under test; use `FriendlyByteBuf` round trips like `recruits/src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java`.
- Do not mock GameTest world helpers; use real spawned entities and template structures in `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java` and `recruits/src/gametest/resources/data/bannermod/structures/*.nbt`.

## Fixtures and Factories

**Test Data:**
```java
public static MessageMovement sampleMovementMessage() {
    return new MessageMovement(MOVEMENT_PLAYER_UUID, 3, MOVEMENT_GROUP_UUID, 2, true);
}
```
- Pattern source: `recruits/src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java`.

```java
public static <T extends AbstractRecruitEntity> T spawnConfiguredRecruit(
        GameTestHelper helper, EntityType<T> entityType, BlockPos relativePos, String customName, UUID ownerId) {
    T recruit = spawnRecruit(helper, entityType, relativePos);
    recruit.setCustomName(Component.literal(customName));
    recruit.setFollowState(2);
    applyBattleLoadout(recruit);
    assignOwners(List.of(recruit), ownerId);
    return recruit;
}
```
- Pattern source: `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java`.

**Location:**
- Unit fixtures and assertion helpers live in `recruits/src/test/java/com/talhanation/recruits/testsupport/`.
- GameTest world setup helpers live in `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/`.
- Template structures for GameTests live in `recruits/src/gametest/resources/data/bannermod/structures/`.

## Coverage

**Requirements:** None enforced by tooling
- No JaCoCo or other coverage plugin is configured in `build.gradle`.
- Practical coverage signals come from breadth of targeted suites: networking, persistence, pathfinding, client sync, compat, build metadata, merge runtime helpers, and command/battle/persistence GameTests.
- `build.gradle` wires `check` to `assemble`, `test`, and `runGameTestServer`, so both JUnit and GameTest suites are part of the default verification gate.

**View Coverage:**
```bash
Not available by default; no coverage report task is configured in `build.gradle`.
```

## Test Types

**Unit Tests:**
- Focus on pure or seam-friendly logic under `recruits/src/test/java/com/talhanation/recruits/` and `src/test/java/com/talhanation/workers/`.
- Common targets include packet registration order (`recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java`), packet codecs (`recruits/src/test/java/com/talhanation/recruits/network/MessageMovementCodecTest.java`), runtime helpers (`src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java`), persistence serialization, and async path helpers.

**Integration Tests:**
- Light integration-style behavior is covered inside JUnit when real Minecraft data structures are cheap to instantiate, such as NBT migration in `src/test/java/com/talhanation/workers/WorkersRuntimeLegacyIdMigrationTest.java` and build metadata inspection in `recruits/src/test/java/com/talhanation/recruits/build/BuildBaselineTest.java`.

**E2E Tests:**
- Forge GameTest is the repository’s end-to-end gameplay verification path.
- Current GameTests cover harness boot, recruit spawn, command authority, formation recovery, persistence sync, and mixed-squad battle scenarios in `recruits/src/gametest/java/com/talhanation/recruits/gametest/**`.
- No workers-specific GameTests were detected under `src/gametest/java/` or `workers/src/gametest/java/`.

## Common Patterns

**Async Testing:**
```java
helper.runAfterDelay(10, () -> {
    for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
        if (!recruit.isInFormation) {
            throw new IllegalArgumentException("Expected targeted recruit to enter formation via packet path");
        }
    }
    helper.succeed();
});
```
- Pattern source: `recruits/src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java`.

**Error Testing:**
```java
private static Object readField(Object instance, String fieldName) {
    try {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    } catch (ReflectiveOperationException e) {
        throw new IllegalArgumentException("Unable to read message field: " + fieldName, e);
    }
}
```
- Pattern source: `recruits/src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java`.

## Verification Workflow

- For logic-only changes, add or update JUnit coverage beside the affected area in `recruits/src/test/java/com/talhanation/recruits/` or `src/test/java/com/talhanation/workers/`, then run `./gradlew test`.
- For gameplay, networking, command-radius, persistence, or formation behavior, add or update a Forge GameTest in `recruits/src/gametest/java/com/talhanation/recruits/gametest/` and matching template data in `recruits/src/gametest/resources/data/bannermod/structures/`, then run `./gradlew runGameTestServer`.
- Before handoff, prefer `./gradlew check` because `build.gradle` makes it the closest thing to a full repository verification pass.
- Preserve the existing split: root `src/test/java/com/talhanation/workers/` is currently for merged workers/runtime smoke tests, while broader legacy/runtime coverage remains under `recruits/src/test/java/com/talhanation/recruits/`.

---

*Testing analysis: 2026-04-11*
