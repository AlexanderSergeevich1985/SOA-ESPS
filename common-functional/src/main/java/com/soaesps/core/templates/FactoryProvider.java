package com.soaesps.core.templates;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FactoryProvider {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(FactoryProvider.class.getName());
        logger.setLevel(Level.INFO);
    }

    public static AbstractFactory getFactory(final String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        Class clazz = null;
        try {
            clazz = Class.forName(name);
            if(AbstractFactory.class.isAssignableFrom(clazz)) {
                return (AbstractFactory) clazz.newInstance();
            }
        }
        catch (final ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.log(Level.INFO, "[FactoryProvider/getFactory]: {}", ex);
        }

        return null;
    }
}