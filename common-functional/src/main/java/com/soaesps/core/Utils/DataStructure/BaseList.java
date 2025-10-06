package com.soaesps.core.Utils.DataStructure;

import java.util.TreeMap;

class BaseList {
    interface ListI {
        ListI subList(int from, int to);
        void set(int index, int value);
        void add(int value);
        int get(int index);
        default boolean checkIndexRange(int from, int to) {
            return from <= to;
        }
    }
    interface IndexStore extends Comparable<IndexStore> {
        int getIndex();
        int getSecondIndex();
    }
    protected static class IndexForAccess implements IndexStore {
        private int index;
        public IndexForAccess(int index) {
            this.index = index;
        }
        @Override
        public int compareTo(IndexStore o) {
            return this.index >= o.getIndex() ? this.index <= o.getSecondIndex() ? 0 : 1 : -1;
        }
        @Override
        public int getIndex() {
            return index;
        }
        @Override
        public int getSecondIndex() {
            return index;
        }
        public IndexForAccess index(int index) {
            this.index = index;
            return this;
        }
    }
    private static class Chunk implements IndexStore {
        private int capacity;
        private int startIndex;
        private int endIndex;
        private int[] values;
        private int curIndex = -1;
        public Chunk(int sti, int size) {
            this.startIndex = sti;
            this.endIndex = this.startIndex + size - 1;
            this.capacity = this.endIndex - this.startIndex + 1;
            this.values = new int[this.capacity];
        }
        public int getByIndex(int index) {
            return values[index-startIndex];
        }
        public void setByIndex(int index, int value) {
            values[index-startIndex] = value;
        }
        public boolean isFull() {
            return curIndex > capacity - 2;
        }
        public void add(int value) {
            values[++curIndex] = value;
        }
        @Override
        public int compareTo(IndexStore o) {
            return Integer.compare(this.endIndex, o.getIndex());
        }
        @Override
        public int getIndex() {
            return this.startIndex;
        }
        @Override
        public int getSecondIndex() {
            return this.endIndex;
        }
    }
    private static class Storage implements ListI {
        private int initSize;
        private int size;
        private IndexForAccess accessKey = new IndexForAccess(0);
        private TreeMap<IndexStore, int[]> register = new TreeMap<>();
        public Storage(int initSize) {
            Chunk chunk = getNewChunk(0, initSize);
            register.put(chunk, chunk.values);
            this.size = initSize;
            this.initSize = size;
        }
        private Chunk getNewChunk(int sti, int size) {
            if (sti < 0 || size < 1) {
                throw new IllegalArgumentException();
            }
            return new Chunk(sti, size);
        }
        public Chunk addChunk(int size) {
            Chunk chunk = new Chunk(this.size, size);
            this.size += size;
            this.register.put(chunk, chunk.values);
            return chunk;
        }
        @Override
        public ListI subList(int from, int to) {
            return null;
        }
        @Override
        public void set(int index, int value) {
            if (index < 0 || index > size-1) {
                throw new IndexOutOfBoundsException();
            }
            Chunk chunk = (Chunk) this.register.ceilingKey(accessKey.index(index));
            chunk.setByIndex(index, value);
        }
        @Override
        public void add(int value) {
            Chunk current = (Chunk) this.register.lastKey();
            if (current.isFull()) {
                this.initSize *= 2;
                current = addChunk(this.initSize);
            }
            current.add(value);
        }
        @Override
        public int get(int index) {
            if (index < 0 || index > size-1) {
                throw new IndexOutOfBoundsException();
            }
            Chunk chunk = (Chunk) this.register.floorKey(accessKey.index(index));
            return chunk.getByIndex(index);
        }
        public int size() {
            return this.size;
        }
    }
    protected static class List implements ListI {
        public static final int DEFAULT_INIT_SIZE = 100;
        private final Storage storage;
        public List() {
            this(DEFAULT_INIT_SIZE);
        }
        public List(int initSize) {
            this.storage = new Storage(initSize);
        }
        public List(int...values) {
            this(DEFAULT_INIT_SIZE);
            for (int value: values) {
                this.add(value);
            }
        }
        @Override
        public SubList subList(int from, int to) {
            checkIndexRange(from, to);
            return new SubList(from, to, this);
        }
        @Override
        public void set(int index, int value) {
            if (index < 1 || index > storage.size()) {
                throw new IndexOutOfBoundsException();
            }
            this.storage.set(index-1, value);
        }
        @Override
        public void add(int value) {
            storage.add(value);
        }
        @Override
        public int get(int index) {
            if (index < 1 || index > storage.size()) {
                throw new IndexOutOfBoundsException();
            }
            return storage.get(index-1);
        }
    }
    public static class SubList extends List {
        private final int startIndex;
        private final int endIndex;
        private final int size;
        private final List ref;
        private SubList(int sti, int ei, List ref) {
            this.startIndex = sti;
            this.endIndex = ei;
            this.size = endIndex - startIndex + 1;
            this.ref = ref;
        }
        @Override
        public int get(int index) {
            if (index < 1 || index > size) {
                throw new IllegalArgumentException();
            }
            return ref.get(startIndex+index-1);
        }
        @Override
        public void set(int index, int value) {
            if (index < 1 || index > size) {
                throw new IndexOutOfBoundsException();
            }
            this.ref.set(startIndex+index-1, value);
        }
        @Override
        public SubList subList(int from, int to) {
            if (from < 1 || from > to || to > size) {
                throw new IndexOutOfBoundsException();
            }
            return ref.subList(startIndex + from - 1, startIndex + to - 1);
        }
        public void add(int value) {
            throw new UnsupportedOperationException();
        }
        public int getStartIndex() {
            return this.startIndex;
        }
        public int getEndIndex() {
            return this.endIndex;
        }
    }
}