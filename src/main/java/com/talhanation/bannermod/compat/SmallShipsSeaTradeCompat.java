package com.talhanation.bannermod.compat;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SmallShipsSeaTradeCompat {

    private static final List<ResourceLocation> SUPPORTED_VESSEL_TYPES = List.of(
            ResourceLocation.fromNamespaceAndPath("smallships", "cog"),
            ResourceLocation.fromNamespaceAndPath("smallships", "galley"),
            ResourceLocation.fromNamespaceAndPath("smallships", "drakkar"),
            ResourceLocation.fromNamespaceAndPath("smallships", "rowboat"),
            ResourceLocation.fromNamespaceAndPath("smallships", "brigg"),
            ResourceLocation.fromNamespaceAndPath("smallships", "dhow")
    );
    private static final String REASON_ABSENT = "chat.bannermod.sea_trade.carrier.unsupported.smallships_absent";
    private static final String REASON_UNSUPPORTED_TYPE = "chat.bannermod.sea_trade.carrier.unsupported.type";
    private static final String REASON_INCOMPATIBLE_ENTITY = "chat.bannermod.sea_trade.carrier.unsupported.incompatible_entity";
    private static final String REASON_MISSING_CARGO = "chat.bannermod.sea_trade.carrier.unsupported.missing_cargo";
    private static final String REASON_MISSING_POSITION = "chat.bannermod.sea_trade.carrier.unsupported.missing_position";
    private static final String REASON_MISSING_NAVIGATION = "chat.bannermod.sea_trade.carrier.unsupported.missing_navigation";
    private static final List<String> CARGO_ACCESSORS = List.of(
            "getCargoContainer",
            "getCargoInventory",
            "getCargo",
            "getContainer",
            "getInventory"
    );
    private static final List<String> NAVIGATION_ACCESSORS = List.of(
            "setDestination",
            "setRouteDestination",
            "moveTo",
            "getNavigation"
    );

    private SmallShipsSeaTradeCompat() {
    }

    public static List<CarrierCandidate> candidateCarrierTypes() {
        return candidateCarrierTypes(BannerModMain.isSmallShipsLoaded, BuiltInRegistries.ENTITY_TYPE.keySet());
    }

    public static boolean hasBindableCarrierCandidate() {
        return hasBindableCarrierCandidate(BannerModMain.isSmallShipsLoaded, BuiltInRegistries.ENTITY_TYPE.keySet(), Class::forName);
    }

    static boolean hasBindableCarrierCandidate(boolean smallShipsLoaded,
                                               Collection<ResourceLocation> registeredEntityTypes,
                                               SmallShips.ReflectiveClassResolver classResolver) {
        return !candidateCarrierTypes(smallShipsLoaded, registeredEntityTypes).isEmpty()
                && SmallShips.hasSmallShipEntityClass(classResolver);
    }

    static List<CarrierCandidate> candidateCarrierTypes(boolean smallShipsLoaded, Collection<ResourceLocation> registeredEntityTypes) {
        if (!smallShipsLoaded || registeredEntityTypes == null || registeredEntityTypes.isEmpty()) {
            return List.of();
        }

        return SUPPORTED_VESSEL_TYPES.stream()
                .filter(registeredEntityTypes::contains)
                .map(CarrierCandidate::new)
                .toList();
    }

    public static Optional<BoundCarrier> bindCarrier(@Nullable Entity entity, UUID routeOwnerId) {
        if (entity == null) return Optional.empty();

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return bindCarrier(entity, typeId, entity.getUUID(), routeOwnerId, BannerModMain.isSmallShipsLoaded, Class::forName);
    }

    public static Optional<String> unsupportedCarrierReason(@Nullable Entity entity) {
        if (entity == null) return Optional.of(REASON_INCOMPATIBLE_ENTITY);

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return inspectCarrierSupport(entity, typeId, BannerModMain.isSmallShipsLoaded, Class::forName).unsupportedReason();
    }

    static Optional<BoundCarrier> bindCarrier(@Nullable Object entity,
                                             @Nullable ResourceLocation typeId,
                                             UUID carrierId,
                                             UUID routeOwnerId,
                                             boolean smallShipsLoaded,
                                             SmallShips.ReflectiveClassResolver classResolver) {
        if (!smallShipsLoaded || entity == null || typeId == null || carrierId == null || routeOwnerId == null) {
            return Optional.empty();
        }
        if (!SUPPORTED_VESSEL_TYPES.contains(typeId)) {
            return Optional.empty();
        }
        if (!SmallShips.isSmallShipEntity(entity, classResolver)) {
            return Optional.empty();
        }
        CarrierSupport support = inspectCarrierSupport(entity, typeId, smallShipsLoaded, classResolver);
        if (!support.supported()) {
            return Optional.empty();
        }
        return Optional.of(new BoundCarrier(typeId, carrierId, routeOwnerId, support));
    }

    static CarrierSupport inspectCarrierSupport(@Nullable Object entity,
                                                @Nullable ResourceLocation typeId,
                                                boolean smallShipsLoaded,
                                                SmallShips.ReflectiveClassResolver classResolver) {
        if (!smallShipsLoaded) {
            return CarrierSupport.unsupported(REASON_ABSENT);
        }
        if (entity == null || typeId == null) {
            return CarrierSupport.unsupported(REASON_INCOMPATIBLE_ENTITY);
        }
        if (!SUPPORTED_VESSEL_TYPES.contains(typeId)) {
            return CarrierSupport.unsupported(REASON_UNSUPPORTED_TYPE);
        }
        if (!SmallShips.isSmallShipEntity(entity, classResolver)) {
            return CarrierSupport.unsupported(REASON_INCOMPATIBLE_ENTITY);
        }
        if (!hasUsableNoArgAccessor(entity, CARGO_ACCESSORS)) {
            return CarrierSupport.unsupported(REASON_MISSING_CARGO);
        }
        if (!hasPositionAndStateAccess(entity)) {
            return CarrierSupport.unsupported(REASON_MISSING_POSITION);
        }
        if (!hasNavigationSupport(entity)) {
            return CarrierSupport.unsupported(REASON_MISSING_NAVIGATION);
        }
        return CarrierSupport.supportedCarrier();
    }

    private static boolean hasUsableNoArgAccessor(Object entity, List<String> methodNames) {
        return Arrays.stream(entity.getClass().getMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> methodNames.contains(method.getName()))
                .anyMatch(method -> invoke(method, entity).isPresent());
    }

    private static boolean hasPositionAndStateAccess(Object entity) {
        Class<?> type = entity.getClass();
        boolean hasCoordinates = hasNoArgMethod(type, "getX") && hasNoArgMethod(type, "getY") && hasNoArgMethod(type, "getZ");
        boolean hasState = hasNoArgMethod(type, "isAlive") || hasNoArgMethod(type, "isRemoved");
        return hasCoordinates && hasState;
    }

    private static boolean hasNavigationSupport(Object entity) {
        return Arrays.stream(entity.getClass().getMethods())
                .filter(method -> NAVIGATION_ACCESSORS.contains(method.getName()))
                .anyMatch(method -> method.getParameterCount() > 0 || invoke(method, entity).isPresent());
    }

    private static boolean hasNoArgMethod(Class<?> type, String methodName) {
        return Arrays.stream(type.getMethods())
                .anyMatch(method -> method.getParameterCount() == 0 && methodName.equals(method.getName()));
    }

    private static Optional<Object> invoke(Method method, Object target) {
        try {
            method.setAccessible(true);
            return Optional.ofNullable(method.invoke(target));
        }
        catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public record CarrierCandidate(ResourceLocation entityTypeId) {
    }

    public record BoundCarrier(ResourceLocation entityTypeId, UUID carrierId, UUID routeOwnerId, CarrierSupport support) {
    }

    public record CarrierSupport(boolean supported, Optional<String> unsupportedReason) {
        private static CarrierSupport supportedCarrier() {
            return new CarrierSupport(true, Optional.empty());
        }

        private static CarrierSupport unsupported(String unsupportedReason) {
            return new CarrierSupport(false, Optional.of(unsupportedReason));
        }
    }
}
