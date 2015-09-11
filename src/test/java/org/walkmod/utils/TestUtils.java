package org.walkmod.utils;

import org.walkmod.WalkModFacade;

import java.lang.reflect.Field;

/**
 * Common methods for testing
 */
public class TestUtils {

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String OS_NAME = System.getProperty("os.name");

    /**
     * Tells if the test is running on Windows
     *
     * @return true if Windows, false if a real OS
     */
    public static boolean isWindows () {
        // Use capital name for Win8+
        return OS_NAME.startsWith("windows") || OS_NAME.startsWith("Windows");
    }


    /**
     * Inspects and object to obtain the the value of a property. </br>
     * Provides a method to access private properties with no accessor.
     *
     * @param instance instance of an object to inspect
     * @param propertyName name of the property to inspect
     * @param expectedType type of the inspected property
     *
     * @return The value of the property
     */
    public static <T> T getValue(Object instance, String propertyName, Class<T> expectedType) {
        Class<WalkModFacade> facade = (Class<WalkModFacade>) instance.getClass();
        try {
            Field field = facade.getDeclaredField(propertyName);
            if (!field.isAccessible())
                field.setAccessible(true);
            return (T)field.get(instance);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
