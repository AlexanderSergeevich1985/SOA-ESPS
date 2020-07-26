package com.soaesps.core.Utils.spring;

import com.soaesps.core.Utils.HashGeneratorHelper;

import org.apache.commons.math3.random.RandomGenerator;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class InjectRandomIntBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger;

    static {
        logger = Logger.getLogger(InjectRandomIntBeanPostProcessor.class.getName());
        logger.setLevel(Level.INFO);
    }

    public static class RandomGeneratorHolder {
        private static RandomGenerator rng = HashGeneratorHelper
                .getRandomGenerator(new Random(System.nanoTime()).nextInt());
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, String
                    .format("postProcessBeforeInitialization::beanName = %1$s, beanClass = %2$s",
                            beanName, bean.getClass().getSimpleName()));
        }

        final Field[] fields = bean.getClass().getDeclaredFields();

        for (final Field field : fields) {
            if (field.isAnnotationPresent(InjectRandomInt.class)) {
                field.setAccessible(true);
                InjectRandomInt annotation = field.getAnnotation(InjectRandomInt.class);
                ReflectionUtils.setField(field, bean, getRandomIntInRange(annotation.min(), annotation.max()));
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) {
        return bean;
    }

    private int getRandomIntInRange(final int min, final int max) {
        return HashGeneratorHelper.getRandLimited(RandomGeneratorHolder.rng, min, max);
    }
}