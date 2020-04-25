package com.soaesps.core.component.aggregator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageBatchExt<T, ID extends Serializable> extends MessageBatch<ID> {
    protected ConcurrentSkipListSet<T> messages;

    public MessageBatchExt() {
        super();
        this.messages = new ConcurrentSkipListSet<>();
    }

    public MessageBatchExt(final Comparator<ID> comparator) {
        super(comparator);
        this.messages = new ConcurrentSkipListSet<>();
    }

    public MessageBatchExt(final Comparator<T> comparator, final Comparator<ID> comparatorId) {
        super(comparatorId);
        this.messages = new ConcurrentSkipListSet<>(comparator);
    }

    public ConcurrentSkipListSet<T> getMessages() {
        return messages;
    }

    public void setMessages(final Collection<T> messages) {
        this.messages.addAll(messages);
    }

    public void addMessage(final T message) {
        this.messages.add(message);
    }

    public void removeMessage(final T message, final ID id) {
        if (this.messages.remove(message)) {
            super.keys.add(id);
        }
    }

    public boolean isContainsMessage(final T message) {
        return this.messages.contains(message);
    }
}