package com.example.rpgplugin.api.skript.events;

import ch.njol.skript.Skript;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

final class SkriptTestBootstrap {

    private static boolean initialized;

    private SkriptTestBootstrap() {
    }

    static void ensureInitialized() {
        if (initialized) {
            return;
        }
        synchronized (SkriptTestBootstrap.class) {
            if (initialized) {
                return;
            }
            try {
                Field instanceField = Skript.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                Object instance = instanceField.get(null);
                if (instance == null) {
                    instance = allocateInstance(Skript.class);
                    instanceField.set(null, instance);
                }
                setJavaPluginEnabled(instance, true);

                Field skriptField = Skript.class.getDeclaredField("skript");
                skriptField.setAccessible(true);
                if (skriptField.get(null) == null) {
                    skriptField.set(null, org.skriptlang.skript.Skript.of(SkriptTestBootstrap.class, "test"));
                }

                Field unmodifiableField = Skript.class.getDeclaredField("unmodifiableSkript");
                unmodifiableField.setAccessible(true);
                if (unmodifiableField.get(null) == null) {
                    org.skriptlang.skript.Skript skriptInstance =
                            (org.skriptlang.skript.Skript) skriptField.get(null);
                    unmodifiableField.set(null, skriptInstance.unmodifiableView());
                }

                Field acceptField = Skript.class.getDeclaredField("acceptRegistrations");
                acceptField.setAccessible(true);
                acceptField.setBoolean(null, true);

                initialized = true;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to initialize Skript for tests", e);
            }
        }
    }

    private static Object allocateInstance(Class<?> type) throws ReflectiveOperationException {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        return unsafe.allocateInstance(type);
    }

    private static void setJavaPluginEnabled(Object instance, boolean enabled) throws ReflectiveOperationException {
        Field enabledField = JavaPlugin.class.getDeclaredField("isEnabled");
        enabledField.setAccessible(true);
        enabledField.setBoolean(instance, enabled);
    }
}
