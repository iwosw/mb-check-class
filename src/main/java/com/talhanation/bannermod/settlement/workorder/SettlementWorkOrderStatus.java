package com.talhanation.bannermod.settlement.workorder;

/**
 * Lifecycle state of a {@link SettlementWorkOrder}. Transitions are driven by
 * {@link SettlementWorkOrderRuntime}.
 *
 * <pre>
 *   PENDING --claim()--> CLAIMED --complete()--> COMPLETED (removed)
 *           \                       \--cancel()--> CANCELLED (removed)
 *            \                       \--release/expire()--> PENDING
 *             \--cancel()--> CANCELLED (removed)
 * </pre>
 */
public enum SettlementWorkOrderStatus {
    PENDING,
    CLAIMED,
    COMPLETED,
    CANCELLED
}
