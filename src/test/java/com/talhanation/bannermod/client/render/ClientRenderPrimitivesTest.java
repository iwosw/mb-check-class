package com.talhanation.bannermod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientRenderPrimitivesTest {
    @Test
    void texturedBillboardQuadEmitsVisibleBobberQuad() {
        RecordingVertexConsumer recording = RecordingVertexConsumer.create();

        ClientRenderPrimitives.texturedBillboardQuad(recording.consumer, new Matrix4f(), 15728880);

        assertEquals(4, recording.count("addVertex"));
        assertEquals(4, recording.count("setUv"));
        assertEquals(4, recording.count("setOverlay"));
        assertEquals(4, recording.count("setLight"));
        assertEquals(4, recording.count("setNormal"));
        recording.calls("setColor").forEach(call -> assertEquals(List.of(255, 255, 255, 255), call.args));
    }

    @Test
    void lineBoxEmitsAllWorkAreaOutlineEdges() {
        RecordingVertexConsumer recording = RecordingVertexConsumer.create();

        ClientRenderPrimitives.lineBox(new PoseStack(), recording.consumer, new AABB(0.0D, 1.0D, 2.0D, 3.0D, 4.0D, 5.0D), 1.0F, 1.0F, 1.0F, 1.0F);

        assertEquals(24, recording.count("addVertex"));
        assertEquals(24, recording.count("setColor"));
        assertEquals(24, recording.count("setNormal"));
        recording.calls("setNormal").forEach(call -> assertNonZeroNormal(call.args));
    }

    @Test
    void lineStripVertexEmitsOpaqueFishingLineVertex() {
        RecordingVertexConsumer recording = RecordingVertexConsumer.create();

        ClientRenderPrimitives.lineStripVertex(new PoseStack().last(), recording.consumer,
                1.0F, 2.0F, 3.0F,
                0.0F, 0.0F, 0.0F, 1.0F,
                0.0F, 1.0F, 0.0F);

        assertEquals(1, recording.count("addVertex"));
        assertEquals(List.of(0.0F, 0.0F, 0.0F, 1.0F), recording.calls("setColor").getFirst().args);
        assertNonZeroNormal(recording.calls("setNormal").getFirst().args);
    }

    private static void assertNonZeroNormal(List<Object> args) {
        float x = (Float) args.get(0);
        float y = (Float) args.get(1);
        float z = (Float) args.get(2);
        assertTrue(x * x + y * y + z * z > 0.0F);
    }

    private static final class RecordingVertexConsumer implements InvocationHandler {
        private final List<Call> calls = new ArrayList<>();
        private VertexConsumer consumer;

        static RecordingVertexConsumer create() {
            RecordingVertexConsumer recording = new RecordingVertexConsumer();
            recording.consumer = (VertexConsumer) Proxy.newProxyInstance(
                    VertexConsumer.class.getClassLoader(),
                    new Class<?>[]{VertexConsumer.class},
                    recording);
            return recording;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            calls.add(new Call(method.getName(), args == null ? List.of() : List.of(args)));
            if (method.getReturnType().isAssignableFrom(VertexConsumer.class)) {
                return consumer;
            }
            if (method.getReturnType() == boolean.class) {
                return false;
            }
            return null;
        }

        long count(String name) {
            return calls(name).size();
        }

        List<Call> calls(String name) {
            return calls.stream()
                    .filter(call -> call.name.equals(name))
                    .toList();
        }
    }

    private record Call(String name, List<Object> args) {
    }
}
