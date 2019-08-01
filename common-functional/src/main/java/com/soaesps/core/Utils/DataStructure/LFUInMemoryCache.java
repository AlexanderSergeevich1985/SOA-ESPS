/**The MIT License (MIT)
 Copyright (c) 2019 by AleksanderSergeevich
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.soaesps.core.Utils.DataStructure;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LFUInMemoryCache<T, ID extends Serializable> extends AbstractInMemoryCache<T, ID> {
    static public long DEFAULT_OBSERVATION_TIME_INTERVAL = 1000;

    private ConcurrentLinkedQueue<CacheKey<ID>> queue = new ConcurrentLinkedQueue<>();

    @Override
    protected void updateStats(final AbstractInMemoryCache.CacheKey<ID> key) {
        if (queue.size() >= DEFAULT_OBSERVATION_TIME_INTERVAL) {
            CacheKey<ID> earlist = queue.poll();
            earlist.setFrequency(earlist.getFrequency() - 1);
        }
        key.setFrequency(key.getFrequency() + 1);
        queue.add(key);
    }

    @Override
    public Comparator<CacheKey<ID>> createComparator() {
        return (o1, o2) -> o1.getFrequency().compareTo(o2.getFrequency());
    }
}