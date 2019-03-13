package Test;

import TestTask.ParserApplication;
import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;

public class ParserApplicationTest {
    private static Logger logger = Logger.getLogger(ParserApplicationTest.class.getName());

    @State(Scope.Thread)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    static public class ParserApplicationBenchmark {
        private ParserApplication application = new ParserApplication();
        private String recordsFilePath;
        private static Properties properties = new Properties();

        static {
            try(InputStream is = new FileInputStream("resources/test.properties")) {
                properties.load(is);
            }
            catch(FileNotFoundException fnfex) {
                if(logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "FileNotFoundException occur : ", fnfex.toString());
                }
            }
            catch(IOException ioex) {
                if(logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "IOException occur : ", ioex.toString());
                }
            }
        }

        private ParserApplication converter = new ParserApplication();

        @Param({"csv", "xml"})
        private String type;

        @Setup(org.openjdk.jmh.annotations.Level.Iteration)
        public void setup() {
            switch (type) {
                case "csv":
                    this.recordsFilePath = properties.getProperty("test.filePath.csv");
                    break;
                case "xml":
                    this.recordsFilePath = properties.getProperty("test.filePath.xml");
                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + type);
            }
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
        for(RunResult runResult : runResults) {
            System.out.println(runResult.getAggregatedResult().toString());
        }
    }

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(ParserApplicationTest.class);
        for(Failure failure : result.getFailures()) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "Failure occur : ", failure.toString());
            }
        }
    }
}
