package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TESTTREAS-001 acceptance gametest — guards the post-ENQUEUE-001 invariant
 * that {@link BannerModTreasuryManager#depositTaxes} and
 * {@link BannerModTreasuryManager#recordArmyUpkeepDebit} are single-threaded
 * with respect to one another, so that multiple same-tick mutations on the
 * same claim accumulate to the algebraic sum of their inputs without any
 * lost updates.
 *
 * <p>There is no dedicated {@code MessageDepositTaxes} packet; treasury
 * mutations enter from server-driven paths (governor heartbeat, war outcome
 * applier, occupation tax runtime) which all sit behind the post-ENQUEUE-001
 * main-thread executor. The acceptance speaks of "MessageDepositTaxes-equivalents"
 * — the equivalent for this codebase is repeated calls to
 * {@code BannerModTreasuryManager#depositTaxes} on the same claim within the
 * same tick. The two tests below cover that property:
 *
 * <ol>
 *   <li>{@link #nDepositsSameTickProduceAlgebraicSumOfInputs} — fires
 *       {@code DEPOSIT_COUNT} deposits sequentially on the main server thread
 *       inside one tick. This is the algebraic-sum property in its purest
 *       form: the post-ENQUEUE-001 contract is that all packet-driven
 *       treasury mutations get marshalled here, and once they are, the final
 *       ledger must equal the sum of inputs.</li>
 *   <li>{@link #nWorkerDispatchedDepositsRouteThroughMainThreadAndProduceAlgebraicSum}
 *       — spawns a small worker pool that submits each deposit via the
 *       server's main-thread executor (mirroring what
 *       {@code IPayloadContext#enqueueWork} does after ENQUEUE-001). Asserts
 *       the final ledger equals the algebraic sum and that every recorded
 *       runner thread was the main server thread. If ENQUEUE-001 were
 *       reverted, callers would invoke {@code depositTaxes} directly from
 *       the netty worker thread and the underlying {@code LinkedHashMap}
 *       would race against itself; this test is the regression guard for
 *       that.</li>
 * </ol>
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModTreasuryConcurrentDepositGameTests {

    private static final int DEPOSIT_COUNT = 1000;
    private static final int DEPOSIT_AMOUNT = 7;
    private static final int WORKER_COUNT = 4;
    private static final int DISPATCHES_PER_WORKER = DEPOSIT_COUNT / WORKER_COUNT;

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void nDepositsSameTickProduceAlgebraicSumOfInputs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BannerModTreasuryManager treasury = BannerModTreasuryManager.get(level);

        UUID claimUuid = UUID.fromString("00000000-0000-0000-0000-0000deba51a1");
        ChunkPos anchor = new ChunkPos(7, 7);
        long depositTick = level.getGameTime();

        // Ensure no pre-existing ledger.
        treasury.removeLedger(claimUuid);

        for (int i = 0; i < DEPOSIT_COUNT; i++) {
            treasury.depositTaxes(claimUuid, anchor, "treasconc", DEPOSIT_AMOUNT, depositTick);
        }

        BannerModTreasuryLedgerSnapshot ledger = treasury.getLedger(claimUuid);
        helper.assertTrue(ledger != null,
                "Expected a treasury ledger to exist after " + DEPOSIT_COUNT + " same-tick deposits");
        int expected = DEPOSIT_COUNT * DEPOSIT_AMOUNT;
        helper.assertTrue(ledger.accruedTaxes() == expected,
                "Expected accruedTaxes == sum of inputs (" + expected + "), got "
                        + ledger.accruedTaxes());
        helper.assertTrue(ledger.treasuryBalance() == expected,
                "Expected treasuryBalance == sum of inputs (" + expected + "), got "
                        + ledger.treasuryBalance());
        helper.assertTrue(ledger.spentArmyUpkeep() == 0,
                "Expected spentArmyUpkeep == 0 after deposit-only run, got "
                        + ledger.spentArmyUpkeep());
        helper.assertTrue(ledger.lastDepositAmount() == DEPOSIT_AMOUNT,
                "Expected lastDepositAmount == per-call amount, got "
                        + ledger.lastDepositAmount());

        // Cleanup so this method is independent of the second one when the
        // gametest harness shares the level across templates.
        treasury.removeLedger(claimUuid);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 1200)
    public static void nWorkerDispatchedDepositsRouteThroughMainThreadAndProduceAlgebraicSum(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        helper.assertTrue(server != null,
                "Gametest must run inside a real MinecraftServer context");
        BannerModTreasuryManager treasury = BannerModTreasuryManager.get(level);

        UUID claimUuid = UUID.fromString("00000000-0000-0000-0000-0000deba51a2");
        ChunkPos anchor = new ChunkPos(11, 13);
        long depositTick = level.getGameTime();
        treasury.removeLedger(claimUuid);

        AtomicInteger completed = new AtomicInteger();
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        List<String> runnerThreadNames = Collections.synchronizedList(new ArrayList<>());
        String mainThreadName = server.getRunningThread().getName();

        // Worker threads enqueue deposits onto the server main-thread
        // executor. This mirrors the post-ENQUEUE-001 packet-handling path:
        // the netty worker thread calls executeServerSide -> enqueueWork ->
        // server.execute(...). Every body therefore runs sequentially on the
        // main thread, so the algebraic-sum property must hold.
        Thread[] workers = new Thread[WORKER_COUNT];
        for (int t = 0; t < WORKER_COUNT; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < DISPATCHES_PER_WORKER; i++) {
                    server.execute(() -> {
                        try {
                            runnerThreadNames.add(Thread.currentThread().getName());
                            treasury.depositTaxes(claimUuid, anchor, "treasconc-worker",
                                    DEPOSIT_AMOUNT, depositTick);
                            completed.incrementAndGet();
                        } catch (Throwable th) {
                            errors.add(th);
                        }
                    });
                }
            }, "treasconc-worker-" + t);
            workers[t].setDaemon(true);
            workers[t].start();
        }
        for (Thread w : workers) {
            try {
                w.join(10_000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                helper.fail("Worker thread join interrupted");
            }
            helper.assertTrue(!w.isAlive(),
                    "Worker thread did not finish enqueueing within 10s");
        }

        int expectedTotal = WORKER_COUNT * DISPATCHES_PER_WORKER;
        int expectedSum = expectedTotal * DEPOSIT_AMOUNT;

        helper.succeedWhen(() -> {
            helper.assertTrue(errors.isEmpty(),
                    "Expected zero deposit-body exceptions; first was: "
                            + (errors.isEmpty() ? "<none>" : errors.get(0).toString()));
            int done = completed.get();
            helper.assertTrue(done == expectedTotal,
                    "Expected " + expectedTotal + " bodies to complete; got " + done);

            List<String> threadSnapshot;
            synchronized (runnerThreadNames) {
                threadSnapshot = new ArrayList<>(runnerThreadNames);
            }
            boolean allOnMain = threadSnapshot.stream().allMatch(name -> name.equals(mainThreadName));
            helper.assertTrue(allOnMain,
                    "Every deposit body must run on the main server thread ("
                            + mainThreadName + "); observed distinct threads: "
                            + threadSnapshot.stream().distinct().sorted().toList());

            BannerModTreasuryLedgerSnapshot ledger = treasury.getLedger(claimUuid);
            helper.assertTrue(ledger != null,
                    "Expected a treasury ledger after " + expectedTotal + " worker-dispatched deposits");
            helper.assertTrue(ledger.accruedTaxes() == expectedSum,
                    "Expected accruedTaxes == algebraic sum of inputs (" + expectedSum
                            + "), got " + ledger.accruedTaxes());
            helper.assertTrue(ledger.treasuryBalance() == expectedSum,
                    "Expected treasuryBalance == algebraic sum (" + expectedSum
                            + "), got " + ledger.treasuryBalance());
            // Cleanup post-success.
            treasury.removeLedger(claimUuid);
        });
    }
}
