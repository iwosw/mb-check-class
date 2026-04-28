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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Adapter for legacy packet classes while their registration/sending runs on NeoForge payload APIs.
 */
public interface BannerModMessage<T extends BannerModMessage<T>> extends Message<T> {
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
        } catch (NoSuchMethodException ignored) {
            // Some packets are one-way and intentionally omit the opposite-side handler.
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

    static PacketFlow serverbound() {
        return PacketFlow.SERVERBOUND;
    }

    static PacketFlow clientbound() {
        return PacketFlow.CLIENTBOUND;
    }
}
