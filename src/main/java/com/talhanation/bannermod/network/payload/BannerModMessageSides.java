package com.talhanation.bannermod.network.payload;

import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks which {@link PacketFlow} sides a {@link BannerModMessage} implementation has
 * declared a handler for. Populated at packet registration time so that
 * {@link BannerModMessage#invokeLegacy(String, Class, Object)} can distinguish
 * between intentional one-way packets (declared only one side) and bugs such as
 * misspelled handler method names (declared neither side, or declared the
 * requested side but reflection cannot find the method).
 *
 * <p>Background: prior to REFLLOG-001 the legacy adapter swallowed every
 * {@link NoSuchMethodException} on the assumption "this is a one-way packet".
 * That mask hid typos like {@code exectueServerSide} until they shipped. With
 * the side registry in place we can:
 * <ul>
 *   <li>fail bootstrap if a packet class declares neither
 *       {@code executeServerSide(BannerModNetworkContext)} nor
 *       {@code executeClientSide(BannerModNetworkContext)} (because then the
 *       class cannot meaningfully handle anything),</li>
 *   <li>swallow the exception only when the packet legitimately did not declare
 *       a handler for the requested side,</li>
 *   <li>raise a loud {@code LOGGER.error} + {@link IllegalStateException} when
 *       the side <em>was</em> declared yet reflection still fails to find the
 *       handler &mdash; that is the typo / refactor signal we want to surface.</li>
 * </ul>
 */
public final class BannerModMessageSides {

    private static final Logger LOGGER = LogManager.getLogger("BannerModMessageSides");

    static final String SERVER_SIDE_METHOD = "executeServerSide";
    static final String CLIENT_SIDE_METHOD = "executeClientSide";

    private static final ConcurrentMap<Class<?>, Set<PacketFlow>> DECLARED_SIDES = new ConcurrentHashMap<>();

    private BannerModMessageSides() {
    }

    /**
     * Scan {@code messageClass} for declared handler methods and record the
     * resulting side set. Throws {@link IllegalStateException} (and emits
     * {@code LOGGER.error}) if the class declares neither
     * {@code executeServerSide(BannerModNetworkContext)} nor
     * {@code executeClientSide(BannerModNetworkContext)} &mdash; in that
     * configuration the packet cannot do useful work and almost always means
     * the implementer misspelled a handler name.
     */
    public static Set<PacketFlow> register(Class<?> messageClass) {
        Set<PacketFlow> sides = scan(messageClass);
        if (sides.isEmpty()) {
            // Typo detection: if no canonical handler exists but the class still
            // declares a method shaped like a side handler (one
            // BannerModNetworkContext parameter, void return, name similar to
            // execute*Side), surface it as the misspelled handler. This is the
            // bug REFLLOG-001 stops swallowing.
            String typo = findHandlerLikeTypo(messageClass);
            if (typo != null) {
                String msg = "Packet class " + messageClass.getName()
                        + " declares a side-handler-shaped method '" + typo + "'"
                        + " but none of executeServerSide(BannerModNetworkContext)"
                        + " or executeClientSide(BannerModNetworkContext)."
                        + " This looks like a misspelled handler (expected"
                        + " 'executeServerSide' or 'executeClientSide').";
                LOGGER.error(msg);
                throw new IllegalStateException(msg);
            }
            // Fallback: a packet whose only handler is annotated @OnlyIn(Dist.CLIENT)
            // (typical for clientbound screen-open packets) will have that method
            // physically stripped from its bytecode on a dedicated server. Trust
            // the executing side declared on the instance as authoritative in
            // that case &mdash; we cannot prove the handler exists on the missing
            // side, but the class clearly intends to be a one-way packet.
            PacketFlow executingSide = probeExecutingSide(messageClass);
            if (executingSide != null) {
                sides = EnumSet.of(executingSide);
            }
        }
        if (sides.isEmpty()) {
            String msg = "Packet class " + messageClass.getName()
                    + " declares neither executeServerSide(BannerModNetworkContext)"
                    + " nor executeClientSide(BannerModNetworkContext) and exposes"
                    + " no executing-side hint. Check for a missing override or a"
                    + " missing no-arg constructor.";
            LOGGER.error(msg);
            throw new IllegalStateException(msg);
        }
        DECLARED_SIDES.put(messageClass, sides);
        return sides;
    }

    /**
     * @return the name of the first declared method that looks like a misspelled
     *         side handler (single {@link BannerModNetworkContext} parameter,
     *         {@code void} return, name not one of the canonical handler
     *         names), or {@code null} if no such method exists.
     */
    private static String findHandlerLikeTypo(Class<?> messageClass) {
        for (Class<?> c = messageClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (m.getParameterTypes()[0] != BannerModNetworkContext.class) continue;
                if (m.getReturnType() != void.class) continue;
                String name = m.getName();
                if (SERVER_SIDE_METHOD.equals(name) || CLIENT_SIDE_METHOD.equals(name)) continue;
                return name;
            }
        }
        return null;
    }

    /**
     * Try to instantiate the packet via its no-arg constructor and ask
     * {@link BannerModMessage#getExecutingSide()} where it intends to run.
     * Returns {@code null} if the class has no accessible no-arg constructor,
     * is not a {@link BannerModMessage}, or instantiation throws.
     */
    private static PacketFlow probeExecutingSide(Class<?> messageClass) {
        if (!BannerModMessage.class.isAssignableFrom(messageClass)) {
            return null;
        }
        try {
            Object instance = messageClass.getDeclaredConstructor().newInstance();
            return ((BannerModMessage<?>) instance).getExecutingSide();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    /**
     * @return {@code true} if {@code messageClass} was registered and declares a
     *         handler for {@code side}. Returns {@code false} when the class is
     *         either unknown to the registry or known but did not declare that
     *         side.
     */
    public static boolean declares(Class<?> messageClass, PacketFlow side) {
        Set<PacketFlow> sides = DECLARED_SIDES.get(messageClass);
        return sides != null && sides.contains(side);
    }

    /**
     * @return {@code true} once {@link #register(Class)} has accepted this class.
     *         Used by {@link BannerModMessage#invokeLegacy(String, Class, Object)}
     *         to keep the legacy "swallow NoSuchMethodException" behaviour for
     *         classes that bypass the catalog (notably the {@code fromBytes} /
     *         {@code toBytes} reflection paths and unit tests that instantiate
     *         packets directly).
     */
    public static boolean isRegistered(Class<?> messageClass) {
        return DECLARED_SIDES.containsKey(messageClass);
    }

    /** Test hook: forget a registration so the same class can be re-registered. */
    static void forget(Class<?> messageClass) {
        DECLARED_SIDES.remove(messageClass);
    }

    private static Set<PacketFlow> scan(Class<?> messageClass) {
        EnumSet<PacketFlow> sides = EnumSet.noneOf(PacketFlow.class);
        if (declaresHandler(messageClass, SERVER_SIDE_METHOD)) {
            sides.add(PacketFlow.SERVERBOUND);
        }
        if (declaresHandler(messageClass, CLIENT_SIDE_METHOD)) {
            sides.add(PacketFlow.CLIENTBOUND);
        }
        return sides;
    }

    private static boolean declaresHandler(Class<?> messageClass, String methodName) {
        for (Class<?> c = messageClass; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(methodName, BannerModNetworkContext.class);
                // The base interface provides default no-op implementations; only
                // count handlers declared on a concrete class (not on the
                // BannerModMessage interface itself).
                if (!m.getDeclaringClass().isInterface()) {
                    return true;
                }
            } catch (NoSuchMethodException ignored) {
                // keep walking the hierarchy
            }
        }
        return false;
    }
}
