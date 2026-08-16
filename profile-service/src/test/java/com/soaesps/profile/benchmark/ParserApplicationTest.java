package com.soaesps.profile.benchmark;

import com.soaesps.profile.utils.ParserApplication;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * JMH benchmark suite for CSV/XML record parsers.
 */
public class ParserApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(ParserApplicationTest.class);

    @State(Scope.Thread)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class ParserApplicationBenchmark {

        private final ParserApplication application = new ParserApplication();

        private static final Properties properties = new Properties();
        private String recordsFilePath;

        static {
            try (InputStream is = ParserApplicationBenchmark.class
                    .getClassLoader().getResourceAsStream("test.properties")) {
                if (is != null) {
                    properties.load(is);
                } else {
                    LoggerFactory.getLogger(ParserApplicationBenchmark.class)
                            .warn("test.properties not found on classpath");
                }
            } catch (IOException ex) {
                LoggerFactory.getLogger(ParserApplicationBenchmark.class)
                        .error("Failed to load test.properties", ex);
            }
        }

        @Param({"csv", "xml"})
        private String type;

        @Setup(Level.Iteration)
        public void setup() {
            this.recordsFilePath = switch (type) {
                case "csv" -> properties.getProperty("test.filePath.csv");
                case "xml" -> properties.getProperty("test.filePath.xml");
                default -> throw new IllegalStateException("Unknown type: " + type);
            };
        }

        @Benchmark
        public void usingCsvParser() {
            application.loadRecordsFromCSVFile(recordsFilePath);
        }

        @Benchmark
        public void usingXMLParser() {
            application.loadRecordsFromXMLFile(recordsFilePath);
        }
    }

    @Test
    public void startBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParserApplicationBenchmark.class.getSimpleName())
                .addProfiler(StackProfiler.class)
                .build();

        Collection<RunResult> runResults = new Runner(opt).run();
        assertFalse(runResults.isEmpty());

        for (RunResult runResult : runResults) {
            logger.info("Benchmark result: {}", runResult.getAggregatedResult());
        }
    }
}