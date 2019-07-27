package com.soaesps.core.Utils.DataStructure;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BaseQueue<T extends Serializable> {
    private volatile long counter;
    private AtomicLong size = new AtomicLong(0);
    private AtomicBoolean writeFlag = new AtomicBoolean(true);
    private AtomicBoolean readFlag = new AtomicBoolean(true);

    private volatile Node<T> first;
    private volatile Node<T> last;

    public BaseQueue() {
        counter = 0L;
        this.first = new Node<>();
        this.last = new Node<>();
        this.first.next = this.last;
        this.last.prev = this.first;
    }

    public long getSize() {
        return size.get();
    }

    public boolean push(final T transaction) {
        if(!writeFlag.compareAndSet(true, false)) return false;
        try {
            if(this.size.get() > 1) {
                final Node<T> newNode = new Node<>(last, transaction);
                this.last.next = newNode;
                this.last = newNode;
            }
            else if(this.size.get() == 1) {
                this.last.item = transaction;
            }
            else {
                this.first.item = transaction;
            }
            this.size.incrementAndGet();
            return true;
        }
        finally {
            writeFlag.set(true);
        }
    }

    public T pull() {
        if(!readFlag.compareAndSet(true, false)) return null;
        try {
            T result;
            if(this.size.get() > 2) {
                result = this.first.item;
                this.first.item = null;
                final Node<T> next = this.first.next;
                this.first.next = null;
                this.first = next;
                this.first.prev = null;
                this.size.decrementAndGet();
            }
            else if(this.size.get() == 0) {
                return null;
            }
            else {
                if(!writeFlag.compareAndSet(true, false)) return null;
                result = this.first.item;
                if(this.size.get() == 2) {
                    this.first.item = this.last.item;
                    this.last.item = null;
                }
                else {
                    this.first.item = null;
                }
                this.size.decrementAndGet();
                writeFlag.set(true);
            }
            return result;
        }
        finally {
            readFlag.set(true);
        }
    }

    private static class Node<E> {
        volatile E item;
        Node<E> next;
        Node<E> prev;

        Node() {}

        Node(Node<E> prev, E element) {
            this.item = element;
            this.prev = prev;
        }

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}