package com.talhanation.bannermod.network.payload;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Adapter for legacy packet classes while their registration/sending runs on NeoForge payload APIs.
 */
public interface BannerModMessage<T extends BannerModMessage<T>> extends Message<T> {
    Logger LEGACY_LOGGER = LogManager.getLogger("BannerModMessage");

    PacketFlow getExecutingSide();

    default T fromBytes(FriendlyByteBuf buf) {
        @SuppressWarnings("unchecked")
        T self = (T) this;
        return self;
    }

    default void toBytes(FriendlyByteBuf buf) {
    }

    default void executeServerSide(BannerModNetworkContext context) {
    }

    default void executeClientSide(BannerModNetworkContext context) {
    }

    @Override
    default CustomPacketPayload.Type<T> type() {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                BannerModMain.MOD_ID,
                getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT)
        ));
    }

    @SuppressWarnings("unchecked")
    @Override
    default T fromBytes(RegistryFriendlyByteBuf buf) {
        invokeLegacy("fromBytes", FriendlyByteBuf.class, buf);
        return (T) this;
    }

    @Override
    default void toBytes(RegistryFriendlyByteBuf buf) {
        invokeLegacy("toBytes", FriendlyByteBuf.class, buf);
    }

    @Override
    default void executeServerSide(IPayloadContext context) {
        invokeLegacy("executeServerSide", BannerModNetworkContext.class, new BannerModNetworkContext(context));
    }

    @Override
    default void executeClientSide(IPayloadContext context) {
        invokeLegacy("executeClientSide", BannerModNetworkContext.class, new BannerModNetworkContext(context));
    }

    default void invokeLegacy(String methodName, Class<?> parameterType, Object argument) {
        try {
            Method method = getClass().getMethod(methodName, parameterType);
            method.invoke(this, argument);
        } catch (NoSuchMethodException missing) {
            // For the side-handler reflection paths (executeServerSide/executeClientSide),
            // distinguish between an intentional one-way packet (we never declared the
            // opposite side) and a typo / refactor bug (we DID declare this side at
            // registration time but reflection still cannot find the method, e.g.
            // someone wrote `exectueServerSide`). The latter is what REFLLOG-001 stops
            // swallowing: the registration scan recorded the declared sides, so a
            // mismatch here is observable and loud.
            PacketFlow side = sideForHandler(methodName);
            if (side != null
                    && BannerModMessageSides.isRegistered(getClass())
                    && BannerModMessageSides.declares(getClass(), side)) {
                String msg = "Packet " + getClass().getName() + " declared handler for "
                        + side + " at registration time but reflection lookup of "
                        + methodName + "(" + parameterType.getSimpleName() + ") failed."
                        + " This usually means the handler method was renamed or"
                        + " misspelled (e.g. 'exectueServerSide').";
                LEGACY_LOGGER.error(msg, missing);
                throw new IllegalStateException(msg, missing);
            }
            // Otherwise: legitimate one-way packet (declared the other side only),
            // or a non-side method (fromBytes/toBytes) for which we keep the
            // historical "missing default is a no-op" behaviour.
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access packet method " + methodName + " on " + getClass().getName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Packet method " + methodName + " failed on " + getClass().getName(), cause);
        }
    }

    private static PacketFlow sideForHandler(String methodName) {
        if (BannerModMessageSides.SERVER_SIDE_METHOD.equals(methodName)) {
            return PacketFlow.SERVERBOUND;
        }
        if (BannerModMessageSides.CLIENT_SIDE_METHOD.equals(methodName)) {
            return PacketFlow.CLIENTBOUND;
        }
        return null;
    }

    static PacketFlow serverbound() {
        return PacketFlow.SERVERBOUND;
    }

    static PacketFlow clientbound() {
        return PacketFlow.CLIENTBOUND;
    }
}
