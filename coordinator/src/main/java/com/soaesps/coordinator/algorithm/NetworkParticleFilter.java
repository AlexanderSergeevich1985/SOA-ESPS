package com.soaesps.coordinator.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Production implementation of the {@link NetworkParticleFilter} abstraction.
 * Executes lock-free sequential Monte Carlo steps with pre-calculated statistics.
 */
@Component
public class NetworkParticleFilter implements PredictionAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(NetworkParticleFilter.class);
    private static final int N = 100;
    private static final double PROCESS_NOISE_SIGMA = 2.0;
    private static final double MEASUREMENT_NOISE_SIGMA = 5.0;
    private static final double REGULARIZATION_SIGMA = 1.0;
    private static final double ESS_THRESHOLD = N / 2.0;

    private final AtomicReference<FilterState> state = new AtomicReference<>(initParticles());

    private static FilterState initParticles() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double[] p = new double[N];
        double[] w = new double[N];
        for (int i = 0; i < N; i++) {
            p[i] = 50.0 + rnd.nextGaussian() * 10.0;
            w[i] = 1.0 / N;
        }
        return new FilterState(p, w);
    }

    @Override
    public void observeRtt(double rttMs) {
        // Guard against NaN or negative values from broken clocks/interceptors
        if (Double.isNaN(rttMs) || rttMs < 0.0) {
            return;
        }

        while (true) {
            FilterState current = state.get();
            FilterState next = step(current, rttMs);
            if (state.compareAndSet(current, next)) {
                break; // Successfully updated
            }
            // CAS failed, retry with the new current state
        }
    }

    @Override
    public double getPrediction() {
        FilterState current = state.get();
        return current.mean + 2.0 * current.stdDev;
    }

    private FilterState step(FilterState s, double measurement) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double[] newWeights = new double[N];
        double sumW = 0.0;
        double sumSquaredW = 0.0;
        double inv2sigma2 = 1.0 / (2.0 * MEASUREMENT_NOISE_SIGMA * MEASUREMENT_NOISE_SIGMA);

        for (int i = 0; i < N; i++) {
            double diff = measurement - s.particles[i];
            double w = s.weights[i] * Math.exp(-diff * diff * inv2sigma2);
            newWeights[i] = w;
            sumW += w;
        }

        if (sumW <= 0) sumW = 1.0;
        for (int i = 0; i < N; i++) {
            newWeights[i] /= sumW;
            sumSquaredW += newWeights[i] * newWeights[i];
        }

        double ess = 1.0 / sumSquaredW;
        double[] nextParticles = new double[N];
        double[] nextWeights = new double[N];

        if (ess < ESS_THRESHOLD) {
            double u = rnd.nextDouble() / N;
            int idx = 0;
            double cumulative = newWeights[0];

            for (int i = 0; i < N; i++) {
                double target = u + (double) i / N;
                while (idx < N - 1 && cumulative < target) {
                    idx++;
                    cumulative += newWeights[idx];
                }
                nextParticles[i] = s.particles[idx]
                        + rnd.nextGaussian() * PROCESS_NOISE_SIGMA
                        + rnd.nextGaussian() * REGULARIZATION_SIGMA;
            }
            Arrays.fill(nextWeights, 1.0 / N);
        } else {
            for (int i = 0; i < N; i++) {
                nextParticles[i] = s.particles[i] + rnd.nextGaussian() * PROCESS_NOISE_SIGMA;
            }
            nextWeights = newWeights;
        }

        return new FilterState(nextParticles, nextWeights);
    }

    /**
     * Immutable snapshot of the filter state with pre-computed statistics.
     */
    private static final class FilterState {
        final double[] particles;
        final double[] weights;

        final double mean;
        final double stdDev;

        FilterState(double[] p, double[] w) {
            this.particles = p;
            this.weights = w;

            // Single-pass statistical evaluation upon state creation
            double sum = 0.0;
            for (double val : p) {
                sum += val;
            }
            this.mean = sum / p.length;

            double var = 0.0;
            for (double val : p) {
                double d = val - this.mean;
                var += d * d;
            }
            this.stdDev = Math.sqrt(var / p.length);
        }
    }
}
