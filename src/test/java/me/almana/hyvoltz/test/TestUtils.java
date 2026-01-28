package me.almana.hyvoltz.test;

import me.almana.hyvoltz.HyVoltzEngine;
import me.almana.hyvoltz.api.HyVoltzAPI;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class TestUtils {
    public static void resetEngine() {
        try {
            // Reset Engine flag
            Field field = HyVoltzEngine.class.getDeclaredField("instantiated");
            field.setAccessible(true);
            field.set(null, false);

            // Reset API static field
            Field apiField = HyVoltzAPI.class.getDeclaredField("engine");
            apiField.setAccessible(true);
            apiField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset HyVoltzEngine/API", e);
        }
    }

    public static HyVoltzEngine createAndRegisterEngine() {
        try {
            Constructor<HyVoltzEngine> constructor = HyVoltzEngine.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            HyVoltzEngine engine = constructor.newInstance();
            HyVoltzAPI.register(engine);
            return engine;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate HyVoltzEngine via reflection", e);
        }
    }
}
