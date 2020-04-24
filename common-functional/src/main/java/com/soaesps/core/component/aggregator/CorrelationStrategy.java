package com.soaesps.core.component.aggregator;

import com.soaesps.core.Utils.CryptoHelper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

@FunctionalInterface
public interface CorrelationStrategy {
    Object getCorrelationKey(final Object message);

    class CorrelationKeyStrategy implements CorrelationStrategy {
        private final Function<Object, String> extractor;

        public CorrelationKeyStrategy(final Function<Object, String> extractor) {
            this.extractor = extractor;
        }

        @Override
        public CorrelationKey getCorrelationKey(final Object message) {
            try {
                return CorrelationKey.build(CryptoHelper.getObjectDigest(extractor.apply(message)));
            } catch (IOException | NoSuchAlgorithmException ex) {
                return null;
            }
        }
    }
}