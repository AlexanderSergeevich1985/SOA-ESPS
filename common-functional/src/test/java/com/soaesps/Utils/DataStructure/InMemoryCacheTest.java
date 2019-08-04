package com.soaesps.Utils.DataStructure;

import com.soaesps.config.InMemoryCacheConfiguration;
import com.soaesps.core.Utils.DataStructure.CacheI;
import com.soaesps.core.Utils.DataStructure.LFUInMemoryCache;
import com.soaesps.core.Utils.DataStructure.LRUInMemoryCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Spy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = {InMemoryCacheConfiguration.class})
public class InMemoryCacheTest {
    static private final Logger logger = Logger.getLogger(InMemoryCacheTest.class.getName());

    private static DecimalFormat df = new DecimalFormat("0.000");

    private static final double REFERENCE_SCORE = 37.132;

    private Integer DEFAULT_COUNTER = 10000;

    @Spy
    @Autowired
    public CacheI<Long, TestObject> lruCache;

    @Spy
    @Autowired
    public CacheI<Long, TestObject> lfuCache;

    static private CacheI<Long, TestObject> cache;

    private CopyOnWriteArrayList<Long> probes = new CopyOnWriteArrayList<>();

    @Before
    public void initTest() {
        try {
            cache = new LFUInMemoryCache<>();
        }
        catch (final Exception ex) {
            if (logger.isLoggable(java.util.logging.Level.INFO)) {
                logger.log(Level.INFO, "[InMemoryCacheTest/initTest] exception occurred: ", ex);
            }
        }
    }

    @Test
    public void A_contextLoads() {
        Assert.assertNotNull(lruCache);
        Assert.assertNotNull(lfuCache);
    }

    @Param({"LRU", "LFU"})
    private String type;

    @Setup(org.openjdk.jmh.annotations.Level.Iteration)
    public void setUp() {
        switch (type) {
            case "LRU":
                cache = new LRUInMemoryCache<>();
                break;
            case "LFU":
                cache = new LFUInMemoryCache<>();
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }

    @Test
    public void runJmhBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(InMemoryCacheTest.class.getSimpleName())
                .build();
        Collection<RunResult> runResults = new Runner(opt).run();
        Assert.assertFalse(runResults.isEmpty());
    }

    @Benchmark
    public void Benchmark_A_addSeveralTestObject() {
        int counter = 0;
        while (counter < DEFAULT_COUNTER) {
            addGetOneTestObject();
            ++counter;
        }
    }

    public void addGetOneTestObject() {
        TestObject object = TestObject.getOneTestObject();
        cache.addWithEvict(object.getId(), object);
        cache.get(object.getId());
    }

    static public class TestObject implements Serializable {
        private Long id;

        private String descriptor;

        public TestObject() {}

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public void setDescriptor(final String descriptor) {
            this.descriptor = descriptor;
        }

        static public TestObject getOneTestObject() {
            TestObject object = new TestObject();
            object.setId(TestSequenceGenerator.getNext());
            object.setDescriptor("Test");

            return object;
        }
    }

    static public class TestSequenceGenerator {
        static private AtomicLong value = new AtomicLong(1);

        static public long getNext() {
            return value.getAndIncrement();
        }
    }
}