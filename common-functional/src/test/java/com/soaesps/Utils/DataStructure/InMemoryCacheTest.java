package com.soaesps.Utils.DataStructure;

import com.soaesps.core.Utils.DataStructure.AbstractInMemoryCache;
import com.soaesps.core.Utils.DataStructure.CacheI;
import org.openjdk.jmh.annotations.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 8)
public class InMemoryCacheTest {
    private Integer DEFAULT_COUNTER = 1000;

    private CacheI<Long, TestObject> cache;

    private List<Long> probes = new ArrayList<>(AbstractInMemoryCache.DEFAULT_MAX_CASHE_SIZE);

    @Benchmark
    public void addSomeTestObject() {
        int counter = 0;
        while (counter < DEFAULT_COUNTER) {
            addOneTestObject();
            ++counter;
        }
    }

    public void addOneTestObject() {
        TestObject object = TestObject.getOneTestObject();
        cache.addWithEvict(object.getId(), object);
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