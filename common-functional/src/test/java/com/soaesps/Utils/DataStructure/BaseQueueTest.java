package com.soaesps.Utils.DataStructure;

import com.soaesps.config.BaseQueueTestConfiguration;
import com.soaesps.core.Utils.DataStructure.BaseQueue;
import io.reactivex.Observable;
import org.junit.Assert;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static com.soaesps.core.Utils.DataStructure.CacheI.DEFAULT_MAX_CASHE_SIZE;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = {BaseQueueTestConfiguration.class})
public class BaseQueueTest {
    static private final Logger logger = Logger.getLogger(BaseQueueTest.class.getName());

    @Spy
    @Autowired
    @Qualifier("baseQueue")
    private BaseQueue<TestObject> baseQueue;

    static private BaseQueue<TestObject> queue;

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    @Test
    public void A_contextLoads() {
        Assert.assertNotNull(baseQueue);
    }

    @Test
    public void B_baseQueueCorrectWork() throws InterruptedException {
        fillQueue(baseQueue);
    }

    private void fillQueue(final BaseQueue<TestObject> queue) {
        while (queue.getSize() < DEFAULT_MAX_CASHE_SIZE) {
            TestObject object = TestObject.getOneTestObject();
            queue.push(object);
        }
    }

    /*private Observable getObservable() {
        Observable<TestObject> observer = Observable.fromPublisher(() -> {

        });
    }*/

    @Setup(org.openjdk.jmh.annotations.Level.Iteration)
    public void setUp() {
        queue = new BaseQueue<>();
    }

    @Test
    public void D_runJmhBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(InMemoryCacheTest.class.getSimpleName())
                .build();
        Collection<RunResult> runResults = new Runner(opt).run();
        Assert.assertFalse(runResults.isEmpty());
    }

    @Benchmark
    public void Benchmark_A_addSeveralTestObject() {
        executor.submit(getWriter());
        executor.submit(getReader());
    }

    private Callable<Integer> getReader() {
        return () -> {
            int counter = 0;
            int attempts = 0;
            while (attempts < 100) {
                TestObject object = baseQueue.pull();
                if (object == null) {
                    ++counter;
                }
                ++attempts;
            }
            return counter;
        };
    }

    private Callable<Integer> getWriter() {
        return () -> {
            int counter = 0;
            int attempts = 0;
            while (attempts < 100000) {
                TestObject object = new TestObject();
                baseQueue.push(object);
                ++attempts;
                System.out.println(attempts);
            }
            return counter;
        };
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