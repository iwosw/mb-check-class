package com.talhanation.recruits.compat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectiveCompatAccessTest {

    @Test
    void missingLookupReturnsEmpty() {
        ReflectiveCompatAccess access = new ReflectiveCompatAccess(name -> {
            throw new ClassNotFoundException(name);
        });

        assertTrue(access.findClass("missing.Type").isEmpty());
        assertTrue(access.findMethod("missing.Type", "noop").isEmpty());
        assertTrue(access.findField("missing.Type", "value").isEmpty());
    }

    @Test
    void presentLookupUsesInjectedResolver() {
        ReflectiveCompatAccess access = new ReflectiveCompatAccess(name -> SampleCompatTarget.class);

        Optional<Class<?>> resolvedClass = access.findClass("ignored.SampleCompatTarget");
        Optional<Method> resolvedMethod = access.findMethod("ignored.SampleCompatTarget", "sampleMethod");

        assertEquals(SampleCompatTarget.class, resolvedClass.orElseThrow());
        assertEquals("sampleMethod", resolvedMethod.orElseThrow().getName());
    }

    @Test
    void missingMembersDegradeToEmpty() {
        ReflectiveCompatAccess access = new ReflectiveCompatAccess(name -> SampleCompatTarget.class);

        assertTrue(access.findMethod("ignored.SampleCompatTarget", "missingMethod").isEmpty());
        assertTrue(access.findField("ignored.SampleCompatTarget", "missingField").isEmpty());
    }

    private static class SampleCompatTarget {
        public void sampleMethod() {
        }
    }
}
